INSERT INTO roles (name) VALUES ('Super Admin');
INSERT INTO roles (name) VALUES ('Accounts Admin');
INSERT INTO roles (name) VALUES ('Production Admin');

INSERT INTO users (is_active, name, password, role_id) VALUES (true, 'admin', 'admin', 1);
INSERT INTO users (is_active, name, password, role_id) VALUES (true, 'bala', 'bala', 2);

INSERT INTO transports (name) VALUES ('A1');

INSERT INTO suppliers (location, name) VALUES ('annur', 'siva');
INSERT INTO suppliers (location, name) VALUES ('cbe', 'anoop');

INSERT INTO categories (code, name) VALUES ('D', 'dull');
INSERT INTO categories (code, name) VALUES ('P', 'Premium');

INSERT INTO sub_categories (name) VALUES ('sleeve');
INSERT INTO sub_categories (name) VALUES ('half-sleeve');

INSERT INTO items (name) VALUES ('premium piping');

INSERT INTO employees (name) VALUES ('niresh');

INSERT INTO employee_skills (employee_id, skill_id) VALUES (1,1);

-- INSERT INTO batch_statuses (name) VALUES ('Created');
-- INSERT INTO batch_statuses (name) VALUES ('Work In Progress');
-- INSERT INTO batch_statuses (name) VALUES ('Packaged');

-- insert into damage_types (name, description) values ("Repairable","Item is damaged but can be repaired");
-- insert into damage_types (name, description) values ("Unrepairable","Item is damaged beyond repair");
-- insert into damage_types (name, description) values ("Supplier Damage","Item is damaged due to supplier's fault");

-- insert into jobwork_types (name) values ("Cutting");
-- insert into jobwork_types (name) values ("Stitching");
-- insert into jobwork_types (name) values ("Packaging");