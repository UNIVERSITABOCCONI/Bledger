<template>
  <section class="container page-wrapper">
    <div class="grid gap-4 align-items-center">
      <div class="col-span-7">
        <Title variant="display" color="blue" weight="regular">B-Ledger </Title>
        <VSpacer size="md" />
        <p class="paragraph">Digital platform for collaborative data-collection in business networks</p>
      </div>
      <div class="col-span-7 col-start-10">
        <Panel class="page-panel">
          <div class="section-form-fieldset">
            <div class="section-form">
              <Dropdown ref="roleDropdown" required v-model="dropdownRole" label="Role" :options="optionsRole"
                aria-required="true" placeholder="Select role" required-msg="This field is required!" />
            </div>
            <div class="section-form">
              <Dropdown ref="companyDropdown" required v-model="dropdownCompany" label="Company"
                :options="optionsCompany" aria-required="true" placeholder="Select company" />
            </div>
          </div>
          <div class="section-form">
            <Button type="submit" class="button" @click="handleSubmit">Login</Button>
          </div>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCompanyStore } from '@/stores/company'
import Dropdown from '../components/forms/Dropdown.vue'
import Panel from "@/components/panel/Panel.vue"
import Button from "@/components/button/Button.vue"
import Title from "@/components/title/Title.vue"
import VSpacer from "@/components/spacer/VSpacer.vue"

const router = useRouter()
const route = useRoute()
const store = useCompanyStore()

const dropdownRole = ref('')
const dropdownCompany = ref('')
const optionsRole = [
  { label: 'Organization', value: 'ORG' },
  { label: 'Third party Auditor', value: 'TPA' },
]
const optionsCompany = ref([])

const roleDropdown = ref(null)
const companyDropdown = ref(null)
const loading = ref(false)
const errorMsg = ref('')

// Fetch companies when role changes
watch(dropdownRole, async (role) => {
  errorMsg.value = ''
  optionsCompany.value = []
  dropdownCompany.value = ''
  if (!role) return

  loading.value = true
  try {
    const list = await store.fetchCompanyNamesByType(role)
    optionsCompany.value = (Array.isArray(list) ? list : []).map(c => ({
      label: c.companyName ?? c.name ?? String(c.label ?? c),
      value: c.id ?? c.value ?? c.companyId ?? String(c),
    }))
    if (optionsCompany.value.length === 1) {
      dropdownCompany.value = optionsCompany.value[0].value
    }
  } catch {
    errorMsg.value = 'Unable to load companies for the selected role.'
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  const validRole = roleDropdown.value?.validate?.() ?? !!dropdownRole.value
  const validCompany = companyDropdown.value?.validate?.() ?? !!dropdownCompany.value
  if (!validRole || !validCompany) return

  loading.value = true
  errorMsg.value = ''
  try {
    await store.login({ companyId: dropdownCompany.value })
    router.replace(route.query.redirect || '/')
  } catch {
    errorMsg.value = 'Login failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.button {
  margin-left: auto;
}

.paragraph {
  font-weight: 400;
  font-size: 1.2rem
}
</style>