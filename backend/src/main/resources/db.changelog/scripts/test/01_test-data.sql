-- This file contains test data used for unit tests.
-- It should be updated whenever the database structure changes.
-- It does not need to be versioned, since tests always run with
-- spring.liquibase.drop-first=true.


-- Insert wallets
INSERT INTO bc_wallet (
    id,
    wallet_address,
    balance,
    to_deploy,
    deployed,
    deployed_at,
    salt,
    wallet_type,
    created_at,
    updated_at,
    deleted_at,
    deleted,
    is_whitelisted
)
VALUES
('wallet-001', '0xABCDEF1234567890', 1000.00, TRUE, FALSE, NULL, '1001', 'COMPANY', NOW(), NOW(), NULL, FALSE, FALSE),
('wallet-002', '0x1234567890ABCDEF', 2000.00, FALSE, TRUE, NOW(), '1002', 'COMPANY', NOW(), NOW(), NULL, FALSE, TRUE),
('wallet-003', '0xA1B2C3D4E5F6', 5000.00, FALSE, TRUE, NOW(), '1003', 'PLATFORM', NOW(), NOW(), NULL, FALSE, TRUE);

-- Insert file binary
INSERT INTO bc_file_binary (
    id,
    file_binary,
    deleted,
    created_at,
    updated_at,
    deleted_at
)
VALUES
('binary-001', decode('48656c6c6f20776f726c64', 'hex'), FALSE, NOW(), NOW(), NULL),  -- "Hello world" as binary
('binary-002', decode('5365636f6e642066696c65', 'hex'), FALSE, NOW(), NOW(), NULL);  -- "Second file" as binary

--- Insert files
INSERT INTO bc_file (
    id,
    type,
    mime_type,
    size,
    file_name,
    keccak256,
    binary_id,
    file_status,
    extracted_data,
    created_at,
    updated_at,
    deleted_at,
    deleted
)
VALUES
('file-001', 'PROFILE_IMAGE', 'image/png', 2048, 'logo.png', 'abc123fakehash', 'binary-001', 'CONFIRMED', '{"width":64}', NOW(), NOW(), NULL, FALSE),
('file-002', 'GRANULAR_SCOPE_1', 'application/pdf', 4096, 'scope.pdf', 'def456fakehash', 'binary-002', 'PENDING', '{"pages":2}', NOW(), NOW(), NULL, FALSE);

-- Insert companies
INSERT INTO bc_company (
    id,
    email,
    company_name,
    nation,
    representative_name,
    representative_surname,
    id_type,
    id_number,
    wallet_id,
    profile_image_id,
    created_at,
    updated_at,
    deleted_at,
    deleted,
    company_type
)
VALUES
('company-001', 'alpha@example.com', 'Alpha Corp', 'Italy', 'Mario', 'Rossi', 'ID_CARD', 'ID123456', 'wallet-001', 'file-001', NOW(), NOW(), NULL, FALSE, 'ORG'),
('company-002', 'beta@example.com', 'Beta Ltd', 'Germany', 'Anna', 'Muller', 'PASSPORT', 'P987654', 'wallet-002', 'file-002', NOW(), NOW(), NULL, FALSE, 'TPA');

-- Step 1: Create the network without root_node_id
INSERT INTO bc_network (
    id,
    root_id,
    network_admin_id,
    network_name,
    members_count,
    uploaded_count,
    audited_count,
    deleted,
    created_at,
    updated_at,
    deleted_at,
    token_id
)
VALUES
('network-001', NULL, 'company-001', 'Sustainability Network', 3, 0, 0, FALSE, NOW(), NOW(), NULL, 10001);

-- Step 2: Create nodes referencing the network (parent_id can be null for now)
INSERT INTO bc_node (
    id,
    network_id,
    parent_id,
    company_id,
    scope_file_id,
    granular_scope1_file_id,
    granular_scope2_file_id,
    node_status,
    audited,
    audited_at,
    upload_file_transaction_id,
    scope3_transaction_id,
    audit_file_transaction_id,
    scope1,
    scope2,
    scope3,
    e_value,
    production_volume,
    quantity,
    transportation_emission,
    total_scope1_and_scope2,
    node_depth,
    approval_expiration_date,
    auditor_id,
    last_export,
    last_compute,
    last_compute_change,
    deleted,
    created_at,
    updated_at,
    deleted_at
)
VALUES
('node-001', 'network-001', NULL, 'company-001', 'file-001', 'file-002', 'file-002', 'ACCEPTED', FALSE, NULL, NULL, NULL, NULL, '10', '20', 30.5, 100.5, 200.0, 300.0, 15.0, '30', 0, CURRENT_DATE, 'company-002', NOW(), NOW(), NOW(), FALSE, NOW(), NOW(), NULL),  -- root
('node-002', 'network-001', 'node-001', 'company-002', 'file-002', 'file-002', 'file-001', 'INVITED', FALSE, NULL, NULL, NULL, NULL, '5', '7', 12.0, 50.0, 60.0, 70.0, 5.0, '12', 1, CURRENT_DATE, 'company-001', NOW(), NOW(), NOW(), FALSE, NOW(), NOW(), NULL),
('node-003', 'network-001', 'node-001', 'company-002', 'file-001', 'file-001', 'file-002', 'REFUSED', FALSE, NULL, NULL, NULL, NULL, '3', '4', 6.0, 30.0, 40.0, 50.0, 3.0, '7', 1, CURRENT_DATE, 'company-002', NOW(), NOW(), NOW(), FALSE, NOW(), NOW(), NULL);

-- Step 3: Update network with the actual root_node_id
UPDATE BC_NETWORK
SET root_id = 'node-001'
WHERE id = 'network-001';

-- Insert metadata for nodes
INSERT INTO bc_node_metadata (
    id,
    node_id,
    type,
    metadata_key,
    metadata_value,
    category,
    source_of_emission,
    unit_of_measure,
    deleted,
    created_at,
    updated_at,
    deleted_at
)
VALUES
('meta-001', 'node-001', 'GLOBAL_SCOPE', 'region', 'Europe', 'scope', 'energy', 'kWh', FALSE, NOW(), NOW(), NULL),
('meta-002', 'node-002', 'SCOPE1_GRANULAR', 'department', 'R&D', 'scope1', 'transport', 'kg', FALSE, NOW(), NOW(), NULL),
('meta-003', 'node-003', 'SCOPE2_GRANULAR', 'note', 'Third-party node', 'scope2', 'logistics', 'kg', FALSE, NOW(), NOW(), NULL);

-- Insert network auditors
INSERT INTO bc_network_auditor (
    id,
    auditor_id,
    network_id,
    created_at,
    updated_at,
    deleted_at,
    deleted,
    requests_count,
    audited_count
)
VALUES
('auditor-001', 'company-002', 'network-001', NOW(), NOW(), NULL, FALSE, 2, 1);

-- Insert network tree cache
INSERT INTO bc_network_tree_cache (
    id,
    network_id,
    tree_json,
    created_at,
    updated_at,
    deleted_at,
    deleted
)
VALUES
('tree-001', 'network-001', '{"id":"node-001","networkId":"network-001","companyId":"company-001","companyName":"Alpha Corp","nodeDepth":0,"children":[]}', NOW(), NOW(), NULL, FALSE);

-- Insert transactions
INSERT INTO bc_transaction (
    id,
    tx_hash,
    creator_id,
    from_wallet_id,
    from_wallet_address,
    to_address,
    gas_price,
    gas_used,
    block_number,
    status,
    payload,
    transaction_data,
    ready_at,
    try_count,
    confirmed_at,
    transaction_type,
    network_id,
    node_id,
    transaction_receipt,
    deleted,
    created_at,
    updated_at,
    deleted_at
)
VALUES
('tx-001', '0xTX001', 'company-001', 'wallet-001', '0xABCDEF1234567890', '0xRecipient1', 50, 21000, 1001, 'CONFIRMED', '{}', '{}', NOW(), 1, NOW(), 'UPLOAD_FILE', 'network-001', 'node-001', '{"status":"ok"}', FALSE, NOW(), NOW(), NULL),
('tx-002', '0xTX002', 'company-002', 'wallet-002', '0x1234567890ABCDEF', '0xRecipient2', 60, 25000, 1002, 'PENDING', '{}', '{}', NOW(), 0, NULL, 'AUDIT', 'network-001', 'node-002', '{"status":"pending"}', FALSE, NOW(), NOW(), NULL);

-- Insert transaction wallet recipients
INSERT INTO bc_transaction_wallet_recipient (
    id,
    transaction_id,
    wallet_id,
    deleted,
    created_at,
    updated_at,
    deleted_at
)
VALUES
('twr-001', 'tx-001', 'wallet-002', FALSE, NOW(), NOW(), NULL),
('twr-002', 'tx-002', 'wallet-001', FALSE, NOW(), NOW(), NULL);

-- Insert smart contracts
INSERT INTO bc_smart_contract (
    id,
    contract_address,
    type,
    created_at,
    updated_at,
    deleted_at,
    deleted
)
VALUES
('sc-001', '0xSC001', 'ERC721', NOW(), NOW(), NULL, FALSE),
('sc-002', '0xSC002', 'IDENTITY', NOW(), NOW(), NULL, FALSE);

-- Insert BLedger network metadata
INSERT INTO bc_b_ledger_network_metadata (
    id,
    network_id,
    content_hash,
    content,
    created_at,
    updated_at,
    deleted_at,
    deleted
)
VALUES
('meta-network-001', 'network-001', 'hash-001', '{"network":"data"}', NOW(), NOW(), NULL, FALSE);

