-- Inserting a variety of customers for comprehensive testing

-- Active customers
INSERT INTO customer (name, cpf, email, phone, status, birth_date, created_at, updated_at) VALUES
('Joao da Silva', '12345678901', 'joao.silva@email.com', '11999990001', 'ACTIVE', '1990-03-12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Maria Souza', '98765432100', 'maria.souza@email.com', '11999990002', 'ACTIVE', '1988-07-21', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lucas Martins', '10203040506', 'lucas.martins@email.com', '11999990006', 'ACTIVE', '1993-02-25', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inactive customers
INSERT INTO customer (name, cpf, email, phone, status, birth_date, created_at, updated_at) VALUES
('Carlos Pereira', '11122233344', 'carlos.pereira@email.com', '11999990003', 'INACTIVE', '1979-11-05', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fernanda Lima', '99988877766', 'fernanda.lima@email.com', '11999990005', 'INACTIVE', '1992-09-17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Suspended customer
INSERT INTO customer (name, cpf, email, phone, status, birth_date, created_at, updated_at) VALUES
('Ana Costa', '55566677788', 'ana.costa@email.com', '11999990004', 'SUSPENDED', '1995-01-30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Customer for deletion test
INSERT INTO customer (name, cpf, email, phone, status, birth_date, created_at, updated_at) VALUES
('Cliente a Deletar', '99999999999', 'deletar@email.com', '11999999999', 'ACTIVE', '2000-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
