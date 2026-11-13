<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BrandHeader from '@/components/common/BrandHeader.vue'
import { useForgotPasswordMutation } from '@/composables/useAuth'
import type { ForgotPasswordRequest } from '@/api/types/auth'

const form = reactive<ForgotPasswordRequest>({ email: '' })
const errors = reactive({ email: '' })
const sent = ref(false)

const { mutate: forgotPassword, isPending, isError, reset } = useForgotPasswordMutation()

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validate = () => {
  errors.email = !form.email
      ? 'Vui lòng nhập email'
      : !emailRegex.test(form.email) ? 'Email không hợp lệ' : ''
  return !errors.email
}

const submit = () => {
  if (!validate()) return
  forgotPassword(form, { onSuccess: () => { sent.value = true } })
}

watch(() => form.email, () => { errors.email = '' })

watch(form, () => {
  if (isError.value) reset()
})
</script>

<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100 p-4">
    <BCard class="w-100 p-4 p-sm-5 border-0 shadow" style="max-width: 420px">
      <BrandHeader title="Quên mật khẩu" subtitle="Nhập email để nhận liên kết đặt lại mật khẩu" />

      <div v-if="sent" class="text-center mt-4">
        <div class="d-flex align-items-center justify-content-center rounded-circle bg-success-subtle text-success mx-auto mb-3" style="width: 3.5rem; height: 3.5rem">
          <svg width="32" height="32" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
            <path d="M20 6 9 17l-5-5"></path>
          </svg>
        </div>
        <h2 class="fs-5 fw-bold mt-3 mb-2">Đã gửi email</h2>
        <p class="small text-body-secondary mb-4">
          Kiểm tra hộp thư của bạn để mở liên kết đặt lại mật khẩu. Liên kết có hiệu lực trong 10 phút.
        </p>
        <BButton to="/login" variant="secondary" class="w-100 small">Quay lại đăng nhập</BButton>
      </div>

      <template v-else>
        <form class="d-flex flex-column gap-3 mt-4" novalidate @submit.prevent="submit">
          <div>
            <label class="form-label small fw-medium mb-1" for="email">Email</label>
            <BFormInput
                id="email"
                v-model="form.email"
                class="border-0 shadow-sm small"
                name="email"
                placeholder="Nhập email"
                type="text"
                required
            />
            <p v-if="errors.email" class="small text-danger mt-2 mb-0">{{ errors.email }}</p>
          </div>

          <BButton variant="primary" class="w-100 mt-2 small shadow-sm" type="submit" :disabled="isPending">
            {{ isPending ? 'Đang gửi...' : 'Gửi liên kết' }}
          </BButton>
        </form>

        <div class="text-center mt-3 fs-xs text-body-secondary">
          <RouterLink to="/login" class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover">
            Quay lại đăng nhập
          </RouterLink>
        </div>
      </template>
    </BCard>
  </BContainer>
</template>
