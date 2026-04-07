-- Add quantity column to batches table
ALTER TABLE batches ADD COLUMN quantity BIGINT NULL;

-- Update existing records with the initial batch quantity (if you have a way to determine it)
-- For example, if you know the original quantities, update them here
-- UPDATE batches SET quantity = <original_quantity> WHERE id = <id>;

-- If availableQuantity was calculated as (quantity - issued), you might want to set quantity based on current data
-- This is just an example - adjust based on your actual data
-- UPDATE batches SET quantity = availableQuantity WHERE quantity IS NULL;
