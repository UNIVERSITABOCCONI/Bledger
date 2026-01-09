ALTER TABLE bc_wallet
    DROP CONSTRAINT chk_bc_wallet_type;

ALTER TABLE bc_wallet
    ADD CONSTRAINT chk_bc_wallet_type
    CHECK (wallet_type IN ('COMPANY', 'AUDITOR', 'PLATFORM'));