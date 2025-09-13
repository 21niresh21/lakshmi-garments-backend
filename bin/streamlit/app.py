import psycopg2
import pandas as pd
import streamlit as st
from dotenv import load_dotenv
import os
import matplotlib.pyplot as plt
import plotly.graph_objects as go
import plotly.express as px

# Load environment variables from .env file
load_dotenv()

# Retrieve database connection details from environment variables
host = os.getenv("DB_HOST")
dbname = os.getenv("DB_NAME")
user = os.getenv("DB_USER")
password = os.getenv("DB_PASSWORD")
port = os.getenv("DB_PORT")


def get_connection():
    """Returns a connection to the PostgreSQL database."""
    return psycopg2.connect(
        host=host,
        dbname=dbname,
        user=user,
        password=password,
        port=port
    )

# Fetch data from database


def fetch_data(query, params=None):
    """
    Fetch data from PostgreSQL database using a parameterized query.
    """
    connection = get_connection()
    try:
        # Executes the SQL query and returns a DataFrame
        df = pd.read_sql(query, connection, params=params)
    except Exception as e:
        st.error(f"Error fetching data: {str(e)}")
        return pd.DataFrame()  # Returns an empty dataframe if there's an error
    finally:
        connection.close()
    return df


def add_item(query, params):
    connection = get_connection()
    try:
        # Open a cursor and execute the query
        with connection.cursor() as cursor:
            cursor.execute(query, params)
            connection.commit()
            st.success(f"Supplier '{params[0]}' added successfully.")
    except Exception as e:
        connection.rollback()
        st.error(f"Error inserting supplier: {str(e)}")
    finally:
        connection.close()


def get_category_and_sub_category_current_stock():
    query = """
    SELECT
    c.name AS category_name,
    s.name AS sub_category_name,
    SUM(w.count) AS total_count
    FROM
        categories c
    JOIN
        warehouses w ON c.id = w.category_id
    JOIN
        sub_categories s ON s.id = w.sub_category_id
    GROUP BY
        c.name, s.name
    ORDER BY
        c.name, s.name;
    """
    return fetch_data(query)


def get_one_supplier_metrics(supplier_id):
    # Use string formatting (f-string) to inject the supplier_id into the query
    query = f"""
        SELECT
            s.name AS supplier_name,
            SUM(i.transport_charge + CAST(i.invoice_number AS FLOAT)) AS total_cost,  -- Cast invoice_number to FLOAT
            SUM(b.quantity) AS total_quantity,
            COUNT(i.id) AS total_invoices  -- Count the number of invoices for the supplier
        FROM
            suppliers s
        JOIN
            invoices i ON s.id = i.supplier_id
        LEFT JOIN
            bales b ON b.lorry_receipt_id = i.id  -- Joining the bales table to get the quantity
        WHERE
            s.id = {supplier_id}  -- Inject supplier_id dynamically using f-string
        GROUP BY
            s.id;
    """
    return fetch_data(query)


def get_category_total_stock():
    query = """
    SELECT c.name AS category_name,
           SUM(w.count) AS total_count
    FROM warehouses w
    JOIN categories c ON w.category_id = c.id
    GROUP BY c.name
    """
    # Replace with actual data fetching logic
    return fetch_data(query)


# @st.dialog("Add Supplier")
# def add_supplier():
#     new_supplier_name = st.text_input("Supplier Name")
#     if st.button("Submit"):
#             query = """INSERT INTO suppliers (name) VALUES (%s)"""
#             params = (new_supplier_name, )
#             add_item(query, params)
#             print(new_supplier_name)
#         # st.rerun()


st.set_page_config(layout="wide")


def page_1():
    st.title("Inventory")

    # Fetch the data
    df = get_category_total_stock()

    # Check if there is data
    if df.empty:
        st.warning("No data found.")
        return

    # Create columns dynamically
    col_count = len(df)
    # Create columns based on the number of categories
    columns = st.columns(col_count)

    # Loop through each category and display using st.metric
    for i, row in df.iterrows():
        category_name = row['category_name']
        total_count = row['total_count']

        # Display the metric in the corresponding column
        with columns[i]:
            st.metric(label=category_name,
                      value=f"{total_count}", delta=None, border=True)

    df = get_category_and_sub_category_current_stock()
    # Display raw data (optional)
    # Create a dictionary to hold the counts for each sub-category in each category
    categories = df['category_name'].unique()
    sub_categories = df['sub_category_name'].unique()
    data_dict = {sub_category: [] for sub_category in sub_categories}

    # Populate the dictionary with the corresponding counts
    for category in categories:
        category_data = df[df['category_name'] == category]
        for sub_category in sub_categories:
            count = category_data[category_data['sub_category_name']
                                  == sub_category]['total_count']
            data_dict[sub_category].append(
                count.iloc[0] if not count.empty else 0)

    # Create traces for Plotly bar chart
    traces = []
    for sub_category in sub_categories:
        traces.append(go.Bar(
            name=sub_category,
            x=categories,
            y=data_dict[sub_category],
        ))

    # Create layout for bar chart
    layout = go.Layout(
        barmode='group',  # Grouped bars
        title="Total Count of Items by Category and Sub-category",
        xaxis=dict(title="Category"),
        yaxis=dict(title="Total Count"),
        xaxis_tickangle=-45
    )

    # Create the bar chart figure
    bar_fig = go.Figure(data=traces, layout=layout)

    # Create sunburst chart using Plotly Express
    sunburst_fig = px.sunburst(
        df,
        path=['category_name', 'sub_category_name'],  # Hierarchical path
        values='total_count',  # Define values to size the sectors
        title="Total Count of Items by Category and Sub-category"
    )

    # Create a new DataFrame with hierarchical indices (Category -> Sub-category)
    df_grouped = df.pivot_table(
        index='category_name', columns='sub_category_name', values='total_count', aggfunc='sum')

    # Fill NaN values with 0 if there are any subcategories without data
    df_grouped = df_grouped.fillna(0)

    # Display the data in a table (st.dataframe) with categories as rows and subcategories as columns
    st.dataframe(df_grouped, use_container_width=True)

    # Layout with two columns

    col1, col2 = st.columns(2, border=True)

    with col1:
        # Display bar chart in the first column
        st.plotly_chart(bar_fig, use_container_width=True)

    with col2:
        # Display sunburst chart in the second column
        st.plotly_chart(sunburst_fig)
# Function for page 2


def page_2():
    st.title("Page 2")

    supplier_data = get_one_supplier_metrics(1)
    print(supplier_data.get('total_cost', 0))
    # Default to 0 if the key doesn't exist
    total_cost = supplier_data.get('total_cost', 0)
    # Default to 0 if the key doesn't exist
    total_quantity = supplier_data.get('total_quantity', 0)
    # Default to 0 if the key doesn't exist
    total_invoices = supplier_data.get('total_invoices', 0)

    # Ensure that the values are numeric (float for cost and int for quantity and invoices)
    total_cost = float(total_cost) if isinstance(
        total_cost, (int, float)) else 0
    total_quantity = int(total_quantity) if isinstance(
        total_quantity, int) else 0
    total_invoices = int(total_invoices) if isinstance(
        total_invoices, int) else 0

    metrics = [
        ("Total Cost", f"₹ {total_cost:,.2f}"),
        ("Total Quantity", f"{total_quantity:,.0f}"),
        ("Total Invoices", f"{total_invoices:,}")
    ]

    # Create three columns
    col1, col2, col3 = st.columns(3)

    # Use zip() to combine the label and values for display in columns
    for col, (label, value) in zip([col1, col2, col3], metrics):
        col.metric(label=label, value=value)


def page_3():
    col1, col2 = st.columns(2)
    
    # Fetch supplier names from the database
    supplier_names = fetch_data("SELECT name FROM suppliers")

    st.header("Invoice Details")
    st.radio(
        "Choose the shipment type",
        ["freight", "own pickup"],
        key="visibility",
        index=None,
        horizontal=True
    )
    st.text_input("Invoice Number")
    st.date_input("Invoice Date", value="today")
    st.date_input("Shipment Received Date", value="today")
    st.selectbox(
        "Supplier Name",
        supplier_names['name'],  # Ensure you're accessing the correct column
        index=None,
        placeholder="Select contact method..."
    )
    
    # Check if 'Add Supplier' button is clicked
    if st.button("Add Supplier"):
        # Set session state to show the supplier form
        st.session_state.show_add_supplier_form = True
    
    # If the form for adding a supplier is visible in session state
    if st.session_state.get("show_add_supplier_form", False):
        new_supplier_name = st.text_input("New Supplier Name")
        new_supplier_location = st.text_input("New Supplier Location")
        
        if st.button("Submit Supplier"):
            # Insert new supplier into the database
            query = """INSERT INTO suppliers (name, location) VALUES (%s, %s)"""
            params = (new_supplier_name, new_supplier_location)
            add_item(query, params)  # Call add_item function to add supplier
            st.success(f"Supplier '{new_supplier_name}' added successfully.")
            st.session_state.show_add_supplier_form = False  # Hide the form after submission
            st.experimental_rerun()  # Re-run the app to show the updated data
    
    # Transport details section
    transport_names = fetch_data("SELECT name FROM transports")
    st.header("Transport Details")
    st.selectbox(
        "Transport Supplier",
        transport_names['name'],  # Ensure you're accessing the correct column
        index=None,
        placeholder="Select transport method..."
    )
    transport_charge = st.number_input(
        "Transport Charge", min_value=0, value=None)
    # submitted = st.form_submit_button("Submit")

# Sidebar Navigation with links
st.sidebar.title("Navigation")

# Links in the sidebar
page = st.sidebar.radio("Choose a page", ("Page 1", "Page 2", "Page 3"))

# Render content based on selected page
if page == "Page 1":
    page_1()
elif page == "Page 2":
    page_2()
elif page == "Page 3":
    page_3()
