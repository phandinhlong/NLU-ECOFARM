import { openDB, type DBSchema, type IDBPDatabase } from 'idb'
import type { EventTypeCode } from '@/api/types/log'

// One logical outbox entry per queued action (LOG event, harvest, or QC), recorded online or
// offline. The clientEventId is created once at record time and never changes, so a retry after a
// lost response reuses it and the server deduplicates — for LOG and harvest, which are idempotent.
// QC is NOT idempotent (no clientEventId on the server), so it is sent once and never auto-retried.
export type QueueStatus = 'PENDING' | 'SENDING' | 'SENT' | 'NEEDS_ATTENTION'

export type QueueKind = 'LOG' | 'HARVEST' | 'QC'

export interface QueuedEvent {
  clientEventId: string
  userId: number
  batchId: number
  // Discriminates how the sync engine sends this entry. Older entries written before this field
  // existed are treated as 'LOG' (see the v2 upgrade).
  kind: QueueKind

  // LOG fields (kind === 'LOG')
  eventType?: EventTypeCode
  occurredAt?: number
  deviceSentAt?: number
  schemaVersion?: number
  payload?: Record<string, unknown>
  attachments: Blob[]
  // File keys returned by a successful upload, so an upload is not repeated on retry.
  uploadedFileKeys: string[]

  // HARVEST fields (kind === 'HARVEST')
  harvestedAt?: number
  estimatedQuantity?: number
  area?: string

  // QC fields (kind === 'QC')
  brix?: number
  avgWeightKg?: number
  confirmPassed?: boolean
  sizeDescription?: string
  note?: string

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
const DB_VERSION = 2

let dbPromise: Promise<IDBPDatabase<OfflineDB>> | null = null

const db = () => {
  dbPromise ??= openDB<OfflineDB>(DB_NAME, DB_VERSION, {
    upgrade(database, oldVersion, _newVersion, tx) {
      if (oldVersion < 1) {
        const store = database.createObjectStore('queue', { keyPath: 'clientEventId' })
        store.createIndex('by-user', 'userId')
        store.createIndex('by-user-batch', ['userId', 'batchId'])
      }
      if (oldVersion < 2) {
        // Backfill the discriminator: every entry from v1 was a LOG event.
        const store = tx.objectStore('queue')
        store.openCursor().then(function next(cursor): Promise<void> | void {
          if (!cursor) return
          const value = cursor.value as QueuedEvent
          if (!value.kind) {
            value.kind = 'LOG'
            cursor.update(value)
          }
          return cursor.continue().then(next)
        })
      }
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
