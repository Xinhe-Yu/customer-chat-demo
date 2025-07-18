-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Table: clients
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    client_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: agencies
CREATE TABLE agencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    agency_data JSONB
);

-- Table: agents
CREATE TABLE agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    secret VARCHAR(255) UNIQUE NOT NULL,
    agency_id UUID NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    agent_data JSONB
);

-- Table: offers (formerly reservations)
CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_id UUID NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    acriss_cat VARCHAR(20) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    pickup_city VARCHAR(100) NOT NULL,
    dropoff_city VARCHAR(100) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    offer_data JSONB
);

-- Table: reservations (booking of an offer by a client)
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_id UUID UNIQUE NOT NULL REFERENCES offers(id),
    client_id UUID REFERENCES clients(id),
    status VARCHAR(50),
    payment_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: support_tickets
CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID REFERENCES clients(id),
    issue_type VARCHAR(100),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
    agent_id UUID REFERENCES agents(id),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
