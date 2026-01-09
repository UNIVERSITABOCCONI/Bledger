// src/stores/company.ts (or .js)
import { defineStore } from 'pinia'
import api from '@/utils/api'


export const useCompanyStore = defineStore('company', {
  
  state: () => ({
  company: null,
  companyProfileImage: null
  }),

  actions: {

    setCompany(data) {
      this.company = data
    },

    // GET companies by role
    async fetchCompanyNamesByType(roleType) {
      const { data } = await api.get(`/public/company/get-company-names-by-type/${roleType}`)
      return data
    },

    // GET my-company
    async fetchMyCompany() {
      const { data } = await api.get('/common/company/my-company')
      this.setCompany(data)
      return data
    },

    // POST login with only companyId
    async login({ companyId }) {
      await api.post('/public/auth/login', { companyId })
      return await this.fetchMyCompany()
    },

    async fetchMyProfileImage() {
      const resp = await api.get('/common/company/my-profile-image', { responseType: 'blob', ignoreError: true })
      const blobUrl = URL.createObjectURL(resp.data)
      this.companyProfileImage=blobUrl
      return blobUrl
      // (Tip) Revoke when you’re done displaying it: URL.revokeObjectURL(blobUrl)
    },

    async logout() {
      try {
        await api.post('/common/auth/logout')
      } catch (e) {
        console.warn('Logout API error:', e)
      }
      this.company = null
      // or: this.$reset()
    },
  },
})
