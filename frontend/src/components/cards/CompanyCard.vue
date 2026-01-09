<template>
  <Panel class="company-card">
    <Image v-if="companyImage" class="company-card-image" :src="companyImage" :alt="name" />

    <Title variant="h3" weight="bold" color="dark-blue">
      {{ name }}
    </Title>

    <div class="company-wallet-info">
      <div>Digital Wallet <a target="_blank" :href="'https://amoy.polygonscan.com/address/' + wallet"><IconPolygon class="icon-polygon inline"/></a> </div> 
      <div class="company-wallet-address" :title="wallet" role="button" tabindex="0" aria-label="Copy wallet address"
        @click="copyToClipboard" @keydown.enter.prevent="copyToClipboard" @keydown.space.prevent="copyToClipboard">
        <span class="addr-clip">{{ wallet }}</span>
      </div>
      <span class="copied-badge" v-if="copied">Copied!</span>
    </div>
  </Panel>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Panel from "@/components/panel/Panel.vue"
import Image from "@/components/image/Image.vue"
import Title from "@/components/title/Title.vue"
import { useCompanyStore } from '@/stores/company'
import IconPolygon from '../icons/IconPolygon.vue'

const store = useCompanyStore()

const placeholder = '/company-placeholder.png'
const copied = ref(false)
const companyImage = ref<string>(null)

// Computed properties from store
const name = computed(() => store.company?.companyName ?? 'Company name')
const wallet = computed(() => store.company?.walletAddress ?? '')

// Fetch data on mount
onMounted(async () => {
  try {
    if (!store.company) {
      await store.fetchMyCompany()
    }

    if(!store.companyProfileImage){
      try {
        await store.fetchMyProfileImage()
        companyImage.value = store.companyProfileImage
      } catch {
        console.log("failed to company profile image");
        companyImage.value = placeholder
      }
    } else {
      companyImage.value = store.companyProfileImage
    }
    
  } catch (e) {
    console.log(e)
    companyImage.value = placeholder
  }
})

// Clean up blob URL
//onBeforeUnmount(() => {
  //if (blobUrlToRevoke) URL.revokeObjectURL(blobUrlToRevoke)
//})

async function copyToClipboard() {
  try {
    await navigator.clipboard.writeText(wallet.value || '')
    copied.value = true
    setTimeout(() => (copied.value = false), 1500)
  } catch (err) {
    console.error('Copy failed:', err)
  }
}
</script>

<style scoped>
@import "@/styles/companyCard.css";
@import "@/styles/xbrlCard.css";


/* optional: middle-ellipsis look */
.addr-clip {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copied-badge {
  margin-left: 8px;
  font-size: 0.75rem;
  opacity: 0.8;
}

.company-wallet-address {
  cursor: pointer;
  outline: none;
  text-align: center;
}

.company-wallet-address:focus {
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.3);
  border-radius: 6px;
  padding: 2px 4px;
}

.inline {
  display: inline;
}
</style>
