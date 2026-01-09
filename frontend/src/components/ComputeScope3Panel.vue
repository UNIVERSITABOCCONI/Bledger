<template>
  <Panel title="Compute your Scope 3">
    <p>Use your network data to compute Emission Intensity and Scope 3 emissions.</p>

    <!-- Status / notification -->
    <div
      class="notification"
      v-if="showUpdateNotice"
      aria-live="polite"
      role="status"
    >
      <IconAlert class="notification-icon" />
      <p class="notification-text">
        Some of your suppliers' data changed — you may need to recompute your Emission Intensity.
      </p>
    </div>


    <!-- Progress -->
    <ProgressBar :progress="progressPct" :copy="progressMessage" />


    <!-- Step 1 -->
    <section class="step">
      <header class="step-header">
        <span class="step-number">1</span>
        <h3>Compute Emission Intensity</h3>
      </header>

      <div class="step-body">
        <p class="muted">
          Make sure you’ve set the quantity and transportation emissions for all of your suppliers
        </p>

        <div class="cta-row">
          <Button
            :disabled="computeEDisabled"
            variant="primary"
            @click="handleComputeE"
            icon
            iconPosition="left"
          >
            Compute Intensity
            <template #icon>
              <IconCompute />
            </template>
          </Button>

          <div v-if="hasE" class="metric" aria-live="polite" role="status">
            <div class="metric-label">Emission Intensity</div>
            <div class="metric-value">{{ formatNumber(myNode?.eValue) }} tCO₂e</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Step 2 -->
    <section class="step" v-if="hasE">
      <header class="step-header">
        <span class="step-number">2</span>
        <h3>Enter Production Volume</h3>
      </header>

      <div class="step-body">
        <InputText
          :disabled="computing"
          v-model="productionVolume"
          label="Production Volume"
          placeholder="e.g., 10"
          inputmode="decimal"
        />

        <div class="cta-row">
          <Button
            variant="primary"
            @click="handleComputeScope3"
            :disabled="computeScope3Disabled"
            icon
            iconPosition="left"
          >
            Compute Scope 3
            <template #icon>
              <IconCompute />
            </template>
          </Button>

          <div v-if="scope3Value != null" class="metric" aria-live="polite" role="status">
            <div class="metric-label">Scope 3 Emissions</div>
            <div class="metric-value">{{ formatNumber(scope3Value) }} tCO₂e</div>
          </div>
        </div>
      </div>
    </section>
  </Panel>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Panel from "@/components/panel/Panel.vue";
import ProgressBar from "@/components/progressBar/ProgressBar.vue";
import Button from "@/components/button/Button.vue";
import InputText from "@/components/forms/InputText.vue";
import { useNetworksStore } from '@/stores/networks'
import IconAlert from './icons/IconAlert.vue';
import IconCompute from './icons/IconCompute.vue';

const props = defineProps<{
  networkId: string;
  progressPercentage?: number;
}>();

const networksStore = useNetworksStore()
const productionVolume = ref<string>('') // keep as string for v-model
const computing = ref(false)

// ----- Data access -----
const nodeDetails = computed(() => networksStore.networkDetails)
const myNode = computed(() => nodeDetails.value?.myNode)
const filePresent = computed(() => Boolean(myNode.value?.scopeFileId))

// ----- Progress & messaging -----
const progressPct = computed(() => props.progressPercentage ?? 0)

const progressMessage = computed(() => {
  if (progressPct.value < 100) {
    const hasSuppliers = networksStore.suppliers.total > 0;

    let message = "";

    if (hasSuppliers) {
      message += "Some suppliers have not yet calculated their Emission Intensity or are missing quantity or transport emissions. ";
    }

    if (!filePresent.value) {
      message += hasSuppliers
        ? "Your Scope documents are also required for completion."
        : "Your Scope documents are required for completion.";
    }

    return message || "Progress is incomplete.";
  } else {
    return "Emission Intensity is ready to be computed.";
  }
});




// ----- Notification logic -----
const lastVersionTimestamp = computed(() => { 
    return nodeDetails.value?.lastVersionCompute
    ? new Date(nodeDetails.value.lastVersionCompute).getTime()
    : null
})

const lastExportTimestamp = computed(() => {
  return nodeDetails.value?.lastCompute
    ? new Date(nodeDetails.value.lastCompute).getTime()
    : null
})

const showUpdateNotice = computed(() =>
  Boolean(
    lastVersionTimestamp.value &&
    lastExportTimestamp.value &&
    myNode.value?.eValue &&
    lastVersionTimestamp.value > lastExportTimestamp.value &&
    progressPct.value === 100
  )
)

// ----- UI state -----
const hasE = computed(() => myNode.value?.eValue != null)
const scope3Value = computed(() => myNode.value?.scope3 ?? null)

const productionVolumeNumber = computed(() => {
  const n = parseFloat((productionVolume.value || '').toString())
  return Number.isFinite(n) && n > 0 ? n : null
})

const computeEDisabled = computed(() =>
  computing.value || !filePresent.value || progressPct.value < 90
)

const computeScope3Disabled = computed(() =>
  computing.value || !hasE.value || productionVolumeNumber.value == null
)

// Prefill production volume if present
watch(
  () => myNode.value?.productionVolume,
  (newVal) => {
    if (newVal != null) productionVolume.value = String(newVal)
  },
  { immediate: true }
)

// ----- Helpers -----
function formatNumber(v?: number | string | null) {
  if (v == null || v === '') return ''
  try {
    return new Intl.NumberFormat().format(Number(v))
  } catch {
    return String(v)
  }
}

// ----- Actions -----
async function handleComputeE() {
  try {
    computing.value = true
    await networksStore.computeE(props.networkId)
    await networksStore.fetchNetworkDetails(props.networkId)
  } catch (error) {
    console.error('Error computing Emission Intensity:', error)
  } finally {
    computing.value = false
  }
}

async function handleComputeScope3() {
  try {
    computing.value = true
    const body = { value: productionVolumeNumber.value ?? 0 }
    await networksStore.computeScope3(props.networkId, body)
    await networksStore.fetchNetworkDetails(props.networkId)
  } catch (error) {
    console.error('Error computing Scope 3:', error)
  } finally {
    computing.value = false
  }
}
</script>

<style scoped>
/* Layout */
.step {
  width: 100%;
  border: 1px solid var(--border, #e6e6e6);
  border-radius: 12px;
  padding: 1rem;
  background: var(--surface, #fff);
}

.step + .step {
  margin-top: 1rem;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.step-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  font-size: 0.9rem;
  font-weight: 600;
  border-radius: 999px;
  background: var(--primary-ghost, #e9f0ff);
  color: var(--primary, #1a56db);
}

.muted {
  margin: 0 0 .75rem 0;
  color: #666;
  font-size: 0.92rem;
}

/* Actions / metrics */
.cta-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.metric {
  padding: .5rem .75rem;
  border-radius: 8px;
  background: var(--metric-bg, #f7f8fa);
  min-width: 220px;
}

.metric-label {
  font-size: 0.8rem;
  color: #6b7280;
}

.metric-value {
  font-size: 1.25rem;
  font-weight: 600;
}

/* Notification */
.notification {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff7e6;
  border: 1px solid #ffecb3;
  padding: .5rem .75rem;
  border-radius: 8px;
}

.notification-icon {
  height: 18px;
  width: 18px;
}

.notification-text {
  font-size: 0.9rem;
  margin: 0;
  margin-bottom: 0.5rem;
}
</style>
