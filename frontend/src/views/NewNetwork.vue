<template>
  <section class="container page-wrapper">
    <div v-if="!showOrganigram" class="grid gap-4">
      <div class="col-span-10 col-start-4">
        <Panel class="panel-page">
          <div class="panel-head">
            <Title variant="h1" weight="regular" color="blue">Network name</Title>
            <span class="step-label">Step <strong>1</strong>/2</span>
          </div>

          <VSpacer size="lg" />

          <div class="section-form-fieldset">
            <div class="section-form">
              <!-- assuming your InputText supports v-model -->
              <InputText v-model="networkName" label="Write the name you want to associate to your new network."
                aria-required="true" placeholder="Network name" required-msg="This field is required!" />
            </div>

            <div class="section-form">
              <Dropdown v-model="governanceModel" label="Select the Governance Model" :options="governanceOptions" />
            </div>

            <div class="section-form">
              <Dropdown v-model="dataModel" label="Select the Data Model" :options="dataOptions" />
            </div>

            <div class="section-form">
              <Dropdown ref="refDropdown2" v-model="dropdown2" label="Select the Auditor for this network"
                note="The auditor will be responsible for validating Scope 1 & 2 data." :options="options2Computed"
                placeholder="Select Auditor" />

              <!-- Selected auditors as badges -->
              <div class="flex gap-2 justify-start flex-wrap">
                <Badge v-for="id in selectedAuditorIds" :key="id" :label="idToName.get(id) ?? id"
                  @remove="removeAuditor(id)" />
              </div>
            </div>
          </div>

          <VSpacer size="lg" />

          <div class="section-form section-form-btn">
            <Button variant="secondary" @click="backToNetworks">Cancel</Button>
            <Button type="submit" class="button" @click="handleSubmit">Next step</Button>
          </div>
        </Panel>
      </div>
    </div>

    <Panel v-else class="panel-page panel-organigram">
      <ActionBar>
        <Button variant="link" icon icon-position="left" @click="showOrganigram = false">
          <template #icon>
            <IconArrowLeft />
          </template>
          All networks
        </Button>
        <template #right-side>
          <Button variant="primary" @click="handleConfirm">Confirm</Button>
        </template>
      </ActionBar>
      <Organigram ref="organigramRef" :edit-mode="true" />
    </Panel>
  </section>

  <ConfirmInvitationsModal
    :open="isConfirmModalOpen"
    @update:open="isConfirmModalOpen = $event"
    @confirm="handleModalConfirm"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Organigram from '@/components/organigram/Organigram.vue'
import Panel from '@/components/panel/Panel.vue'
import ActionBar from '@/components/actionBar/ActionBar.vue'
import IconArrowLeft from '@/components/icons/IconArrowLeft.vue'
import Button from '@/components/button/Button.vue'
import Dropdown from '@/components/forms/Dropdown.vue'
import Title from '@/components/title/Title.vue'
import VSpacer from '@/components/spacer/VSpacer.vue'
import InputText from '@/components/forms/InputText.vue'
import Badge from '@/components/badge/Badge.vue'
import ConfirmInvitationsModal from '@/components/ConfirmInvitationsModal.vue'

import { useNetworksStore } from '@/stores/networks'
import { useCompanyStore } from '@/stores/company'
import { useErrorStore } from '@/stores/errorStore'

import { useRouter } from "vue-router"
import api from '@/utils/api'

type CompanyLite = { companyId: string; companyName: string }
type Option = { label: string; value: string; disabled?: boolean }

const router = useRouter();

const networkStore = useNetworksStore()
const companyStore = useCompanyStore()
const errorStore = useErrorStore()

const showOrganigram = ref(false)
const isConfirmModalOpen = ref(false)

const networkName = ref('')
const governanceModel = ref('Admin-based')
const dataModel = ref('Product-level Carbon Accounting')
const dropdown2 = ref<string | null>(null)

const governanceOptions = [{ label: 'Admin-based', value: 'Admin-based' }]
const dataOptions = [{ label: 'Product-level Carbon Accounting', value: 'Product-level Carbon Accounting' }]

/** Raw list from API */
const auditors = ref<CompanyLite[]>([])

/** Map for quick label lookup in badges */
const idToName = computed<Map<string, string>>(() => {
  return new Map(auditors.value.map(a => [a.companyId, a.companyName]))
})

/** User's picked auditors (IDs only) */
const selectedAuditorIds = ref<string[]>([])

/** Disable options already selected */
const options2Computed = computed<Option[]>(() =>
  auditors.value
    .map(a => ({
      label: a.companyName,
      value: a.companyId,
      disabled: selectedAuditorIds.value.includes(a.companyId),
    }))
    .filter(opt => !opt.disabled)
)

/** When a dropdown selection occurs, append to list and clear the select */
watch(dropdown2, (val) => {
  if (!val) return
  if (!selectedAuditorIds.value.includes(val)) {
    console.log("added auditor: ", val);
    selectedAuditorIds.value.push(val)
  }
  // clear the single-select so user can pick another

  dropdown2.value = null
})

function removeAuditor(id: string) {
  selectedAuditorIds.value = selectedAuditorIds.value.filter(x => x !== id)
}

function backToNetworks() {
  router.push({ name: 'networks' })
}

const refDropdown2 = ref<{ validate?: () => boolean } | null>(null)

async function loadAuditors() {
  // Pick the role type your backend expects for auditors (change if needed)
  const ROLE_TYPE = 'TPA'
  const list = await companyStore.fetchCompanyNamesByType(ROLE_TYPE)
  auditors.value = Array.isArray(list) ? list : []
}

onMounted(async () => {
  await loadAuditors()
  networkStore.editMode = true
  networkStore.updateMode = false
})


async function handleSubmit() {
  const hasName = networkName.value.trim().length > 0

  if (hasName) {
    try {
      const response = await api.get(`/bna/network/check-name-existence/${encodeURIComponent(networkName.value.trim())}`)
      const { exists } = response.data

      if (exists) {
        errorStore.showError('A network with this name already exists. Please choose a different name.')
        return
      }

      networkStore.setCurrentNetwork({
        name: networkName.value.trim(),
        auditorsIds: selectedAuditorIds.value,
      } as any)
      showOrganigram.value = true
    } catch (error) {
      console.error('Error checking name existence:', error)
      errorStore.showError('An error occurred while checking the network name. Please try again.')
    }
  }
}

const organigramRef = ref<InstanceType<typeof Organigram> | null>(null)

async function handleConfirm() {
  isConfirmModalOpen.value = true
}

async function handleModalConfirm() {
  const { name, auditorsIds } = (networkStore as any).currentNetwork ?? {}

  if (!name || !networkStore.currentNetworkTree) {
    console.warn('Missing network name or tree')
    return
  }

  const payload = {
    auditorIds: auditorsIds,
    name,
    tree: networkStore.currentNetworkTree
  }

  try {
    const res = await networkStore.createNetwork(payload)
    router.push({ name: 'network-details', params: { networkId: res.content.id } })
  } catch (e) {
    console.error('createNetwork failed', e)
  }
}

</script>
