// SPDX-License-Identifier: MIT
pragma solidity ^0.8.22;

import "@openzeppelin/contracts/access/manager/AccessManager.sol";

/**
 * IdentityManager
 * - Minimal wrapper around OpenZeppelin AccessManager.
 * - Defines global role IDs to be used in the rest of the system.
 *
 * Roles:
 *  - ADMIN_ROLE   = 0 (type(uint64).min) → owner/administrator (the PLATFORM Safe)
 *  - COMPANY_ROLE = 1 → company wallets (can own NFTs)
 *  - AUDITOR_ROLE = 2 → auditor wallets (eligible to be set as auditors for networks)
 */
contract IdentityManager is AccessManager {
    uint64 public constant COMPANY_ROLE = 1;
    uint64 public constant AUDITOR_ROLE = 2;

    constructor(address admin) AccessManager(admin) {}
}