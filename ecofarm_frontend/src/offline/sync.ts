import { logService } from '@/api/services/log.service'
import { qualityService } from '@/api/services/quality.service'
import { offlineQueue, type QueuedEvent } from '@/offline/db'

// Errors that must stop retrying: auth problems keep the entry but halt (user must act),
// business conflicts/validation surface a reason and need the user to fix or discard.
const isAuthError = (status?: number) => status === 401 || status === 403
const isBusinessError = (status?: number) => status === 409 || status === 422 || status === 400

const statusOf = (e: unknown): number | undefined =>
  (e as { response?: { status?: number } })?.response?.status

export interface SyncOutcome {
  sent: number
  stoppedBatches: number[]
}

/**
 * Drains one user's queue. Entries are grouped by batch and sent in record order; a batch stops at
 * its first error so a dependent action (e.g. QC after harvest) never jumps ahead. Auth errors abort
 * the whole run without deleting anything. Only one tab runs this at a time via the Web Locks API;
 * the server remains the authority on permission, lock state and duplicates.
 */
export async function syncUser(userId: number): Promise<SyncOutcome> {
  const runner = async (): Promise<SyncOutcome> => {
    const entries = await offlineQueue.listForUser(userId)
    const byBatch = new Map<number, QueuedEvent[]>()
    for (const entry of entries) {
      if (entry.status === 'SENT') continue
      // QC that already needs attention is never retried automatically (not idempotent).
      if (entry.kind === 'QC' && entry.status === 'NEEDS_ATTENTION') continue
      const list = byBatch.get(entry.batchId) ?? []
      list.push(entry)
      byBatch.set(entry.batchId, list)
    }

    const outcome: SyncOutcome = { sent: 0, stoppedBatches: [] }
    for (const [batchId, batchEntries] of byBatch) {
      for (const entry of batchEntries) {
        try {
          await sendOne(entry)
          outcome.sent += 1
        } catch (e) {
          const status = statusOf(e)
          if (isAuthError(status)) {
            // Keep everything; stop the whole run and let the UI prompt re-login.
            return outcome
          }
          entry.attempts += 1
          entry.lastError = (e as Error).message
          // QC is not idempotent: a lost response may already have created the record, so a retry
          // could duplicate it. Any QC failure parks the entry for manual resolution. LOG/harvest
          // are idempotent via clientEventId, so a transient (non-business) error stays PENDING.
          if (entry.kind === 'QC') {
            entry.status = 'NEEDS_ATTENTION'
          } else {
            entry.status = isBusinessError(status) ? 'NEEDS_ATTENTION' : 'PENDING'
          }
          await offlineQueue.put(entry)
          // Stop this batch so later actions do not overtake the failed one.
          outcome.stoppedBatches.push(batchId)
          break
        }
      }
    }
    return outcome
  }

  // Single-writer across tabs. Where Web Locks is unavailable, fall back to running directly.
  if (typeof navigator !== 'undefined' && 'locks' in navigator) {
    return navigator.locks.request('ecofarm-offline-sync', runner)
  }
  return runner()
}

async function sendOne(entry: QueuedEvent): Promise<void> {
  entry.status = 'SENDING'
  await offlineQueue.put(entry)

  if (entry.kind === 'HARVEST') {
    await qualityService.harvest(entry.batchId, {
      clientEventId: entry.clientEventId,
      harvestedAt: entry.harvestedAt!,
      estimatedQuantity: entry.estimatedQuantity!,
      area: entry.area,
    })
    entry.status = 'SENT'
    await offlineQueue.put(entry)
    return
  }

  if (entry.kind === 'QC') {
    // Upload attachments once; reuse the returned keys if this somehow re-runs before parking.
    if (entry.attachments.length && entry.uploadedFileKeys.length === 0) {
      const files = entry.attachments.map(
        (blob, i) => new File([blob], `attachment-${i}`, { type: blob.type }),
      )
      const uploaded = await logService.uploadAttachments(entry.batchId, files)
      entry.uploadedFileKeys = uploaded.map((a) => a.fileKey)
      await offlineQueue.put(entry)
    }
    await qualityService.recordQc(entry.batchId, {
      brix: entry.brix!,
      avgWeightKg: entry.avgWeightKg!,
      confirmPassed: entry.confirmPassed,
      sizeDescription: entry.sizeDescription,
      note: entry.note,
      attachmentFileKeys: entry.uploadedFileKeys,
    })
    entry.status = 'SENT'
    await offlineQueue.put(entry)
    return
  }

  // kind === 'LOG'
  // Upload attachments once; reuse the returned keys on any later retry.
  if (entry.attachments.length && entry.uploadedFileKeys.length === 0) {
    const files = entry.attachments.map(
      (blob, i) => new File([blob], `attachment-${i}`, { type: blob.type }),
    )
    const uploaded = await logService.uploadAttachments(entry.batchId, files)
    entry.uploadedFileKeys = uploaded.map((a) => a.fileKey)
    await offlineQueue.put(entry)
  }

  await logService.createEvent(entry.batchId, {
    eventType: entry.eventType!,
    occurredAt: entry.occurredAt!,
    clientEventId: entry.clientEventId,
    payload: entry.payload!,
    attachmentFileKeys: entry.uploadedFileKeys,
    deviceSentAt: entry.deviceSentAt,
  })

  entry.status = 'SENT'
  await offlineQueue.put(entry)
}
