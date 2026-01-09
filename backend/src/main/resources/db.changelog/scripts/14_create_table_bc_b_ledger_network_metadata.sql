CREATE TABLE bc_b_ledger_network_metadata (
    id VARCHAR(36),
    network_id VARCHAR(255),
    content_hash VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);

ALTER TABLE bc_b_ledger_network_metadata
    ADD CONSTRAINT pk_bc_b_ledger_network_metadata PRIMARY KEY (id);

