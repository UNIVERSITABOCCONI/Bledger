alter table bc_transaction
ADD column node_id varchar(36);

ALTER TABLE bc_transaction
    ADD CONSTRAINT fk_bc_tx_node_id FOREIGN KEY (node_id) REFERENCES bc_node (id);