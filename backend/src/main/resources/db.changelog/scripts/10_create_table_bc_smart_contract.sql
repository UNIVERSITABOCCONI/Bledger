CREATE TABLE bc_smart_contract (
    id VARCHAR(36),
    contract_address VARCHAR(255),
    type VARCHAR,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_smart_contract
    ADD CONSTRAINT pk_bc_smart_contract PRIMARY KEY (id);