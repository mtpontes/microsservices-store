CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT,
    created_at TIMESTAMP(6) NULL,
    is_active BOOLEAN NOT NULL,
    modified_at TIMESTAMP(6) NULL,
    name VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK_departments_name UNIQUE (name)
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT,
    created_at TIMESTAMP(6) NULL,
    is_active BOOLEAN NOT NULL,
    modified_at TIMESTAMP(6) NULL,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK_categories_name UNIQUE (name),
    CONSTRAINT FK_categories_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

CREATE TABLE manufacturers (
    id BIGINT AUTO_INCREMENT,
    additional_info VARCHAR(255) NULL,
    city VARCHAR(50) NULL,
    country VARCHAR(50) NULL,
    postal_code VARCHAR(20) NULL,
    state VARCHAR(50) NULL,
    street VARCHAR(100) NULL,
    contact_person VARCHAR(100) NULL,
    created_at TIMESTAMP(6) NULL,
    email VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL,
    modified_at TIMESTAMP(6) NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(19) NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK_manufacturers_name UNIQUE (name)
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT,
    created_at TIMESTAMP(6) NULL,
    description VARCHAR(255) NULL,
    main_image VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL,
    modified_at TIMESTAMP(6) NULL,
    name VARCHAR(100) NOT NULL,
    current_price DECIMAL(38,2) NULL,
    end_promotion TIMESTAMP(6) NULL,
    on_promotion BOOLEAN NULL,
    original_price DECIMAL(38,2) NULL,
    promotional_price DECIMAL(38,2) NULL,
    start_promotion TIMESTAMP(6) NULL,
    specs VARCHAR(255) NULL,
    unit INT NOT NULL,
    category_id BIGINT NULL,
    manufacturer_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK_products_name UNIQUE (name),
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT FK_products_manufacturer FOREIGN KEY (manufacturer_id) REFERENCES manufacturers (id)
);

CREATE TABLE product_additional_images (
    product_id BIGINT NOT NULL,
    additional_images VARCHAR(255) NULL,
    CONSTRAINT FK_additional_images_product FOREIGN KEY (product_id) REFERENCES products (id)
);
