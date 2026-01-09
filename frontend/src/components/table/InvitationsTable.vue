<template>
  <TableCustom
    :headers="headers"
    :rows="rows"
    :total="slice.total"
    :page="slice.page"
    :size="slice.size"
    :fetchFn="fetchPage"
  />
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import TableCustom from '@/components/table/TableCustom.vue'
import Button from '@/components/button/Button.vue'
import { useNetworksStore } from '@/stores/networks'

const networksStore = useNetworksStore()

const headers = [
  { key: 'name', name: 'Invitations', width: '50%', type: 'name' },
  { key: 'action', name: '', width: '50%', type: 'action' },
]

const slice = computed(() => networksStore.invitations || { content: [], total: 0, page: 0, size: 10 })

function actionCell(invitationId: string | number) {
  const handleAccept = async () => {
    try {
      await networksStore.acceptInvitation(invitationId)
      await networksStore.fetchInvitations({ page: slice.value.page, size: slice.value.size, sort: 'updated_at,desc' })
      await networksStore.fetchParticipantNetworks()
    } catch (error) {
      console.error('Error accepting invitation:', error)
    }
  }
  const handleDecline = async () => {
    try {
      await networksStore.refuseInvitation(invitationId)
      await networksStore.fetchInvitations({ page: slice.value.page, size: slice.value.size, sort: 'updated_at,desc' })
      await networksStore.fetchParticipantNetworks()
    } catch (error) {
      console.error('Error declining invitation:', error)
    }
  }

  return h(
    'div',
    { class: 'flex gap-1 justify-end' },
    [
      h(Button as any, { variant: 'secondary', onClick: handleAccept }, { default: () => 'Accept' }),
      h(Button as any, { variant: 'alert', onClick: handleDecline }, { default: () => 'Decline' }),
    ]
  )
}

const rows = computed(() =>
  (slice.value.content ?? []).map((x: any) => ({
    id: x.id,
    name: x.networkName,
    action: actionCell(x.id),
  }))
)

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchInvitations({
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>