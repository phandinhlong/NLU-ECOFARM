<script setup lang="ts">
import { ref } from 'vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'
import '../../styles/login.css'

const mode = ref<'login' | 'register'>('login')
</script>

<template>
  <div class="login-page d-flex flex-column justify-content-between min-vh-100 position-relative bg-body-secondary text-body">
    <main class="flex-grow-1 d-flex align-items-center justify-content-center w-100 px-3 py-4">
      <div class="card w-100 p-4 p-sm-5 bg-body border-0 shadow" style="max-width: 420px">
        <LoginForm v-if="mode === 'login'" @switch-to-register="mode = 'register'" />
        <RegisterForm v-else @switch-to-login="mode = 'login'" />
      </div>
    </main>

    <footer class="w-100 py-3 text-center fs-xs text-body-secondary">© 2026 EcoFarm</footer>
  </div>
</template>
