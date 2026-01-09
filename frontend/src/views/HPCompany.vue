<template>
  <section class="container page-wrapper">
    <div class="grid gap-2">
      <div class="col-span-5">
        <CompanyCard />
      </div>

      <div class="col-span-11">
        <Panel>
          <div class="grid gap-8">
            <div class="col-span-16">
              <Title variant="h6" weight="bold" color="dark-blue" sans>Company data</Title>
            </div>

            <div class="col-span-8">
              <div class="section-form">
                <InputText required label="Nation" v-model="dropdownNation" aria-required="true"
                  placeholder="Placeholder" disabled />
              </div>

              <VSpacer size="md" />

              <div class="section-form">
                <InputText required label="ID Type" v-model="dropdownIDType" aria-required="true"
                  placeholder="Placeholder" disabled />
              </div>
            </div>

            <div class="col-span-8">
              <div class="section-form">
                <InputText label="Company Name" :model-value="companyName" placeholder="placeholder" disabled />
              </div>

              <VSpacer size="md" />

              <div class="section-form">
                <InputText required v-model="dropdownIDNumber" label="ID Number" aria-required="true"
                  placeholder="Placeholder" disabled />
              </div>
            </div>

            <VSpacer size="xs" />
            <div class="col-span-16">
              <Title variant="h6" weight="bold" color="dark-blue" sans>Representative data</Title>
            </div>

            <div class="col-span-8">
              <div class="section-form">
                <InputText label="Name" placeholder="placeholder" :model-value="repName" disabled />
              </div>

              <VSpacer size="md" />

              <div class="section-form">
                <InputText type="email" label="E-mail" placeholder="placeholder" :model-value="email" disabled />
              </div>
            </div>

            <div class="col-span-8">
              <div class="section-form">
                <InputText label="Surname" placeholder="placeholder" :model-value="repSurname" disabled />
              </div>
            </div>

          </div>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Panel from "@/components/panel/Panel.vue";
import CompanyCard from "@/components/cards/CompanyCard.vue";
import Title from "@/components/title/Title.vue";
import InputText from "@/components/forms/InputText.vue";
import VSpacer from "@/components/spacer/VSpacer.vue";
import { useCompanyStore } from '@/stores/company'

const store = useCompanyStore()

// Placeholder di default per l’immagine
let blobUrlToRevoke: string | null = null

// Se non presente, carica my-company e poi l’immagine profilo
onMounted(async () => {

  hydrateDropdownsFromCompany()
})

// Pulisci l’ObjectURL creato
onBeforeUnmount(() => {
  if (blobUrlToRevoke) URL.revokeObjectURL(blobUrlToRevoke)
})

// --- Computed dai dati in store (CompanyDto) ---
const companyName = computed(() => store.company?.companyName ?? 'Company name')
const email = computed(() => store.company?.email ?? '')
const repName = computed(() => store.company?.representativeName ?? '')
const repSurname = computed(() => store.company?.representativeSurname ?? '')
const nationValue = computed(() => store.company?.nation ?? '')
const idTypeValue = computed(() => store.company?.idType ?? '')
const idNumberValue = computed(() => store.company?.idNumber ?? '')

// --- Dropdown v-model (valori selezionati) ---
const dropdownNation = ref<string>('')
const dropdownIDType = ref<string>('')
const dropdownIDNumber = ref<string>('')

// --- Opzioni (disabilitate, ma mostriamo il valore corrente) ---
const optionsNation = ref<{ label: string, value: string }[]>([])
const optionsIDType = ref<{ label: string, value: string }[]>([])
const optionsIDNumber = ref<{ label: string, value: string }[]>([])

function hydrateDropdownsFromCompany() {
  // Nation
  if (nationValue.value) {
    optionsNation.value = [{ label: nationValue.value, value: nationValue.value }]
    dropdownNation.value = nationValue.value
  } else {
    optionsNation.value = []
    dropdownNation.value = ''
  }

  // ID Type (enum come stringa)
  if (idTypeValue.value) {
    optionsIDType.value = [{ label: String(idTypeValue.value), value: String(idTypeValue.value) }]
    dropdownIDType.value = String(idTypeValue.value)
  } else {
    optionsIDType.value = []
    dropdownIDType.value = ''
  }

  // ID Number
  if (idNumberValue.value) {
    optionsIDNumber.value = [{ label: idNumberValue.value, value: idNumberValue.value }]
    dropdownIDNumber.value = idNumberValue.value
  } else {
    optionsIDNumber.value = []
    dropdownIDNumber.value = ''
  }
}
</script>


