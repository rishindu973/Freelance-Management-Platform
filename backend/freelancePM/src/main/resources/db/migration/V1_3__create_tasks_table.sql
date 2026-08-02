CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    manager_id INTEGER NOT NULL,
    freelancer_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    

    CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_freelancer FOREIGN KEY (freelancer_id) REFERENCES users(id) ON DELETE CASCADE
);