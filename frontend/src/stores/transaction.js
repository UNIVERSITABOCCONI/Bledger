// src/stores/transaction.js
import { defineStore } from 'pinia'
import api from '@/utils/api'

export const useTransactionStore = defineStore('transaction', {
  state: () => ({
    networkTransactions: { content: [], total: 0, page: 0, size: 10 },
  }),

  actions: {
    // Poll transaction status
    async pollTransactionStatus(transactionId) {
      try {
        const { data } = await api.get(`/common/transaction/poll-status/${transactionId}`)
        return data
      } catch (error) {
        console.error('Error polling transaction status:', error)
        throw error
      }
    },

    // Fetch network transactions
    async fetchNetworkTransactions(networkId, options = {}) {
      try {
        const { data } = await api.get(`/common/transaction/get-network-transactions/${networkId}?sort=updatedAt,DESC`, {
          params: { page: this.networkTransactions.page, size: this.networkTransactions.size, ...options }
        })

        this.networkTransactions = {
          content: data.content,
          page: data.pageable.pageNumber,
          size: data.pageable.pageSize,
          total: data.totalElements
        }
      } catch (error) {
        console.error('Error fetching network transactions:', error)
      }
    },
  },
})