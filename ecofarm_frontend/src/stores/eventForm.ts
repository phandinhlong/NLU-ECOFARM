import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { EventType } from '@/api/types/event'

export const useEventFormStore = defineStore('eventForm', () => {
  const clientEventId = ref('')
  const eventType = ref<EventType | ''>('')
  const occurredAt = ref('')
  const data = ref<Record<string, unknown>>({})

  const regenerateClientId = () => {
    clientEventId.value = crypto.randomUUID()
  }

  const reset = () => {
    regenerateClientId()
    eventType.value = ''
    occurredAt.value = ''
    data.value = {}
  }

  const setEventType = (type: EventType) => {
    eventType.value = type
    data.value = {}
  }

  return { clientEventId, eventType, occurredAt, data, reset, regenerateClientId, setEventType }
})
