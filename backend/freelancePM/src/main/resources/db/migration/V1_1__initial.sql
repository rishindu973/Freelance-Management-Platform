CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL, --BCrypt hash only no raw text
                       role VARCHAR(20) NOT NULL
);

CREATE TABLE manager(
                        user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE ,
                        full_name VARCHAR(255) NOT NULL,
                        company_name VARCHAR(255),
                        contact_number VARCHAR(20)
);

CREATE TABLE freelancer(
                           user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE ,
                           manager_id INTEGER REFERENCES users(id),
                           full_name VARCHAR(255) NOT NULL,
                           title VARCHAR(255),
                           contact_number VARCHAR(20),
                           salary DECIMAL(10,2),
                           status VARCHAR(50),
                           drive_link TEXT
);

CREATE TABLE client(
                       id SERIAL PRIMARY KEY ,
                       manager_id INTEGER REFERENCES users(id),
                       name VARCHAR(255) NOT NULL ,
                       company_name VARCHAR(255),
                       contract_type VARCHAR(100),
                       contact_number VARCHAR(20),
                       email VARCHAR(255),
                       payment_status VARCHAR(50)
);

CREATE TABLE project(
                        id SERIAL PRIMARY KEY ,
                        client_id INTEGER REFERENCES client(id) ON DELETE CASCADE ,
                        manager_id INTEGER REFERENCES users(id),
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        type VARCHAR(100),
                        start_date DATE,
                        deadline DATE,
                        status VARCHAR(50) DEFAULT 'pending'
);

CREATE TABLE invoice(
                        id SERIAL PRIMARY KEY ,
                        client_id INTEGER REFERENCES client(id),
                        project_id INTEGER REFERENCES project(id),
                        amount DECIMAL(12,2) NOT NULL ,
                        type VARCHAR(50),
                        generated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        description TEXT,
                        status VARCHAR(20) DEFAULT 'due'
);

CREATE TABLE finance(
                        id SERIAL PRIMARY KEY ,
                        project_id INTEGER REFERENCES project(id),
                        freelancer_id INTEGER REFERENCES freelancer(user_id),
                        manager_id INTEGER REFERENCES manager(id),
                        amount DECIMAL(12,2) NOT NULL ,
                        flag VARCHAR(20), -- to hold payment status: done, due
                        type VARCHAR(20), -- to hold income, expense
                        entry_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE report(
                       id SERIAL PRIMARY KEY ,
                       type VARCHAR(20), -- foe monthly/weekly/annually
                       generated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       total_income DECIMAL(15,2),
                       total_expense DECIMAL(15,2),
                       total_profit DECIMAL(15,2)
);

CREATE TABLE report_detail(
                              id SERIAL PRIMARY KEY ,
                              report_id INTEGER REFERENCES report(id) ON DELETE CASCADE ,
                              client_id INTEGER REFERENCES client(id),
                              project_id INTEGER REFERENCES project(id),
                              income DECIMAL(12,2),
                              expense DECIMAL(12,2),
                              profit DECIMAL(12,2)
);

CREATE TABLE project_freelancer (
                                    project_id INTEGER REFERENCES project(id) ON DELETE CASCADE ,
                                    freelancer_id INTEGER REFERENCES freelancer(user_id) ON DELETE CASCADE ,
                                    PRIMARY KEY (project_id,freelancer_id)
);