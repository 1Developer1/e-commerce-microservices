CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price_amount DECIMAL(10, 2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    stock_quantity INT NOT NULL
);

INSERT INTO products (id, name, description, price_amount, price_currency, stock_quantity)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'MacBook Pro', 'Apple M3 Pro islemcili 16 inc MacBook Pro', 1999.99, 'USD', 10),
    ('22222222-2222-2222-2222-222222222222', 'AirPods Max', 'Gurultu engelleyici kablosuz kulak ustu kulaklik', 549.00, 'USD', 25);
