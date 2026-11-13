<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BrandHeader from '@/components/common/BrandHeader.vue'
import { useLoginMutation } from '@/composables/useAuth'
import type { LoginRequest } from '@/api/types/auth'

const emit = defineEmits<{ 'switch-to-register': [] }>()

const form = reactive<LoginRequest>({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const showPassword = ref(false)

const { mutate: login, isPending, isError, reset } = useLoginMutation()
const errorMessage = 'Lỗi hệ thống'

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/

const validate = () => {
  errors.email = !form.email
      ? 'Vui lòng nhập email'
      : !emailRegex.test(form.email) ? 'Email không hợp lệ' : ''
  errors.password = !form.password
      ? 'Vui lòng nhập mật khẩu'
      : !passwordRegex.test(form.password)
        ? 'Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường và 1 số'
        : ''
  return !errors.email && !errors.password
}

const submit = () => {
  if (!validate()) return
  login(form)
}

watch(() => form.email, () => { errors.email = '' })
watch(() => form.password, () => { errors.password = '' })

watch(form, () => {
  if (isError.value) reset()
})
</script>

<template>
  <BrandHeader title="Đăng nhập" subtitle="Hệ thống truy xuất nguồn gốc dưa lưới" />

  <form class="d-flex flex-column gap-3 mt-4" novalidate @submit.prevent="submit">
    <div>
      <label class="form-label small fw-medium mb-1" for="email">Email</label>
      <BFormInput
          id="email"
          v-model="form.email"
          class="border-0 shadow-sm small"
          :class="{ 'bg-danger-subtle text-danger': isError }"
          name="email"
          placeholder="Nhập email"
          type="text"
          required
      />
      <p v-if="errors.email" class="small text-danger mt-2 mb-0">{{ errors.email }}</p>
    </div>

    <div>
      <label class="form-label small fw-medium mb-1" for="password">Mật khẩu</label>
      <div class="position-relative">
        <BFormInput
            id="password"
            v-model="form.password"
            class="border-0 shadow-sm small pe-5"
            :class="{ 'bg-danger-subtle text-danger': isError }"
            name="password"
            placeholder="••••••••"
            :type="showPassword ? 'text' : 'password'"
            required
        />
        <button
            class="btn border-0 position-absolute top-0 end-0 h-100 d-flex align-items-center pe-3 text-body-secondary"
            type="button"
            aria-label="Hiện hoặc ẩn mật khẩu"
            @click="showPassword = !showPassword"
        >
          <svg v-if="showPassword" width="20" height="20" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path>
            <path d="M6.61 6.61A13.53 13.53 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path>
            <line x1="2" x2="22" y1="2" y2="22"></line>
          </svg>
          <svg v-else width="20" height="20" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
        </button>
      </div>
      <p v-if="errors.password" class="small text-danger mt-2 mb-0">{{ errors.password }}</p>
      <p v-if="isError" class="font-monospace small text-danger mt-2 mb-0">{{ errorMessage }}</p>
    </div>

    <div class="pt-1 text-end">
      <RouterLink to="/forgot-password" class="small fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover">
        Quên mật khẩu?
      </RouterLink>
    </div>

    <BButton variant="primary" class="w-100 mt-2 small shadow-sm" type="submit" :disabled="isPending">
      {{ isPending ? 'Đang đăng nhập...' : 'Đăng nhập' }}
    </BButton>

    <div class="text-center mt-3 fs-xs text-body-secondary">
      Chưa có tài khoản?
      <a class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover" href="#" @click.prevent="emit('switch-to-register')">Đăng ký</a>
    </div>
    <div class="text-center fs-xs text-body-secondary">
      Cần hỗ trợ?
      <a class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover" href="#">Liên hệ quản trị viên</a>
    </div>
  </form>
</template>
