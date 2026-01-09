// src/stores/fileStore.js
import { defineStore } from 'pinia'
import api from '@/utils/api'

export const useFileStore = defineStore('fileStore', {
  state: () => ({
    // Upload data
    uploadData: null,
    uploadedFiles: {
      globalScopeFile: null,
      granularScope1File: null,
      granularScope2File: null
    }
  }),

  actions: {
    async downloadFile(nodeId, companyName) {
      try {
        const response = await api.get(`/common/file/download/${nodeId}`, {
          responseType: 'blob'
        })

        // Create blob link to download
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `${companyName}.zip`)
        document.body.appendChild(link)
        link.click()

        // Clean up
        link.remove()
        window.URL.revokeObjectURL(url)

        return response.data
      } catch (error) {
        console.error('Error downloading file:', error)
        throw error
      }
    },

    // File Upload Actions
    async uploadScopeFiles(networkId, files = {}) {
      const formData = new FormData()
      if (files.granularScope1File) {
        formData.append('GRANULAR_SCOPE_1', files.granularScope1File)
      }
      if (files.granularScope2File) {
        formData.append('GRANULAR_SCOPE_2', files.granularScope2File)
      }
      if (files.globalScopeFile) {
        formData.append('GLOBAL_SCOPE_FILE', files.globalScopeFile)
      }

      const { data } = await api.post(`/bu/file/upload-scope-files/${networkId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      this.uploadData = data
      return data
    },

    async confirmFileData(networkId) {
      const { data } = await api.post(`/bu/file/confirm-file-data/${networkId}`)
      return data
    },

    async deleteFileData(networkId) {
      const { data } = await api.delete(`/bu/file/delete-data/${networkId}`)
      return data
    },

    setUploadedFiles(files) {
      this.uploadedFiles = { ...files }
    }
  }
})