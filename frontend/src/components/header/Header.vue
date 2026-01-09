<template>
  <header class="app-header" navItems="{{ navItems }}">
    <!-- Logo -->

    <div class="logo" @click="emit('logo-click', $event)">
      <!-- Inserirai il logo qui -->
      <slot name="logo">
        B-Ledger
        <em>.demo</em>
      </slot>
    </div>

    <nav v-if="showNavAndLogout" class="nav-center">
      <ul class="nav-list">
        <li v-for="item in navItems" :key="item.label" :class="{ active: item.active }" @click="handleNavigate(item)">
          <RouterLink :class="'nav-bar-link'" :to="item.to">{{ item.label }}</RouterLink>
        </li>
      </ul>
    </nav>

    <!-- Hamburger Menu -->
    <HamburgerMenu v-if="showNavAndLogout">
      <ul class="nav-list">
        <li v-for="item in navItems" :key="item.label" :class="{ active: item.active }" @click="handleNavigate(item)">
          <RouterLink :class="'nav-bar-link'" :to="item.to">{{ item.label }}</RouterLink>
        </li>
      </ul>

      <Button class="logout-mobile" variant="link" @click="emit('logout', $event)">
        <div class="logout-content">
          <div>{{ logoutLabel }}</div>
          <div class="logout-company">({{companyStore.company.companyName}})</div>
        </div>
      </Button>
    </HamburgerMenu>

    <Button v-if="showNavAndLogout" class="logout" variant="link" @click="emit('logout', $event)" icon iconPosition="right">
      <div class="logout-content">
        <div>{{ logoutLabel }}</div>
        <div class="logout-company">({{companyStore.company.companyName}})</div>
      </div>
      <template #icon>
        <IconLogout />
      </template>
    </Button>

    <!-- Logout a destra -->
  </header>
</template>

<script setup lang="ts">
import IconLogout from "@/components/icons/IconLogout.vue";
import Button from "@/components/button/Button.vue";
import HamburgerMenu from "@/components/hamburger/HamburgerMenu.vue";
import { useCompanyStore } from "@/stores/company";

const companyStore = useCompanyStore()

interface NavItem {
  label: string;
  active: boolean;
  to: string;
}

const props = withDefaults(defineProps<{
  navItems: NavItem[]
  logoutLabel?: string
  showNavAndLogout?: boolean
}>(), {
  logoutLabel: 'Logout',
  showNavAndLogout: true
})

const emit = defineEmits<{
  (e: 'logo-click', event: MouseEvent): void
  (e: 'logout', event: MouseEvent): void
}>()

function handleNavigate(item) {
  console.log('Navigated to:', item)
}

</script>


<style scoped>
@import "@/styles/header.css";
</style>
