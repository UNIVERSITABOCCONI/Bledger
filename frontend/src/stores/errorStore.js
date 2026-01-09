import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useCompanyStore } from './company'

export const useErrorStore = defineStore('error', () => {
  const isErrorModalOpen = ref(false)
  const errorMessage = ref(null)

  const setErrorModalOpen = (open) => {
    isErrorModalOpen.value = open
  }

  const setErrorMessage = (message) => {
    errorMessage.value = message
  }

  const showError = (message) => {
    const companyStore = useCompanyStore()
    if (companyStore.company !== null) {
      errorMessage.value = message
      isErrorModalOpen.value = true
    }
  }

  return {
    isErrorModalOpen: computed(() => isErrorModalOpen.value),
    errorMessage: computed(() => errorMessage.value),
    setErrorModalOpen,
    setErrorMessage,
    showError
  }
})