// src/stores/networks.ts (or .js)
import { defineStore } from 'pinia'
import api from '@/utils/api'
import { useCompanyStore } from '@/stores/company'

export const useNetworksStore = defineStore('networks', {
  state: () => ({
    admin: { content: [], total: 0, page: 0, size: 10 },
    participant: { content: [], total: 0, page: 0, size: 10 },
    tpaNetworks: { content: [], total: 0, page: 0, size: 10 },
    invitations: { content: [], total: 0, page: 0, size: 10 },
    lastCreated: null,
    currentNetwork: null,
    currentNetworkTree: null,
    selectedNode: null,
    currentNetworkMap: null,
    editMode: true,
    updateMode: false,
    loading: false,

    // Network details data
    networkDetails: null,
    clients: { content: [], total: 0, page: 0, size: 10 },
    suppliers: { content: [], total: 0, page: 0, size: 10 },
    otherMembers: { content: [], total: 0, page: 0, size: 10 }, // BNA
    refused: { content: [], total: 0, page: 0, size: 10 }, // BNA
    auditors: { content: [], total: 0, page: 0, size: 10 },
    // TPA specific data
    tpaRequests: { content: [], total: 0, page: 0, size: 10 },
    tpaOtherMembers: { content: [], total: 0, page: 0, size: 10 },
    nodeDetails: null,


    // NEW — raw list and a Set of available ids
    allOrgCompanies: [],
    // keep requested name (typo preserved): availbleOption
    availbleOption: new Set(),
    isDeleteModalOpen: false,
    removed: [],
  }),

  getters: {
    // dropdown-ready options (only available ones)
    orgDropdownOptions(state) {
      return state.allOrgCompanies
        .filter((c) => state.availbleOption.has(c.companyId))
        .map((c) => ({
          label: c.companyName,
          value: c.companyId,
          disabled: false,
        }))
    },

    getCurrentNetwork(state) {
      return state.currentNetwork
    }
  },

  actions: {
    async fetchAdminNetworks({ page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bna/network/my-networks?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.admin = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.admin
    },

    async fetchParticipantNetworks({ page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bu/network/my-networks?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.participant = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.participant
    },

    async fetchTpaNetworks({ page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/tpa/network/my-networks?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.tpaNetworks = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.tpaNetworks
    },

    async fetchInvitations({ page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bu/network/my-invitations?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.invitations = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.invitations
    },

    async acceptInvitation(networkId) {
      const { data } = await api.post(`/bu/network/accept-invitation/${networkId}`)
      return data
    },

    async refuseInvitation(networkId) {
      const { data } = await api.post(`/bu/network/refuse-invitation/${networkId}`)
      return data
    },

    async exportSuppliers(networkId) {
      const response = await api.get(`/bu/file/export/${networkId}`, {
        responseType: 'blob'
      })

      // Generate filename with YYYYMMDD_CompanyName pattern
      const companyStore = useCompanyStore()
      const companyName = companyStore.company?.companyName || 'UnknownCompany'
      const sanitizedCompany = companyName.replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '')
      const today = new Date()
      const dateStr = today.getFullYear() + String(today.getMonth() + 1).padStart(2, '0') + String(today.getDate()).padStart(2, '0')
      const filename = `${dateStr}_${sanitizedCompany}.zip`

      // Create blob link to download
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()

      // Clean up
      link.remove()
      window.URL.revokeObjectURL(url)

      return response.data
    },

    async computeE(networkId, body = null) {
      const { data } = await api.post(`/bu/network/compute-e/${networkId}`, body)
      return data
    },

    async computeScope3(networkId, body = null) {
      const { data } = await api.post(`/bu/network/compute-scope3/${networkId}`, body)
      return data
    },

    async createNetwork(request) {
      const { data } = await api.post('/bna/network/create', request)
      this.currentNetwork = data.content
      return data
    },

    setCurrentNetwork(network) {
      this.currentNetwork = network
    },
    resetCurrentNetwork() {
      this.currentNetwork = null
    },

    async fetchNetworkById(id) {
      const { data } = await api.get(`/bna/network/${id}`)
      this.currentNetwork = data
      return data
    },

    // NEW — load ORG companies once and initialize the Set of available ids
    async loadOrgCompanies() {
      const companyStore = useCompanyStore()
      const list = await companyStore.fetchCompanyNamesByType('ORG')
      // expect shape [{ companyId, companyName }]
      this.allOrgCompanies = Array.isArray(list) ? list : []
      this.availbleOption = new Set(this.allOrgCompanies.map((x) => x.companyId))
    },

    // NEW — consume an id when inserted/selected
    consumeCompanyId(companyId) {
      if (companyId) this.availbleOption.delete(companyId)
    },

    // NEW — release an id when removed/unselected
    releaseCompanyId(companyId) {
      if (companyId) this.availbleOption.add(companyId)
    },

    // Optional — reset to all available
    resetAvailableCompanies() {
      this.availbleOption = new Set(this.allOrgCompanies.map((x) => x.companyId))
    },

    // Network Details Actions
    async fetchNetworkDetails(networkId, isTpa = false) {
      const path = `/${isTpa ? 'tpa' : 'bu'}/network/details/${networkId}`
      const { data } = await api.get(path)
      this.networkDetails = data
      return data
    },


    async fetchClients(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bu/network/get-clients/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.clients = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.clients
    },

    async fetchSuppliers(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bu/network/get-suppliers/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.suppliers = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.suppliers
    },

    async fetchOtherMembers(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bna/network/get-other-members/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.otherMembers = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.otherMembers
    },

    async fetchRefused(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bna/network/get-refused/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.refused = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.refused
    },

    async fetchAuditors(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/bu/network/get-auditors/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.auditors = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.auditors
    },

    // TPA specific methods
    async fetchTpaRequests(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/tpa/network/my-requests/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.tpaRequests = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.tpaRequests
    },

    async fetchTpaOtherMembers(networkId, { page = 0, size = 10, sort = 'updated_at,desc' } = {}) {
      const { data } = await api.get(
        `/tpa/network/get-other-members/${networkId}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`,
      )
      this.tpaOtherMembers = {
        content: data?.content ?? (Array.isArray(data) ? data : []),
        total: data?.totalElements ?? (Array.isArray(data) ? data.length : 0),
        page,
        size,
      }
      return this.tpaOtherMembers
    },

    async fetchNodeDetails(nodeId) {
      const { data } = await api.get(`/common/network/node-details/${nodeId}`)
      this.nodeDetails = data
      return data
    },

    async verifyTpaData(nodeId) {
      const { data } = await api.post(`/tpa/network/verify-data/${nodeId}`)
      return data
    },

    async addAuditors(networkId, payload) {
      const { data } = await api.put(`/bna/network/add-auditors/${networkId}`, payload)
      return data
    },

    async requestAudit(networkId, payload) {
      const { data } = await api.post(`/bu/network/request-audit/${networkId}`, payload)
      return data
    },

    async fetchNetworkTreeAdmin(networkId) {
      const { data } = await api.get(`/bna/network/network-tree/get/${networkId}`)
      return data
    },

    async fetchNetworkTreeParticipant(networkId) {
      const { data } = await api.get(`/bu/network/network-tree/get/${networkId}`)
      return data
    },

    async updateNetworkTree(networkId) {
      const payload =
            {
              updatedTree: this.currentNetworkTree,
              removed: this.removed
            };
       const { data } = await api.put(`/bna/network/network-tree/update/${networkId}`, payload)
       // Reset payload and modes
        
       this.removed=[]
       this.editMode = false
       this.updateMode = false
       return data
     },

    initTree(companyId, companyName) {
      this.currentNetworkTree = {
        "companyId": companyId,
        "companyName": companyName,
        "children": []
      }

      this.currentNetworkMap= {}

      this.currentNetworkMap[companyId] = this.currentNetworkTree
      this.selectedNode = companyId;
      this.consumeCompanyId(companyId)
    },

    addNode(companyId, companyName, parentCompanyId) {
      const node = {
          "companyId": companyId,
          "companyName": companyName,
          "parentCompanyId": parentCompanyId,
          "children": []
        }

      this.currentNetworkMap[companyId] = node

      const parent = this.currentNetworkMap[parentCompanyId]

      parent.children.push(this.currentNetworkMap[companyId]);
      this.selectedNode = companyId;
      this.consumeCompanyId(companyId)
    },

    editNode(oldCompanyId, newCompanyId, newCompanyName) {
      const node = this.currentNetworkMap[oldCompanyId];
      if (!node) return;

      // Update node properties
      node.companyId = newCompanyId;
      node.companyName = newCompanyName;

      // Update map key if companyId changed
      if (oldCompanyId !== newCompanyId) {
        delete this.currentNetworkMap[oldCompanyId];
        this.currentNetworkMap[newCompanyId] = node;

        // Update parentCompanyId in children
        for (let child of node.children) {
          child.parentCompanyId = newCompanyId;
        }
      }

      // Update selectedNode if it was the edited node
      if (this.selectedNode === oldCompanyId) {
        this.selectedNode = newCompanyId;
      }
      this.releaseCompanyId(oldCompanyId)
      this.consumeCompanyId(newCompanyId)
    },

    removeNode(companyId) {
      const node = this.currentNetworkMap[companyId];
      if (!node) return;

      const parentNode = this.currentNetworkMap[node.parentCompanyId];
      if (parentNode) {
        const index = parentNode.children.indexOf(node);
        if (index > -1) parentNode.children.splice(index, 1);
      }

      // Recursively remove children
      const childrenIds = this.listChildren(companyId);
      childrenIds.forEach(childId => {
        delete this.currentNetworkMap[childId]
        this.releaseCompanyId(childId)
      });
      
      delete this.currentNetworkMap[companyId];
      this.releaseCompanyId(companyId)

      this.selectedNode = parentNode?.companyId || this.currentNetworkTree.companyId;

      if(this.updateMode && node.id) {
        this.removed.push(node.id)
      }
    },

    moveNode(companyId, parentCompanyId){
      console.log("moving node")
      const node = this.currentNetworkMap[companyId];
      const oldParentNode = node.parentCompanyId ? this.currentNetworkMap[node.parentCompanyId] : null;
      const parentNode = this.currentNetworkMap[parentCompanyId];

      node.parentCompanyId = parentCompanyId;
      parentNode.children.push(node);
      if (oldParentNode) {
        const index = oldParentNode.children.indexOf(node);
        if (index > -1) oldParentNode.children.splice(index, 1);
      }
    },
    
    listChildren(companyId, childrenList = []) {
      const node = this.currentNetworkMap[companyId];
      if (!node) return childrenList;

      for (let child of node.children) {
        childrenList.push(child.companyId);
        this.listChildren(child.companyId, childrenList);
      }
      return childrenList;
    },

    populateNetworkTree(treeData) {
      // Set the tree
      this.currentNetworkTree = treeData;

      // Add parentCompanyId to all nodes
      const addParentIds = (node, parentId = null) => {
        node.parentCompanyId = parentId;
        for (let child of node.children || []) {
          addParentIds(child, node.companyId);
        }
      };
      addParentIds(treeData);

      // Build currentNetworkMap
      this.currentNetworkMap = {};
      const buildMap = (node, map) => {
        map[node.companyId] = node;
        for (let child of node.children || []) {
          buildMap(child, map);
        }
      };
      buildMap(treeData, this.currentNetworkMap);

      // Consume all companyIds in the tree
      for (let companyId of Object.keys(this.currentNetworkMap)) {
        this.consumeCompanyId(companyId);
      }

      // Set selectedNode to root
      this.selectedNode = treeData.companyId;
    },

    moveSelected(dir) {

       if (!this.selectedNode) {
         this.selectedNode = this.currentNetworkTree.companyId
         return;
       }

       const node = this.currentNetworkMap[this.selectedNode];

       if(dir == 'up') {
         if(node.parentCompanyId) {
           this.selectedNode = node.parentCompanyId
         } else {
           this.selectedNode = node.companyId
         }
       } else if (dir == 'down') {
         if(node.children.length == 0) return;
         this.selectedNode = node.children[0].companyId
       } else if (dir == 'right') {
         if(node.parentCompanyId) {
           const parent = this.currentNetworkMap[node.parentCompanyId]
           const index = parent.children.indexOf(node) + 1
           this.selectedNode = parent.children[index % parent.children.length].companyId
         }
       } else if (dir == 'left') {
         if(node.parentCompanyId) {
           const parent = this.currentNetworkMap[node.parentCompanyId]
           const index = parent.children.indexOf(node) - 1
           const wrappedIndex = index < 0 ? parent.children.length - 1 : index
           this.selectedNode = parent.children[wrappedIndex].companyId
         }
       }
      },

    async updateNode(nodeId, payload) {
      const { data } = await api.post(`/bu/network/update-node/${nodeId}`, payload)
      return data
    },


   },
})
