import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref<'light' | 'dark' | 'auto'>('auto')

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
  }

  const setTheme = (newTheme: 'light' | 'dark' | 'auto') => {
    theme.value = newTheme
  }

  return {
    sidebarCollapsed,
    theme,
    toggleSidebar,
    setSidebarCollapsed,
    setTheme,
  }
})
