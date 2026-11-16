<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { ColorVariant } from 'bootstrap-vue-next'
import { useOfflineSync } from '@/composables/useOfflineSync'
import { useUserStore } from '@/stores'
import type { QueueStatus } from '@/offline/db'

const userStore = useUserStore()
const userId = computed(() => userStore.user?.id ?? 0)
const { queue, syncing, refresh, sync, discard } = useOfflineSync()

onMounted(() => {
  if (userId.value) refresh(userId.value)
})

const statusVariant = (status: QueueStatus): ColorVariant => {
  switch (status) {
    case 'SENT':
      return 'success'
    case 'SENDING':
      return 'info'
    case 'NEEDS_ATTENTION':
      return 'danger'
    default:
      return 'warning'
  }
}

const formatTime = (ms: number): string => new Date(ms).toLocaleString()
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="mb-0">Hàng đợi đồng bộ</h4>
      <BButton variant="primary" :disabled="syncing || !userId" @click="sync(userId)">
        {{ syncing ? 'Đang đồng bộ…' : 'Đồng bộ ngay' }}
      </BButton>
    </div>

    <BAlert v-if="!queue.length" :model-value="true" variant="light">
      Không có sự kiện nào trong hàng đợi.
    </BAlert>

    <BCard v-for="entry in queue" :key="entry.clientEventId" class="mb-2">
      <div class="d-flex justify-content-between align-items-start gap-2">
        <div>
          <div class="d-flex align-items-center gap-2">
            <strong>{{ entry.eventType }}</strong>
            <BBadge :variant="statusVariant(entry.status)">{{ entry.status }}</BBadge>
            <span class="text-muted small">Lô #{{ entry.batchId }}</span>
          </div>
          <div class="text-muted small">
            Ghi: {{ formatTime(entry.createdAt) }} · Số lần thử: {{ entry.attempts }}
          </div>
          <div v-if="entry.lastError" class="text-danger small">{{ entry.lastError }}</div>
        </div>
        <BButton
          v-if="entry.status === 'NEEDS_ATTENTION'"
          size="sm"
          variant="outline-danger"
          @click="discard(userId, entry.clientEventId)"
        >
          Bỏ
        </BButton>
      </div>
    </BCard>
  </div>
</template>
