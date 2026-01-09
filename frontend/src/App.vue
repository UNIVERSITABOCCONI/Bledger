<template>
  <Header @logo-click="() => router.push('/')" @logout="handleLogout" :navItems="navItems" :showNavAndLogout="!!store.company" />

  <main class="main">
    <RouterView />
  </main>

  <Footer copyLabel="B-Ledger @ 2025. All rights reserved." />

  <ErrorModal />
</template>

<script setup>
import Header from "@/components/header/Header.vue"
import Footer from "@/components/footer/Footer.vue"
import ErrorModal from "@/components/modal/ErrorModal.vue"
import { useRouter, useRoute } from "vue-router"
import { computed } from "vue"
import { useCompanyStore } from "@/stores/company"
import { useNetworksStore } from "./stores/networks"
import { useTransactionStore } from "./stores/transaction"

const router = useRouter()
const route = useRoute()
const store = useCompanyStore()
const networkStore = useNetworksStore()
const transactionStore = useTransactionStore();

const navItems = computed(() => [
  { label: 'Overview', active: route.path === '/', to: '/' },
  { label: 'Networks', active: route.path !== '/', to: '/networks' },
])

async function handleLogout() {
  await store.logout()
  try {
    networkStore.$reset();
    store.$reset();
    URL.revokeObjectURL(store.companyProfileImage)
    transactionStore.$reset();
  } catch(e){
    console.log("error resetting store", e)
  }

  router.push('/login')
}
</script>