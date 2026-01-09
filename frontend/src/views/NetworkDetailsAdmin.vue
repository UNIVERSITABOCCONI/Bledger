<template>
  <section class="container page-wrapper">
    <!-- Network Details View -->
    <div v-if="!showDiagramView" class="grid gap-4">
      <div :class="isTpa ? 'col-span-16' : 'col-span-10'">
        <Panel>
            <ActionBar class="top-action-bar" style="display:flex">
              <Button variant="link" icon icon-position="left" @click="handleAllNetworks">
                <template #icon>
                  <IconArrowLeft />
                </template>
                All networks
              </Button>
              <template #right-side>
                <div class="top-right-content">
                  <span class="role-text">{{isTpa ? 'AUDITOR' : (networkDetails?.amIAdmin ? 'ADMIN' : 'PARTICIPANT')}} {{(isTpa ? '' : "Level "+  networkDetails?.myNode?.nodeDepth )}}</span>
                  <HamburgerMenu>
                    <Button v-if="isAdmin" variant="link" icon icon-position="left" @click="openAddAuditorsModal">
                      <template #icon>
                        <IconAdd />
                      </template>
                      Add auditor
                    </Button>
 
                    <Button variant="secondary" icon iconPosition="right" @click="handleDiagramClick">
                      Diagram
                      <template #icon>
                        <IconDiagram />
                      </template>
                    </Button>
 
                    <Button v-if="isAdmin" variant="secondary" icon iconPosition="right" @click="handleEditClick">
                      Edit
                      <template #icon>
                        <IconEdit />
                      </template>
                    </Button>
                  </HamburgerMenu>
                </div>
              </template>
            </ActionBar>
            <Title variant="h1" weight="regular" color="blue">
              {{ networkDetails?.networkName || 'Network Details' }}
              <IconPolygon class="network-polygon-icon" @click="openTransactionsModal" />
            </Title>
            <ActionBar v-if="!isTpa" class="desktop-action-bar">
              <Button variant="link" v-if="isAdmin" icon icon-position="left" @click="openAddAuditorsModal">
                <template #icon>
                  <IconAdd />
                </template>
                Add auditor
              </Button>
              <template #right-side>
                <Button variant="secondary" icon iconPosition="right" @click="handleDiagramClick">
                  Diagram
                  <template #icon>
                    <IconDiagram />
                  </template>
                </Button>
 
                <Button variant="secondary" v-if="isAdmin" icon iconPosition="right" @click="handleEditClick">
                  Edit
                  <template #icon>
                    <IconEdit />
                  </template>
                </Button>
              </template>
            </ActionBar>
          <template v-if="isTpa">
            <TpaRequestsTable :network-id="networkId" @review="reviewFiles" />
            <TpaOtherMembersTable :network-id="networkId" />
          </template>
          <template v-else>
            <ClientsTable :network-id="networkId" />
            <SuppliersTable :network-id="networkId" />
            <OtherMembersTable v-if="isAdmin" :network-id="networkId" />
            <RefusedInvitationsTable v-if="isAdmin" :network-id="networkId" />
          </template>

         </Panel>
      </div>
      <div v-if="!isTpa" class="col-span-6">
        <ComputeScope3Panel :network-id="networkDetails?.id" :progress-percentage="networkDetails?.progressEcomputation" :my-node="networkDetails?.myNode" />
        <MyScopePanel :myNode="networkDetails?.myNode" :networkId="networkId" @auditRequested="handleAuditRequested" @download-all="handleDownloadAll" />
        <ExportSuppliersPanel :network-id="networkDetails?.id" :progressPercentage="networkDetails?.progressPercentage"
          :lastVersionDate="networkDetails?.lastVersionDate" />


      </div>
    </div>

    <!-- Diagram View -->
    <Panel v-else class="panel-page panel-organigram">
      <ActionBar>
        <Button variant="link" icon icon-position="left" @click="handleBackToNetworkDetails">
          <template #icon>
            <IconArrowLeft />
          </template>
          Back to Network Details
        </Button>
        <div v-if="isAdmin" class="download-button">
          <Tooltip position="left" content="Exports the tree structure in JSON for Merkle hash verification using the endpoint /api/v1/public/network/merkle-hash">
            <IconInfo />
          </Tooltip>
          <Button variant="secondary" @click="downloadJson" icon iconPosition="left">
            <template #icon>
              <IconDownload />
            </template>
            JSON
          </Button>
        </div>
        <template #right-side v-if="updateMode">
          <Button variant="primary" @click="showConfirmModal = true">Confirm</Button>
        </template>

      </ActionBar>

      <Organigram ref="organigramRef" />
    </Panel>
  </section>

  <!-- Network Transactions Modal -->
  <NetworkTransactionsModal :network-id="networkId" v-model:open="showTransactionsModal" />

  <!-- Add Auditors Modal -->
  <AddAuditorsModal v-if="!isTpa" :network-id="networkId" v-model:open="showAddAuditorsModal" />

  <!-- Review Files Modal -->
  <ReviewFilesModal  v-else :node-id="selectedNodeId" v-model:open="showReviewFilesModal" @verified="handleVerified" />

  <!-- Confirm Operation Modal -->
  <ConfirmOperationModal v-model:open="showConfirmModal" title="Confirm Network Update" message="Are you sure you want to update the network?" @confirm="handleConfirmUpdate" />
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNetworksStore } from '@/stores/networks'
import Panel from "@/components/panel/Panel.vue";
import ActionBar from "@/components/actionBar/ActionBar.vue";
import Button from "@/components/button/Button.vue";
import IconArrowLeft from "@/components/icons/IconArrowLeft.vue";
import Title from "@/components/title/Title.vue";
import IconDiagram from "@/components/icons/IconDiagram.vue";
import IconEdit from "@/components/icons/IconEdit.vue";
import IconPolygon from "@/components/icons/IconPolygon.vue";
import ClientsTable from "@/components/table/ClientsTable.vue";
import SuppliersTable from "@/components/table/SuppliersTable.vue";
import OtherMembersTable from "@/components/table/OtherMembersTable.vue";
import RefusedInvitationsTable from "@/components/table/RefusedInvitationsTable.vue";
import TpaRequestsTable from "@/components/table/TpaRequestsTable.vue";
import TpaOtherMembersTable from "@/components/table/TpaOtherMembersTable.vue";
import IconAdd from "@/components/icons/IconAdd.vue";
import MyScopePanel from "@/components/MyScopePanel.vue";
import ExportSuppliersPanel from "@/components/ExportSuppliersPanel.vue";
import ComputeScope3Panel from "@/components/ComputeScope3Panel.vue";
import NetworkTransactionsModal from "@/components/NetworkTransactionsModal.vue";
import AddAuditorsModal from "@/components/AddAuditorsModal.vue";
import Organigram from "@/components/organigram/Organigram.vue";
import ReviewFilesModal from "@/components/modal/ReviewFilesModal.vue";
import ConfirmOperationModal from "@/components/ConfirmOperationModal.vue";
import HamburgerMenu from "@/components/hamburger/HamburgerMenu.vue";
import IconDownload from "@/components/icons/IconDownload.vue";
import IconInfo from "@/components/icons/IconInfo.vue";
import Tooltip from "@/components/tooltip/Tooltip.vue";
import { useCompanyStore } from '@/stores/company';
import { useFileStore } from '@/stores/fileStore';

const route = useRoute()
const router = useRouter()
const networksStore = useNetworksStore()
const companyStore = useCompanyStore()
const fileStore = useFileStore()

const networkId = computed(() => route.params.networkId as string)
const networkDetails = computed(() => networksStore.networkDetails)
const isAdmin = computed(() => networkDetails.value?.amIAdmin ?? false)
const isTpa = computed(() => companyStore.company?.companyType === 'TPA')
const updateMode = computed(() => networksStore.updateMode)

const showTransactionsModal = ref(false)
const showAddAuditorsModal = ref(false)
const showDiagramView = ref(false)
const networkTree = ref(null)
const showReviewFilesModal = ref(false)
const selectedNodeId = ref('')
const showConfirmModal = ref(false)
const organigramRef = ref(null)

function handleAllNetworks() {
  router.push({ name: 'networks' })
}

function openTransactionsModal() {
  showTransactionsModal.value = true
}

function openAddAuditorsModal() {
  showAddAuditorsModal.value = true
}

async function handleDiagramClick() {
  showDiagramView.value = true
}

async function handleEditClick() {
  networksStore.editMode = true
  networksStore.updateMode = true
  showDiagramView.value = true
}

function handleBackToNetworkDetails() {
  showDiagramView.value = false
  networkTree.value = null
}

function reviewFiles(requestId) {
  selectedNodeId.value = requestId
  showReviewFilesModal.value = true
}


onMounted(async () => {
  if (networkId.value) {
    networksStore.editMode = false
    await networksStore.fetchNetworkDetails(networkId.value, isTpa.value)
    if (isTpa.value) {
      await Promise.all([
        networksStore.fetchTpaRequests(networkId.value),
        networksStore.fetchTpaOtherMembers(networkId.value),
      ])
    } else {
      await Promise.all([
        networksStore.fetchClients(networkId.value),
        networksStore.fetchSuppliers(networkId.value),
        networksStore.fetchAuditors(networkId.value),
      ])
      if (isAdmin.value) {
        await Promise.all([
          networksStore.fetchOtherMembers(networkId.value),
          networksStore.fetchRefused(networkId.value),
        ])
      }
    }
  }
})

async function handleAuditRequested() {
  if (networkId.value) {
    await networksStore.fetchNetworkDetails(networkId.value)
  }
}

async function handleDownloadAll() {
  try {
    const nodeId = networkDetails.value?.myNode?.id
    const companyName = companyStore.company?.companyName
    if (nodeId && companyName) {
      await fileStore.downloadFile(nodeId, companyName)
    } else {
      console.error('Node ID or company name not available')
    }
  } catch (error) {
    console.error('Error downloading all files:', error)
  }
}

async function handleVerified() {
  if (networkId.value && isTpa.value) {
    await networksStore.fetchTpaRequests(networkId.value)
  }
}

const handleConfirmUpdate = async () => {
  const networkId = route.params.networkId;
  if (networkId) {
    await networksStore.updateNetworkTree(networkId);
    // Optionally refetch the tree to sync with server
    const treeData = await networksStore.fetchNetworkTreeAdmin(networkId);
    networksStore.populateNetworkTree(treeData);

    await Promise.all([
        networksStore.fetchOtherMembers(networkId),
        networksStore.fetchRefused(networkId),
        networksStore.fetchClients(networkId),
        networksStore.fetchSuppliers(networkId),
    ])
  }
}

const downloadJson = () => {
  if (organigramRef.value) {
    organigramRef.value.getConvertedTree();
    const data = organigramRef.value.getConvertedTree();
    const json = JSON.stringify(data, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'organigram.json';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}


</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding: 1rem;
}

.pagination span {
  font-weight: 500;
}

.network-polygon-icon {
  display: inline-block;
  margin-left: 8px;
  cursor: pointer;
  width: 20px;
  height: 20px;
  color: #6C00F6;
}

.network-polygon-icon:hover {
  opacity: 0.8;
}

.top-right-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  align-self: flex-end;
}

.role-text {
  display: inline;
}

.desktop-btn {
  display: flex;
}

@media (max-width: 768px) {
  .role-text {
    display: none;
  }

  .desktop-btn {
    display: none;
  }

  .desktop-action-bar {
    display: none;
  }

  .top-action-bar {
    flex-direction: row;
  }
}


.download-button {
  display: flex;
  z-index: 10;
  color: var(--Blue-2, #0046AE);
}
</style>