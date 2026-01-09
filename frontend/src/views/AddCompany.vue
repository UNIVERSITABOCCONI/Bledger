<template>
  <section class="container page-wrapper">
    <div class="grid gap-4 align-items-center">
      <div class="col-span-12">
        <Panel class="page-panel">
          <div class="panel-head">
            <div class="title-heading-4">Add Company</div>
          </div>
          <VSpacer size="md" />
          <div class="section-form-fieldset">
            <div class="section-form">
              <InputText
                v-model="username"
                label="Username"
                placeholder="Enter username"
                required
              />
            </div>
            <div class="section-form">
              <InputText
                v-model="password"
                type="password"
                label="Password"
                placeholder="Enter password"
                required
              />
            </div>
            <div class="section-form">
              <label for="file" class="input-label">File</label>
              <input
                id="file"
                type="file"
                accept=".zip"
                @change="handleFileChange"
                required
              />
            </div>
          </div>
          <div class="section-form">
            <Button type="submit" class="button" @click="handleSubmit" :disabled="!isFormValid">Submit</Button>
          </div>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import InputText from '../components/forms/InputText.vue'
import Panel from "@/components/panel/Panel.vue"
import Button from "@/components/button/Button.vue"
import VSpacer from "@/components/spacer/VSpacer.vue"
import api from '../utils/api.js'

const username = ref('')
const password = ref('')
const file = ref(null)

const isFormValid = computed(() => username.value && password.value && file.value)

function handleFileChange(event) {
  file.value = event.target.files[0]
}

async function handleSubmit() {
  if (!isFormValid.value) return

  const formData = new FormData()
  formData.append('COMPANIES_ZIP', file.value)

  const authHeader = 'Basic ' + btoa(username.value + ':' + password.value)

  try {
    const response = await api.post('/am/add-companies', formData, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'multipart/form-data'
      }
    })
    console.log('Success:', response.data)
    // Handle success, maybe show a message or redirect
  } catch (error) {
    console.error('Error:', error)
    // Handle error
  }
}
</script>

<style scoped>
.button {
  margin-left: auto;
}
</style>