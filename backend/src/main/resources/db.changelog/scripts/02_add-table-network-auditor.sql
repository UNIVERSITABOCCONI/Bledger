CREATE TABLE bc_network_auditor (
    id VARCHAR(36),
    auditor_id VARCHAR(36),
    network_id VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted BOOLEAN
);
ALTER TABLE bc_network_auditor
    ADD CONSTRAINT pk_bc_network_auditor PRIMARY KEY (id),
    ADD CONSTRAINT fk_bc_network_auditor_network_id FOREIGN KEY (network_id) REFERENCES bc_network (id),
    ADD CONSTRAINT fk_bc_network_auditor_auditor_id FOREIGN KEY (auditor_id) REFERENCES bc_company (id);

-----------------------------------------------------

ALTER TABLE bc_network DROP COLUMN network_auditor_id;
ALTER TABLE bc_network DROP COLUMN scope3_enabled;

ALTER TABLE bc_node ADD COLUMN auditor_id VARCHAR(36);

ALTER TABLE bc_node
    ADD CONSTRAINT fk_bc_node_auditor_id FOREIGN KEY (auditor_id) REFERENCES bc_company (id);