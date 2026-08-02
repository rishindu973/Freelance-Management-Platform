CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER REFERENCES invoice(id) ON DELETE CASCADE,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'completed'  -- completed, failed, refunded
);