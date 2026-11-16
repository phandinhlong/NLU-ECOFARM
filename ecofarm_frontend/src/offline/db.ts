import { openDB, type DBSchema, type IDBPDatabase } from 'idb'
import type { EventTypeCode } from '@/api/types/log'

// One logical outbox entry per cultivation event recorded (online or offline).
// The clientEventId is created once at record time and never changes, so a retry
// after a lost response reuses it and the server deduplicates.
export type QueueStatus = 'PENDING' | 'SENDING' | 'SENT' | 'NEEDS_ATTENTION'

export interface QueuedEvent {
  clientEventId: string
  userId: number
  batchId: number
  eventType: EventTypeCode
  occurredAt: number
  deviceSentAt: number
  schemaVersion: number
  payload: Record<string, unknown>
  attachments: Blob[]
  // File keys returned by a successful upload, so an upload is not repeated on retry.
  uploadedFileKeys: string[]
  status: QueueStatus
  attempts: number
  lastError?: string
  createdAt: number
}

interface OfflineDB extends DBSchema {
  queue: {
    key: string
    value: QueuedEvent
    indexes: { 'by-user': number; 'by-user-batch': [number, number] }
  }
}

const DB_NAME = 'ecofarm-offline'
const DB_VERSION = 1

let dbPromise: Promise<IDBPDatabase<OfflineDB>> | null = null

const db = () => {
  dbPromise ??= openDB<OfflineDB>(DB_NAME, DB_VERSION, {
    upgrade(database) {
      const store = database.createObjectStore('queue', { keyPath: 'clientEventId' })
      store.createIndex('by-user', 'userId')
      store.createIndex('by-user-batch', ['userId', 'batchId'])
    },
  })
  return dbPromise
}

export const offlineQueue = {
  async put(entry: QueuedEvent): Promise<void> {
    await (await db()).put('queue', entry)
  },

  async get(clientEventId: string): Promise<QueuedEvent | undefined> {
    return (await db()).get('queue', clientEventId)
  },

  // All entries for one user, oldest first, so a batch syncs in record order.
  async listForUser(userId: number): Promise<QueuedEvent[]> {
    const all = await (await db()).getAllFromIndex('queue', 'by-user', userId)
    return all.sort((a, b) => a.createdAt - b.createdAt)
  },

  async delete(clientEventId: string): Promise<void> {
    await (await db()).delete('queue', clientEventId)
  },

  // Remove already-sent entries for a user per the cleanup policy.
  async pruneSent(userId: number): Promise<void> {
    const database = await db()
    const tx = database.transaction('queue', 'readwrite')
    const index = tx.store.index('by-user')
    for await (const cursor of index.iterate(userId)) {
      if (cursor.value.status === 'SENT') await cursor.delete()
    }
    await tx.done
  },
}
