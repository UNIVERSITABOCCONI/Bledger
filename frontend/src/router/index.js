import { createRouter, createWebHistory } from 'vue-router'
import HPCompany from '../views/HPCompany.vue'
import LoginView from '../views/LoginView.vue'
import { useCompanyStore } from '@/stores/company'
import NewNetwork from '@/views/NewNetwork.vue'
import Networks from '@/views/Networks.vue'
import NetworkDetailsAdmin from '@/views/NetworkDetailsAdmin.vue'
import UploadDocumentSuper from '@/views/UploadDocumentSuper.vue'
import AddCompany from '@/views/AddCompany.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HPCompany },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/new-network', name: 'new-network', component: NewNetwork },
    { path: '/networks', name: 'networks', component: Networks },
    {
      path: '/network-details/:networkId',
      name: 'network-details',
      component: NetworkDetailsAdmin,
    },
    {
      path: '/upload-document/:networkId',
      name: 'upload-document',
      component: UploadDocumentSuper,
    },
    {
      path: '/am/add-company',
      name: 'add-company',
      component: AddCompany,
    }
  ],
})

router.beforeEach(async (to) => {
  if (to.name === 'login' || to.name === 'add-company') return true

  const store = useCompanyStore()
  if (store.company) return true

  try {
    await store.fetchMyCompany()
    return true
  } catch {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
