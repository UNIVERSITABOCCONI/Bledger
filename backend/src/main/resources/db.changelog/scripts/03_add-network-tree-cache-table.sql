CREATE TABLE bc_network_tree_cache (
    id VARCHAR(36),
    network_id VARCHAR(36),
    tree_json TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_network_tree_cache
    ADD CONSTRAINT pk_bc_network_tree_cache PRIMARY KEY (id),
    ADD CONSTRAINT fk_bc_network_tree_cache_network_id FOREIGN KEY (network_id) REFERENCES bc_network (id);
