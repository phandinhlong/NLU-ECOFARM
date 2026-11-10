<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authService } from '@/api/services'
import { useUserStore } from '@/stores'
import BrandHeader from '@/components/common/BrandHeader.vue'
import type { LoginRequest } from '@/api/types/auth'

const MAX_ATTEMPTS = 3

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive<LoginRequest>({ username: '', password: '' })
const remember = ref(false)
const showPassword = ref(false)
const submitting = ref(false)
const error = ref('')
const attempts = ref(0)
const locked = ref(false)

const submit = async () => {
  if (submitting.value || locked.value) return

  error.value = ''
  submitting.value = true

  try {
    const { data } = await authService.login(form)
    userStore.setUser(data.data.user)
    userStore.setToken(data.data.token)
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/dashboard')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Đăng nhập thất bại'
    attempts.value += 1

    if (/khóa|locked|disabled/i.test(message) || attempts.value >= MAX_ATTEMPTS) {
      locked.value = true
      return
    }

    error.value = `Sai tên đăng nhập hoặc mật khẩu. Còn ${MAX_ATTEMPTS - attempts.value} lần thử.`
  } finally {
    submitting.value = false
  }
}

const reset = () => {
  locked.value = false
  attempts.value = 0
  error.value = ''
  form.password = ''
}
</script>

<template>
  <div class="login-page d-flex flex-column justify-content-between min-vh-100 position-relative">
    <header class="position-absolute top-0 end-0 p-4">
      <div class="d-flex align-items-center gap-1 login-lang">
        <button type="button">VN</button>
        <span class="login-lang-divider">|</span>
        <button type="button" class="login-lang-active">EN</button>
      </div>
    </header>

    <main class="flex-grow-1 d-flex align-items-center justify-content-center w-100 px-3 py-4">
      <div class="login-card card w-100 p-4 p-sm-5">
        <BrandHeader title="Đăng nhập" subtitle="Hệ thống truy xuất nguồn gốc dưa lưới" />

        <div v-if="!locked" class="mt-4">
          <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
            <div>
              <label class="form-label login-label" for="username">Tên đăng nhập</label>
              <BFormInput
                id="username"
                v-model="form.username"
                class="login-input"
                :class="{ 'is-error': !!error }"
                name="username"
                placeholder="Nhập tên đăng nhập hoặc email"
                type="text"
                required
              />
            </div>

            <div>
              <label class="form-label login-label" for="password">Mật khẩu</label>
              <div class="position-relative">
                <BFormInput
                  id="password"
                  v-model="form.password"
                  class="login-input pe-5"
                  :class="{ 'is-error': !!error }"
                  name="password"
                  placeholder="••••••••"
                  :type="showPassword ? 'text' : 'password'"
                  required
                />
                <button
                  class="login-pwd-toggle position-absolute top-0 end-0 h-100 d-flex align-items-center border-0 bg-transparent pe-3"
                  type="button"
                  aria-label="Hiện hoặc ẩn mật khẩu"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="showPassword" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
                    <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path>
                    <path d="M6.61 6.61A13.53 13.53 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path>
                    <line x1="2" x2="22" y1="2" y2="22"></line>
                  </svg>
                  <svg v-else fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
              </div>
              <p v-if="error" class="login-error mt-2 mb-0">{{ error }}</p>
            </div>

            <div class="d-flex align-items-center justify-content-between pt-1">
              <BFormCheckbox v-model="remember" name="remember" class="login-remember mb-0">
                Ghi nhớ đăng nhập
              </BFormCheckbox>
              <a class="login-link" href="#">Quên mật khẩu?</a>
            </div>

            <BButton class="login-submit w-100 mt-2" type="submit" :disabled="submitting">
              {{ submitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </BButton>

            <div class="text-center mt-3 login-hint">
              Chưa có tài khoản? <a href="#">Đăng ký</a>
            </div>
            <div class="text-center mt-2 login-hint">
              Cần hỗ trợ? <a href="#">Liên hệ quản trị viên</a>
            </div>
          </form>
        </div>

        <div v-else class="login-locked mt-4 d-flex flex-column align-items-center text-center">
          <div class="login-locked-box w-100 p-4 d-flex flex-column align-items-center">
            <div class="login-locked-icon d-flex align-items-center justify-content-center mb-3">
              <svg fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
                <rect width="18" height="11" x="3" y="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </div>
            <p class="login-locked-text mb-0">
              Tài khoản đã bị khóa tạm thời. Vui lòng liên hệ quản trị viên.
            </p>
          </div>
          <BButton class="login-back w-100 mt-4" type="button" @click="reset">Quay lại</BButton>
          <div class="login-hint mt-3">Thời gian mở khóa tự động: 15:00</div>
        </div>
      </div>
    </main>

    <footer class="w-100 py-3 text-center login-footer">© 2026 EcoFarm</footer>
  </div>
</template>
