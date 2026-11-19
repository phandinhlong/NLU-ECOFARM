import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { EventReviewStatus } from '@/api/types/event'

export const useEventReviewStore = defineStore('eventReview', () => {
  const selectedEventId = ref<number | null>(null)
  const filterGreenhouseId = ref<number | null>(null)
  const filterStatus = ref<EventReviewStatus | null>('UNREVIEWED')
  const rejectTargetId = ref<number | null>(null)
  const amendTargetId = ref<number | null>(null)

  const select = (id: number | null) => {
    selectedEventId.value = id
  }

  const setGreenhouseFilter = (id: number | null) => {
    filterGreenhouseId.value = id
  }

  const setStatusFilter = (status: EventReviewStatus | null) => {
    filterStatus.value = status
  }

  const openReject = (id: number) => {
    rejectTargetId.value = id
  }

  const closeReject = () => {
    rejectTargetId.value = null
  }

  const openAmend = (id: number) => {
    amendTargetId.value = id
  }

  const closeAmend = () => {
    amendTargetId.value = null
  }

  const reset = () => {
    selectedEventId.value = null
    filterGreenhouseId.value = null
    filterStatus.value = 'UNREVIEWED'
    rejectTargetId.value = null
    amendTargetId.value = null
  }

  return {
    selectedEventId,
    filterGreenhouseId,
    filterStatus,
    rejectTargetId,
    amendTargetId,
    select,
    setGreenhouseFilter,
    setStatusFilter,
    openReject,
    closeReject,
    openAmend,
    closeAmend,
    reset,
  }
})
