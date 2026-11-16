import { ref } from 'vue'
import { offlineQueue, type QueuedEvent, type QueueStatus } from '@/offline/db'
import { syncUser } from '@/offline/sync'
import type { CreateEventRequest } from '@/api/types/log'

// Shared across callers so the queue view and the record form see the same state.
const queue = ref<QueuedEvent[]>([])
const syncing = ref(false)

export interface EnqueueInput {
  userId: number
  batchId: number
  request: Omit<CreateEventRequest, 'attachmentFileKeys' | 'deviceSentAt'>
  attachments: Blob[]
}

export function useOfflineSync() {
  const refresh = async (userId: number) => {
    queue.value = await offlineQueue.listForUser(userId)
  }

  // Persist first (status PENDING); only report success after the IndexedDB write commits.
  const enqueue = async (input: EnqueueInput): Promise<void> => {
    const entry: QueuedEvent = {
      clientEventId: input.request.clientEventId,
      userId: input.userId,
      batchId: input.batchId,
      eventType: input.request.eventType,
      occurredAt: input.request.occurredAt,
      deviceSentAt: Date.now(),
      schemaVersion: Number(input.request.payload.schemaVersion ?? 1),
      payload: input.request.payload,
      attachments: input.attachments,
      uploadedFileKeys: [],
      status: 'PENDING',
      attempts: 0,
      createdAt: Date.now(),
    }
    await offlineQueue.put(entry)
    await refresh(input.userId)
  }

  const sync = async (userId: number): Promise<void> => {
    if (syncing.value || !navigator.onLine) return
    syncing.value = true
    try {
      await syncUser(userId)
    } finally {
      syncing.value = false
      await refresh(userId)
    }
  }

  const discard = async (userId: number, clientEventId: string) => {
    await offlineQueue.delete(clientEventId)
    await refresh(userId)
  }

  const pending = (status: QueueStatus) => queue.value.filter((q) => q.status === status)

  return { queue, syncing, refresh, enqueue, sync, discard, pending }
}
