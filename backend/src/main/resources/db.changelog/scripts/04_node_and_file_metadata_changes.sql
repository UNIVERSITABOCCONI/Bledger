ALTER TABLE bc_file
ADD COLUMN file_status VARCHAR(255),
ADD COLUMN extracted_data TEXT;

ALTER TABLE bc_node_metadata
ADD COLUMN type VARCHAR(255),
ADD COLUMN category VARCHAR(255),
ADD COLUMN source_of_emission VARCHAR(255),
ADD COLUMN unit_of_measure VARCHAR;

ALTER TABLE bc_node
ADD COLUMN granular_scope1_file_id VARCHAR,
ADD COLUMN granular_scope2_file_id VARCHAR,
ADD CONSTRAINT fk_bc_node_granular_scope1_file_id FOREIGN KEY (granular_scope1_file_id) REFERENCES bc_file (id),
ADD CONSTRAINT fk_bc_node_granular_scope2_file_id FOREIGN KEY (granular_scope2_file_id) REFERENCES bc_file (id);
