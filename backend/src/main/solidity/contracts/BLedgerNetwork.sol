// SPDX-License-Identifier: MIT
pragma solidity ^0.8.22;

import {AccessManaged} from "@openzeppelin/contracts/access/manager/AccessManaged.sol";
import {IAccessManager} from "@openzeppelin/contracts/access/manager/IAccessManager.sol";
import {ERC721} from "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import {ERC721Pausable} from "@openzeppelin/contracts/token/ERC721/extensions/ERC721Pausable.sol";


contract BLedgerNetwork is ERC721, ERC721Pausable, AccessManaged {
    // Roles (must match IdentityManager)
    uint64 public constant ADMIN_ROLE   = type(uint64).min;
    uint64 public constant COMPANY_ROLE = 1;
    uint64 public constant AUDITOR_ROLE = 2;

    // Token state
    uint256 private _nextTokenId;
    mapping(uint256 => string)  private _tokenUrl;
    mapping(uint256 => bytes32) private _tokenContentHash;
    mapping(uint256 => mapping(address => bool)) private _auditorOf; // auditors per tokenId

    // Events
    enum UpdateReason { Generic, Upload, Audit, RefusedInvitation }

    event NetworkCreated(uint256 indexed tokenId, address indexed to, string uri, bytes32 contentHash);
    event NetworkUpdated(uint256 indexed tokenId, address indexed updater, string newUri, bytes32 contentHash, UpdateReason reason);
    event AuditorSet(uint256 indexed tokenId, address indexed auditor, bool active);
    event MetadataUpdate(uint256 _tokenId); // EIP-4906

    // Errors
    error NonexistentToken();
    error EmptyBatch();
    error MismatchedArrayLengths();
    error ReceiverMustBeUser();
    error SenderMustBeAdmin();
    error EmptyUrl();
    error NotAuthorized();

    constructor(address initialAuthority, string memory name, string memory symbol)
    ERC721(name, symbol)
    AccessManaged(initialAuthority)
    {}

    // ---------------- Admin / restricted ----------------

    /// Mint a new token directly to the company wallet.
    function safeMint(address to, string calldata uri, bytes32 contentHash) external restricted {
        if (bytes(uri).length == 0) revert EmptyUrl();
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);
        _setTokenData(tokenId, uri, contentHash);
        emit NetworkCreated(tokenId, to, uri, contentHash);
        emit MetadataUpdate(tokenId);
    }

    /// Mint a new token and set multiple auditors at creation time.
    function safeMintWithAuditors(
        address to,
        string calldata uri,
        bytes32 contentHash,
        address[] calldata auditors
    ) external restricted {
        if (bytes(uri).length == 0) revert EmptyUrl();
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);
        _setTokenData(tokenId, uri, contentHash);
        // set auditors in batch
        _setAuditorsBatch(tokenId, auditors, true);
        emit NetworkCreated(tokenId, to, uri, contentHash);
        emit MetadataUpdate(tokenId);
    }

    /// Batch mint multiple tokens.
    function batchSafeMint(
        address[] calldata to,
        string[] calldata uris,
        bytes32[] calldata contentHashes
    ) external restricted {
        uint256 length = uris.length;
        if (length == 0) revert EmptyBatch();
        if (to.length != length || contentHashes.length != length) revert MismatchedArrayLengths();

        uint256 startTokenId = _nextTokenId;
        unchecked {
            for (uint256 i = 0; i < length; i++) {
                if (bytes(uris[i]).length == 0) revert EmptyUrl();
                uint256 tokenId = startTokenId + i;
                _mint(to[i], tokenId);
                _setTokenData(tokenId, uris[i], contentHashes[i]);
                emit NetworkCreated(tokenId, to[i], uris[i], contentHashes[i]);
                emit MetadataUpdate(tokenId);
            }
            _nextTokenId = startTokenId + length;
        }
    }

    /// Batch mint with auditors list per token.
    /// auditorsList[k] applies to tokenId = startTokenId + k
    function batchSafeMintWithAuditors(
        address[] calldata to,
        string[] calldata uris,
        bytes32[] calldata contentHashes,
        address[][] calldata auditorsList
    ) external restricted {
        uint256 length = uris.length;
        if (length == 0) revert EmptyBatch();
        if (to.length != length || contentHashes.length != length || auditorsList.length != length)
            revert MismatchedArrayLengths();

        uint256 startTokenId = _nextTokenId;
        unchecked {
            for (uint256 i = 0; i < length; i++) {
                if (bytes(uris[i]).length == 0) revert EmptyUrl();
                uint256 tokenId = startTokenId + i;
                _mint(to[i], tokenId);
                _setTokenData(tokenId, uris[i], contentHashes[i]);
                _setAuditorsBatch(tokenId, auditorsList[i], true);
                emit NetworkCreated(tokenId, to[i], uris[i], contentHashes[i]);
                emit MetadataUpdate(tokenId);
            }
            _nextTokenId = startTokenId + length;
        }
    }

    /// Batch add/remove auditors for a given token.
    function setAuditors(uint256 tokenId, address[] calldata auditors, bool active) external {
        _requireMinted(tokenId);
        _canAddAuditor(tokenId, _msgSender());
        _setAuditorsBatch(tokenId, auditors, active);
    }

    // --------------- Update URI (owner/auditor/admin) ----------------

    function updateTokenURI(uint256 tokenId, string calldata newUri, bytes32 contentHash, UpdateReason reason)
    external
    {
        _requireMinted(tokenId);
        if (bytes(newUri).length == 0) revert EmptyUrl();
        if (!_canUpdate(tokenId, _msgSender())) revert NotAuthorized();

        _setTokenData(tokenId, newUri, contentHash);
        emit NetworkUpdated(tokenId, _msgSender(), newUri, contentHash, reason);
        emit MetadataUpdate(tokenId);
    }

    // ------------------------ Views ---------------------------------

    function tokenURI(uint256 tokenId) public view override returns (string memory) {
        _requireMinted(tokenId);
        return _tokenUrl[tokenId];
    }

    function tokenContentHash(uint256 tokenId) external view returns (bytes32) {
        _requireMinted(tokenId);
        return _tokenContentHash[tokenId];
    }

    function isAuditor(uint256 tokenId, address who) external view returns (bool) {
        return _auditorOf[tokenId][who];
    }

    // ------------------ Internals / helpers -------------------------

    function _setTokenData(uint256 tokenId, string memory url, bytes32 contentHash) internal {
        _tokenUrl[tokenId] = url;
        _tokenContentHash[tokenId] = contentHash;
    }

    function _setAuditorsBatch(uint256 tokenId, address[] calldata auditors, bool active) internal {
        // Optional hardening: ensure each address has AUDITOR_ROLE globally
        IAccessManager mgr = IAccessManager(authority());
        for (uint256 i = 0; i < auditors.length; i++) {
            address a = auditors[i];
            (bool isMember, uint32 delay) = mgr.hasRole(AUDITOR_ROLE, a);
            require(isMember && delay == 0, "Auditor lacks global role");
            _auditorOf[tokenId][a] = active;
            emit AuditorSet(tokenId, a, active);
        }
    }

    function _requireMinted(uint256 tokenId) internal view {
        if (_ownerOf(tokenId) == address(0)) revert NonexistentToken();
    }

    function _isSenderAdmin() internal view returns (bool) {
        (bool isMember, uint32 executionDelay) = IAccessManager(authority()).hasRole(ADMIN_ROLE, _msgSender());
        return isMember && executionDelay == 0;
    }

    function _canUpdate(uint256 tokenId, address sender) internal view returns (bool) {
        if (_isSenderAdmin()) return true;
        if (ownerOf(tokenId) == sender) return true;
        if (_auditorOf[tokenId][sender]) return true;
        return false;
    }

    function _canAddAuditor(uint256 tokenId, address sender) internal view returns (bool) {
        if (_isSenderAdmin()) return true;
        if (ownerOf(tokenId) == sender) return true;
        return false;
    }

    /**
     * Transfer rules:
     * - If sender is not admin:
     *    - recipient must have COMPANY_ROLE (for mint/transfer).
     *    - if not mint (from != 0), only admin can transfer.
     */
    function _update(address to, uint256 tokenId, address auth)
    internal
    override(ERC721, ERC721Pausable)
    returns (address)
    {
        address from = _ownerOf(tokenId);
        bool isSenderAdmin = _isSenderAdmin();

        if (!isSenderAdmin) {
            if (to != address(0)) {
                (bool isMember, uint32 delay) = IAccessManager(authority()).hasRole(COMPANY_ROLE, to);
                if (!(isMember && delay == 0)) revert ReceiverMustBeUser();
            }
            if (from != address(0)) {
                (bool isMemberFrom, uint32 delayFrom) = IAccessManager(authority()).hasRole(ADMIN_ROLE, from);
                if (!(isMemberFrom && delayFrom == 0)) revert SenderMustBeAdmin();
            }
        }
        return super._update(to, tokenId, auth);
    }

    function supportsInterface(bytes4 interfaceId)
    public
    view
    override(ERC721)
    returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }
}
