<template>
  <Modal
    v-model:open="isModalOpen"
    variant="simple"
    modal-title="Update Supplier Values"
    cta-confirm-label="Update"
    cta-close-label="Cancel"
    button-variant="primary"
    @confirm="onConfirm"
    @close="onClose"
  >
    <p>Update quantity and transportation emission values for <strong>{{ supplier?.companyName }}</strong></p>

    <div class="form-group">
      <div style="display:flex; justify-content: flex-start; gap: 5px">
        <label for="quantity">Quantity (q)</label>
        <Tooltip position="right" content="Quantity of materials supplied by this supplier needed to produce one unit of own product">
          <IconInfo />
        </Tooltip>
      </div>
      <input
        id="quantity"
        type="number"
        step="0.01"
        v-model="quantity"
        class="input-field"
        placeholder="Enter quantity"
      />
    </div>

    <div class="form-group">
      <label for="transportationEmission">Transportation Emission (t) <span class="unit">the entered value should be in tCO₂e</span></label>
      <input
        id="transportationEmission"
        type="number"
        step="0.01"
        v-model="transportationEmission"
        class="input-field"
        placeholder="Enter transportation emission"
      />
    </div>
  </Modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useNetworksStore } from '@/stores/networks'
import Modal from '@/components/modal/Modal.vue'
import Tooltip from './tooltip/Tooltip.vue';
import IconInfo from './icons/IconInfo.vue';

const props = defineProps<{
  networkId: string
  supplier: any
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'updated': []
}>()

const networksStore = useNetworksStore()

const isModalOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const quantity = ref('')
const transportationEmission = ref('')

watch(() => props.open, (newVal) => {
  if (newVal && props.supplier) {
    quantity.value = props.supplier.quantity?.toString() || ''
    transportationEmission.value = props.supplier.transportationEmission?.toString() || ''
  }
})

const onConfirm = async () => {
  if (!props.supplier || !quantity.value || !transportationEmission.value) return

  try {
    await networksStore.updateNode(props.supplier.id, {
      quantity: parseFloat(quantity.value),
      transportEmissions: parseFloat(transportationEmission.value)
    })
    networksStore.fetchNetworkDetails(networksStore.networkDetails.id);
    emit('updated')
    onClose()
  } catch (error) {
    console.error('Error updating supplier values:', error)
  }
}

const onClose = () => {
  isModalOpen.value = false
  quantity.value = ''
  transportationEmission.value = ''
}
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.input-field {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.input-field:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.unit {
  font-size: 0.9rem;
  color: #666;
  font-weight: normal;
}
</style>