<script setup lang="js">
import Node from "@/components/organigram/Node.vue";
import { computed, ref, onMounted, nextTick } from "vue";
import { useRoute } from "vue-router";
import NetworkController from "@/components/organigram/NetworkController.vue";
import DeleteNodeModal from "@/components/modal/DeleteNodeModal.vue";
import { useNetworksStore } from "@/stores/networks";
import { useCompanyStore } from "@/stores/company";

const props = defineProps({
});

const emit = defineEmits(['add-node']);

const route = useRoute();
const networksStore = useNetworksStore();
const companyStore = useCompanyStore();
const orgDropdownOptions = computed(() => networksStore.orgDropdownOptions);

const tree = computed(() => networksStore.currentNetworkTree)

const isAdmin = computed(() => networksStore.networkDetails?.amIAdmin)

const loading = ref(true);
const wrapperRef = ref(null);


console.log('orgDropdownOptions ', orgDropdownOptions);

const addRootNode = () => {
  networksStore.initTree(companyStore.company.id, companyStore.company.companyName);
  networksStore.consumeCompanyId(companyStore.company.id)
}

onMounted(async () => {
  await networksStore.loadOrgCompanies();
  loading.value = true;

  const networkId = route.params.networkId;
  if (networkId) {
    const networkDetails = await networksStore.fetchNetworkDetails(networkId);
    const treeData = networkDetails?.amIAdmin
      ? await networksStore.fetchNetworkTreeAdmin(networkId)
      : await networksStore.fetchNetworkTreeParticipant(networkId);
    networksStore.populateNetworkTree(treeData);
    await nextTick();
    centerRoot();
  } else {
    addRootNode();   
    await nextTick()    
    centerRoot();
  }

  loading.value = true;
})

function convertToCompanyTree(node) {
  if (!node) return null;

  const res = {
    companyId: node.companyId ?? null,
    companyName: node.companyName ?? null,
    children: []
  };

  // traverse children array
  for (let child of node.children || []) {
    res.children.push(convertToCompanyTree(child));
  }

  return res;
}

function getConvertedTree() {
  return convertToCompanyTree(networksStore.currentNetworkTree);
}

const handleMove = (direction) => {
  const el = wrapperRef.value;
  if (!el) return;
  const scrollAmount = 100;
  if (direction === 'up') el.scrollTop -= scrollAmount;
  else if (direction === 'down') el.scrollTop += scrollAmount;
  else if (direction === 'left') el.scrollLeft -= scrollAmount;
  else if (direction === 'right') el.scrollLeft += scrollAmount;
}

const handleAddSupplier = () => {
  if (networksStore.selectedNode) {
    const selectedNode = networksStore.currentNetworkMap[networksStore.selectedNode];
    if (selectedNode) {
      const companyInitialValue = networksStore.orgDropdownOptions[0];
      networksStore.addNode(companyInitialValue.value, companyInitialValue.label, selectedNode.companyId);
    }
  }
}

const handleDelete = () => {
  networksStore.isDeleteModalOpen = true
}

const handleConfirm = async () => {
  const networkId = route.params.networkId;
  if (networkId) {
    await networksStore.updateNetworkTree(networkId);
    // Optionally refetch the tree to sync with server
    const treeData = await networksStore.fetchNetworkTreeAdmin(networkId);
    networksStore.populateNetworkTree(treeData);
    await nextTick(); 
    centerRoot();
  }
}

const centerRoot = () => {  const el = wrapperRef.value;
    if (!el) return;
    el.scrollLeft = Math.max(0, (el.scrollWidth - el.clientWidth) / 2);
};

defineExpose({ getConvertedTree });
</script>

<template>
  <div ref="wrapperRef" class="organigram-wrapper">
    <div class="organigram__container organigram__container--root">
      <div class="organigram__children__wrapper">
        <div v-if="tree" class="organigram__children">
          <Node :companyId="tree.companyId" root/>
        </div>
      </div>
    </div>
    <NetworkController v-if="networksStore.editMode"
      @move="handleMove"
      @add-supplier="handleAddSupplier"
      @delete="handleDelete"
      @confirm="handleConfirm"
    />
  </div>

  <DeleteNodeModal/>

</template>

<style scoped>
@import "@/styles/organigram.css";




</style>
