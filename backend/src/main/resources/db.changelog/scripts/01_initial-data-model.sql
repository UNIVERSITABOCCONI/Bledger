CREATE TABLE bc_wallet (
    id VARCHAR(36),
    wallet_address VARCHAR(255),
    balance NUMERIC(38, 2),
    to_deploy BOOLEAN,
    deployed BOOLEAN,
    deployed_at TIMESTAMP,
    salt TEXT,
    wallet_type VARCHAR,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_wallet
    ADD CONSTRAINT pk_bc_wallet PRIMARY KEY (id),
    ADD CONSTRAINT uq_bc_wallet_salt UNIQUE (salt),
    ADD CONSTRAINT chk_bc_wallet_type CHECK (wallet_type IN ('COMPANY', 'PLATFORM'));

---------------------------------------------------

CREATE TABLE bc_company (
    id VARCHAR(36),
    email VARCHAR(255),
    nation VARCHAR(255),
    company_name VARCHAR(255),
    id_type VARCHAR(255),
    id_number VARCHAR(255),
    representative_name VARCHAR(255),
    representative_surname VARCHAR(255),
    wallet_id VARCHAR(36),
    profile_image_id VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_company
    ADD CONSTRAINT pk_bc_company PRIMARY KEY (id),
    ADD CONSTRAINT chk_bc_company_id_type CHECK (
        id_type IN ('ID_CARD', 'PASSPORT', 'DRIVING_LICENSE', 'TAX_CODE', 'OTHER')
    );

---------------------------------------------------

CREATE TABLE bc_company_role (
    id VARCHAR(36),
    company_id VARCHAR(36),
    role VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_company_role
    ADD CONSTRAINT pk_bc_company_role PRIMARY KEY (id),
    ADD CONSTRAINT chk_bc_company_role CHECK (
        role IN ('BUSINESS_UNIT', 'BUSINESS_NETWORK_ADMIN', 'THIRD_PARTY_AUDITOR')
    );

---------------------------------------------------

CREATE TABLE bc_network (
    id VARCHAR(36),
    root_id VARCHAR(36),
    network_admin_id VARCHAR(36),
    network_auditor_id VARCHAR(36),
    network_name VARCHAR(255),
    members_count INT,
    uploaded_count INT,
    audited_count INT,
    scope3_enabled BOOLEAN,
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_network
    ADD CONSTRAINT pk_bc_network PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_node (
    id VARCHAR(36),
    network_id VARCHAR(36),
    parent_id VARCHAR(36),
    company_id VARCHAR(36),
    scope_file_id VARCHAR(36),
    node_status VARCHAR(255),
    audited BOOLEAN,
    audited_at TIMESTAMP,
    upload_file_transaction_id VARCHAR(36),
    scope3_transaction_id VARCHAR(36),
    audit_file_transaction_id VARCHAR(36),
    scope1 NUMERIC,
    scope2 NUMERIC,
    scope3 NUMERIC,
    node_depth INT,
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_node
    ADD CONSTRAINT pk_bc_node PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_node_metadata (
    id VARCHAR(36),
    metadata_key VARCHAR(255),
    metadata_value TEXT,
    node_id VARCHAR(36),
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_node_metadata
    ADD CONSTRAINT pk_bc_node_metadata PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_transaction (
    id VARCHAR(36),
    tx_hash VARCHAR(255),
    creator_id VARCHAR(36),
    from_wallet_id VARCHAR(36),
    from_wallet_address VARCHAR(255),
    to_address VARCHAR(255),
    gas_price NUMERIC,
    gas_used NUMERIC,
    block_number INT,
    status VARCHAR(50),
    payload TEXT,
    transaction_data TEXT,
    ready_at TIMESTAMP,
    try_count INT,
    confirmed_at TIMESTAMP,
    transaction_type VARCHAR(255),
    network_id VARCHAR(36),
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_transaction
    ADD CONSTRAINT pk_bc_transaction PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_transaction_wallet_recipient (
    id VARCHAR(36),
    transaction_id VARCHAR(36),
    wallet_id VARCHAR(36),
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_transaction_wallet_recipient
    ADD CONSTRAINT pk_bc_transaction_wallet_recipient PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_file_binary (
    id VARCHAR(36),
    file_binary BYTEA,
    deleted BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE bc_file_binary
    ADD CONSTRAINT pk_bc_file_binary PRIMARY KEY (id);

---------------------------------------------------

CREATE TABLE bc_file (
    id VARCHAR(36),
    type VARCHAR(255),
    mime_type VARCHAR(255),
    size INT,
    file_name VARCHAR(255),
    sha256 VARCHAR(255),
    binary_id VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_file
    ADD CONSTRAINT pk_bc_file PRIMARY KEY (id);

---- FOREIGN KEYS ----

-- bc_company
ALTER TABLE bc_company
    ADD CONSTRAINT fk_bc_company_wallet FOREIGN KEY (wallet_id) REFERENCES bc_wallet (id),
    ADD CONSTRAINT fk_bc_company_profile_image FOREIGN KEY (profile_image_id) REFERENCES bc_file (id);

-- bc_company_role
ALTER TABLE bc_company_role
    ADD CONSTRAINT fk_bc_company_role_company FOREIGN KEY (company_id) REFERENCES bc_company (id);

-- bc_network
ALTER TABLE bc_network
    ADD CONSTRAINT fk_bc_network_root_node FOREIGN KEY (root_id) REFERENCES bc_node (id),
    ADD CONSTRAINT fk_bc_network_admin FOREIGN KEY (network_admin_id) REFERENCES bc_company (id),
    ADD CONSTRAINT fk_bc_network_auditor FOREIGN KEY (network_auditor_id) REFERENCES bc_company (id)
    ;

-- bc_node
ALTER TABLE bc_node
    ADD CONSTRAINT fk_bc_node_network FOREIGN KEY (network_id) REFERENCES bc_network (id),
    ADD CONSTRAINT fk_bc_node_parent FOREIGN KEY (parent_id) REFERENCES bc_node (id),
    ADD CONSTRAINT fk_bc_node_company FOREIGN KEY (company_id) REFERENCES bc_company (id),
    ADD CONSTRAINT fk_bc_node_scope_file FOREIGN KEY (scope_file_id) REFERENCES bc_file (id),
    ADD CONSTRAINT fk_bc_node_upload_file_transaction FOREIGN KEY (upload_file_transaction_id) REFERENCES bc_transaction (id),
    ADD CONSTRAINT fk_bc_node_scope3_transaction FOREIGN KEY (scope3_transaction_id) REFERENCES bc_transaction (id),
    ADD CONSTRAINT fk_bc_node_audit_file_transaction FOREIGN KEY (audit_file_transaction_id) REFERENCES bc_transaction (id);

-- bc_node_metadata
ALTER TABLE bc_node_metadata
    ADD CONSTRAINT fk_bc_node_metadata_node FOREIGN KEY (node_id) REFERENCES bc_node (id);

-- bc_transaction
ALTER TABLE bc_transaction
    ADD CONSTRAINT fk_bc_transaction_wallet FOREIGN KEY (from_wallet_id) REFERENCES bc_wallet (id),
    ADD CONSTRAINT fk_bc_transaction_network FOREIGN KEY (network_id) REFERENCES bc_network (id);

-- bc_transaction_wallet_recipient
ALTER TABLE bc_transaction_wallet_recipient
    ADD CONSTRAINT fk_bc_tx_wallet_recipient_tx FOREIGN KEY (transaction_id) REFERENCES bc_transaction (id),
    ADD CONSTRAINT fk_bc_tx_wallet_recipient_wallet FOREIGN KEY (wallet_id) REFERENCES bc_wallet (id);

-- bc_file
ALTER TABLE bc_file
    ADD CONSTRAINT fk_bc_file_binary FOREIGN KEY (binary_id) REFERENCES bc_file_binary (id);
