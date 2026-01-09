<template>
  <section class="container page-wrapper">
    <div class="grid gap-2">
      <!-- LEFT: Company card -->
      <div class="col-span-5">
        <CompanyCard />
      </div>

      <!-- RIGHT: Tables -->
      <div class="col-span-11" v-if="!loading">
        <Panel>

          <div class="section-form" v-if="!isTpa">
            <Button class="button" @click="goToNewNetwork">New Network</Button>
          </div>

          <!-- ORG Tables -->
          <template v-if="!isTpa">
            <!-- ADMIN -->
            <AdminNetworksTable />

            <VSpacer size="lg" />

            <!-- PARTICIPANT -->
            <ParticipantNetworksTable />

            <VSpacer size="lg" />

            <!-- INVITATIONS -->
            <InvitationsTable />
          </template>

          <!-- TPA Table -->
          <template v-else>
            <TpaNetworksTable />
          </template>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import Panel from "@/components/panel/Panel.vue"
import CompanyCard from "@/components/cards/CompanyCard.vue"
import VSpacer from "@/components/spacer/VSpacer.vue"
import Button from "@/components/button/Button.vue"
import { useCompanyStore } from '@/stores/company'
import { useNetworksStore } from '@/stores/networks'
import { useRouter } from 'vue-router'

// Table wrappers
import AdminNetworksTable from "@/components/table/AdminNetworksTable.vue"
import ParticipantNetworksTable from "@/components/table/ParticipantNetworksTable.vue"
import InvitationsTable from "@/components/table/InvitationsTable.vue"
import TpaNetworksTable from "@/components/table/TpaNetworksTable.vue"

// Stores
const companyStore = useCompanyStore()
const networksStore = useNetworksStore()

const loading = ref(false)
const isTpa = computed(() => companyStore.company?.companyType === 'TPA')

// Tables moved to wrapper components

const router = useRouter()

function goToNewNetwork() {
  router.push('/new-network')
}

// Fetch data on mount
onMounted(async () => {
  try { if (!companyStore.company) await companyStore.fetchMyCompany() } 
  catch (e) { console.log(e) }
  loading.value = true
  if (isTpa.value) {
    networksStore.tpaNetworks = null
    await networksStore.fetchTpaNetworks({ page: 0, size: 10, sort: 'updated_at,desc' })
  } else {
    networksStore.invitations = null
    networksStore.participant = null
    networksStore.admin = null
    await Promise.all([
      networksStore.fetchAdminNetworks({ page: 0, size: 10, sort: 'updated_at,desc' }),
      networksStore.fetchParticipantNetworks({ page: 0, size: 10, sort: 'updated_at,desc' }),
      networksStore.fetchInvitations({ page: 0, size: 10, sort: 'updated_at,desc' }),
    ])
  }
  loading.value = false
})
</script>

<style scoped>
.flex {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.uploaded {
  font-weight: 600;
}

.ico {
  width: 16px;
  height: 16px;
}

.ico-checked {
  width: 16px;
  height: 16px;
  color: #20b15a;
}

.button {
  margin-left: auto;
}
</style>
