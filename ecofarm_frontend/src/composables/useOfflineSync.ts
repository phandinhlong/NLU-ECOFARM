import { ref } from 'vue'
import { offlineQueue, type QueuedEvent, type QueueStatus } from '@/offline/db'
import { syncUser } from '@/offline/sync'
import type { CreateEventRequest } from '@/api/types/log'
import type { HarvestRequest, QcRequest } from '@/api/types/quality'

// Shared across callers so the queue view and the record forms see the same state.
const queue = ref<QueuedEvent[]>([])
const syncing = ref(false)

export interface EnqueueInput {
  userId: number
  batchId: number
  request: Omit<CreateEventRequest, 'attachmentFileKeys' | 'deviceSentAt'>
  attachments: Blob[]
}

export interface EnqueueHarvestInput {
  userId: number
  batchId: number
  request: HarvestRequest
}

export interface EnqueueQcInput {
  userId: number
  batchId: number
  request: QcRequest
  attachments: Blob[]
}

const base = (userId: number, batchId: number, clientEventId: string): QueuedEvent => ({
  clientEventId,
  userId,
  batchId,
  kind: 'LOG',
  attachments: [],
  uploadedFileKeys: [],
  status: 'PENDING',
  attempts: 0,
  createdAt: Date.now(),
})

export function useOfflineSync() {
  const refresh = async (userId: number) => {
    queue.value = await offlineQueue.listForUser(userId)
  }

  // Persist first (status PENDING); only report success after the IndexedDB write commits.
  const enqueue = async (input: EnqueueInput): Promise<void> => {
    const entry: QueuedEvent = {
      ...base(input.userId, input.batchId, input.request.clientEventId),
      kind: 'LOG',
      eventType: input.request.eventType,
      occurredAt: input.request.occurredAt,
      deviceSentAt: Date.now(),
      schemaVersion: Number(input.request.payload.schemaVersion ?? 1),
      payload: input.request.payload,
      attachments: input.attachments,
    }
    await offlineQueue.put(entry)
    await refresh(input.userId)
  }

  const enqueueHarvest = async (input: EnqueueHarvestInput): Promise<void> => {
    const entry: QueuedEvent = {
      ...base(input.userId, input.batchId, input.request.clientEventId),
      kind: 'HARVEST',
      harvestedAt: input.request.harvestedAt,
      estimatedQuantity: input.request.estimatedQuantity,
      area: input.request.area,
    }
    await offlineQueue.put(entry)
    await refresh(input.userId)
  }

  const enqueueQc = async (input: EnqueueQcInput): Promise<void> => {
    // QC has no server-side idempotency key; generate a local id only to key the queue entry.
    const entry: QueuedEvent = {
      ...base(input.userId, input.batchId, crypto.randomUUID()),
      kind: 'QC',
      brix: input.request.brix,
      avgWeightKg: input.request.avgWeightKg,
      confirmPassed: input.request.confirmPassed,
      sizeDescription: input.request.sizeDescription,
      note: input.request.note,
      attachments: input.attachments,
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

  return { queue, syncing, refresh, enqueue, enqueueHarvest, enqueueQc, sync, discard, pending }
}
