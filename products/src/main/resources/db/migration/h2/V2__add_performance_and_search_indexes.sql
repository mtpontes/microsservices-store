CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_manufacturer ON products (manufacturer_id);
CREATE INDEX idx_products_current_price ON products (current_price);

CREATE INDEX idx_products_name_description ON products (name, description);
