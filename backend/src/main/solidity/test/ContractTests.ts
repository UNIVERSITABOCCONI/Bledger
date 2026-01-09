import { loadFixture } from "@nomicfoundation/hardhat-toolbox/network-helpers";
import { expect } from "chai";
import hre from "hardhat";

describe("BLedgerNetwork Application Flow (mint with auditors + batch set)", function () {
  async function deployContracts() {
    const [owner, company1, company2, auditor1, auditor2, stranger] = await hre.ethers.getSigners();

    // Deploy IdentityManager with owner as ADMIN
    const IdentityManager = await hre.ethers.getContractFactory("IdentityManager");
    const identityManager = await IdentityManager.deploy(owner.address);

    // Deploy BLedgerNetwork with authority = IdentityManager
    const BLedgerNetwork = await hre.ethers.getContractFactory("BLedgerNetwork");
    const network = await BLedgerNetwork.deploy(
      await identityManager.getAddress(),
      "BLedger Network",
      "BLN"
    );

    // Roles
    const ADMIN_ROLE   = await identityManager.ADMIN_ROLE();
    const COMPANY_ROLE = await identityManager.COMPANY_ROLE();
    const AUDITOR_ROLE = await identityManager.AUDITOR_ROLE();

    // Grant roles
    await identityManager.grantRole(COMPANY_ROLE, company1.address, 0);
    await identityManager.grantRole(COMPANY_ROLE, company2.address, 0);
    await identityManager.grantRole(AUDITOR_ROLE, auditor1.address, 0);
    await identityManager.grantRole(AUDITOR_ROLE, auditor2.address, 0);

    // Map restricted functions to ADMIN_ROLE (including new ones)
    const i = network.interface;
    const selectors = [
      i.getFunction("safeMint").selector,
      i.getFunction("safeMintWithAuditors").selector,
      i.getFunction("batchSafeMint").selector,
      i.getFunction("batchSafeMintWithAuditors").selector,
      i.getFunction("setAuditors").selector
    ];

    await identityManager.setTargetFunctionRole(
      await network.getAddress(),
      selectors,
      ADMIN_ROLE
    );

    return {
      identityManager, network,
      owner, company1, company2, auditor1, auditor2, stranger,
      ADMIN_ROLE, COMPANY_ROLE, AUDITOR_ROLE
    };
  }

  // Helper: random URL + contentHash
  function randomUrlAndHash(prefix = "https://backend.local/json/") {
    const entropy = hre.ethers.hexlify(hre.ethers.randomBytes(16)).replace("0x", "");
    const url = `${prefix}${entropy}`;
    const json = `{"demo":true,"nonce":"${entropy}"}`;
    const contentHash = hre.ethers.keccak256(hre.ethers.toUtf8Bytes(json));
    return { url, contentHash };
  }

  describe("Mint with auditors at creation", function () {
    it("admin mints and assigns multiple auditors in one tx", async function () {
      const { network, owner, company1, auditor1, auditor2 } = await loadFixture(deployContracts);

      const { url, contentHash } = randomUrlAndHash();
      const auditors = [auditor1.address, auditor2.address];

      const tx = await network.connect(owner).safeMintWithAuditors(company1.address, url, contentHash, auditors);
      const receipt = await tx.wait();
      console.log("Gas used: safeMintWithAuditors(2 auditors) ->", receipt.gasUsed.toString());

      expect(await network.ownerOf(0)).to.equal(company1.address);
      expect(await network.isAuditor(0, auditor1.address)).to.equal(true);
      expect(await network.isAuditor(0, auditor2.address)).to.equal(true);
    });

    it("batchSafeMintWithAuditors mints N tokens with per-token auditors", async function () {
      const { network, owner, company1, company2, auditor1, auditor2 } = await loadFixture(deployContracts);

      const tos = [company1.address, company2.address];
      const urls: string[] = [];
      const hashes: string[] = [];
      const auditorsList: string[][] = [
        [auditor1.address],                 // token 0 auditors
        [auditor1.address, auditor2.address]// token 1 auditors
      ];

      for (let i = 0; i < tos.length; i++) {
        const { url, contentHash } = randomUrlAndHash();
        urls.push(url);
        hashes.push(contentHash);
      }

      const tx = await network.connect(owner).batchSafeMintWithAuditors(tos, urls, hashes, auditorsList);
      const receipt = await tx.wait();
      console.log(`Gas used: batchSafeMintWithAuditors(${tos.length} tokens) ->`, receipt.gasUsed.toString());

      expect(await network.ownerOf(0)).to.equal(company1.address);
      expect(await network.ownerOf(1)).to.equal(company2.address);
      expect(await network.isAuditor(0, auditor1.address)).to.equal(true);
      expect(await network.isAuditor(1, auditor1.address)).to.equal(true);
      expect(await network.isAuditor(1, auditor2.address)).to.equal(true);
    });
  });

  describe("Batch setAuditors, updates and reverts", function () {
    it("admin can add/remove multiple auditors in one call; auditors can update URI", async function () {
      const { network, owner, company1, auditor1, auditor2 } = await loadFixture(deployContracts);

      // Mint token 0 without auditors
      const a = randomUrlAndHash();
      await (await network.connect(owner).safeMint(company1.address, a.url, a.contentHash)).wait();

      // Add two auditors in batch
      const addTx = await network.connect(owner).setAuditors(0, [auditor1.address, auditor2.address], true);
      const addRcpt = await addTx.wait();
      console.log("Gas used: setAuditors(add 2) ->", addRcpt.gasUsed.toString());

      expect(await network.isAuditor(0, auditor1.address)).to.equal(true);
      expect(await network.isAuditor(0, auditor2.address)).to.equal(true);

      // Auditor1 updates
      const b = randomUrlAndHash();
      const up1 = await network.connect(auditor1).updateTokenURI(0, b.url, b.contentHash, 2);
      console.log("Gas used: updateTokenURI by auditor1 ->", (await up1.wait()).gasUsed.toString());

      // Remove auditor2, keep auditor1
      const remTx = await network.connect(owner).setAuditors(0, [auditor2.address], false);
      const remRcpt = await remTx.wait();
      console.log("Gas used: setAuditors(remove 1) ->", remRcpt.gasUsed.toString());

      expect(await network.isAuditor(0, auditor2.address)).to.equal(false);

      // auditor2 now cannot update
      const c = randomUrlAndHash();
      await expect(network.connect(auditor2).updateTokenURI(0, c.url, c.contentHash, 2))
        .to.be.revertedWithCustomError(network, "NotAuthorized");
    });

    it("owner (company) can update; non-authorized user reverts", async function () {
      const { network, owner, company1, stranger } = await loadFixture(deployContracts);

      const a = randomUrlAndHash();
      await (await network.connect(owner).safeMint(company1.address, a.url, a.contentHash)).wait();

      const b = randomUrlAndHash();
      const up = await network.connect(company1).updateTokenURI(0, b.url, b.contentHash, 1);
      console.log("Gas used: updateTokenURI by owner ->", (await up.wait()).gasUsed.toString());

      const c = randomUrlAndHash();
      await expect(network.connect(stranger).updateTokenURI(0, c.url, c.contentHash, 0))
        .to.be.revertedWithCustomError(network, "NotAuthorized");
    });
  });
});
