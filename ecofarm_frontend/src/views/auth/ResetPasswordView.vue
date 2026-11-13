<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import BrandHeader from '@/components/common/BrandHeader.vue'
import { useChangePasswordMutation } from '@/composables/useAuth'
import type { ChangePasswordRequest } from '@/api/types/auth'

const route = useRoute()
const isValid = route.query.status === 'reset-password'

const form = reactive<ChangePasswordRequest>({ password: '', confirmPassword: '' })
const errors = reactive({ password: '', confirmPassword: '' })
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const { mutate: changePassword, isPending, isError, reset } = useChangePasswordMutation()

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/

const validate = () => {
  errors.password = !form.password
      ? 'Vui lòng nhập mật khẩu'
      : !passwordRegex.test(form.password)
        ? 'Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường và 1 số'
        : ''
  errors.confirmPassword = !form.confirmPassword
      ? 'Vui lòng xác nhận mật khẩu'
      : form.confirmPassword !== form.password ? 'Mật khẩu xác nhận không khớp' : ''
  return !errors.password && !errors.confirmPassword
}

const submit = () => {
  if (!validate()) return
  changePassword(form)
}

watch(() => form.password, () => {
  errors.password = ''
  errors.confirmPassword = ''
})
watch(() => form.confirmPassword, () => { errors.confirmPassword = '' })

watch(form, () => {
  if (isError.value) reset()
})
</script>

<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100 p-4">
    <BCard class="w-100 p-4 p-sm-5 border-0 shadow" style="max-width: 420px">
      <template v-if="!isValid">
        <BrandHeader title="Đặt lại mật khẩu thất bại" subtitle="Hệ thống truy xuất nguồn gốc dưa lưới" />

        <div class="d-flex align-items-center justify-content-center rounded-circle bg-danger-subtle text-danger mx-auto my-4" style="width: 3.5rem; height: 3.5rem">
          <svg width="32" height="32" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
            <path d="M18 6 6 18"></path>
            <path d="m6 6 12 12"></path>
          </svg>
        </div>

        <p class="text-center small text-body-secondary mb-4">
          Liên kết không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại liên kết đặt lại mật khẩu.
        </p>

        <BButton to="/login" variant="primary" class="w-100 small shadow-sm">Về trang đăng nhập</BButton>
        <p class="text-center fs-xs text-body-secondary mt-3 mb-0">
          <RouterLink to="/forgot-password" class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover">
            Yêu cầu liên kết mới
          </RouterLink>
        </p>
      </template>

      <template v-else>
        <BrandHeader title="Đặt mật khẩu mới" subtitle="Nhập mật khẩu mới cho tài khoản của bạn" />

        <form class="d-flex flex-column gap-3 mt-4" novalidate @submit.prevent="submit">
          <div>
            <label class="form-label small fw-medium mb-1" for="newPassword">Mật khẩu</label>
            <div class="position-relative">
              <BFormInput
                  id="newPassword"
                  v-model="form.password"
                  class="border-0 shadow-sm small pe-5"
                  name="password"
                  placeholder="Nhập mật khẩu"
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
          </div>

          <div>
            <label class="form-label small fw-medium mb-1" for="newConfirmPassword">Xác nhận mật khẩu</label>
            <div class="position-relative">
              <BFormInput
                  id="newConfirmPassword"
                  v-model="form.confirmPassword"
                  class="border-0 shadow-sm small pe-5"
                  name="confirmPassword"
                  placeholder="Nhập lại mật khẩu"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  required
              />
              <button
                  class="btn border-0 position-absolute top-0 end-0 h-100 d-flex align-items-center pe-3 text-body-secondary"
                  type="button"
                  aria-label="Hiện hoặc ẩn mật khẩu"
                  @click="showConfirmPassword = !showConfirmPassword"
              >
                <svg v-if="showConfirmPassword" width="20" height="20" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
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
            <p v-if="errors.confirmPassword" class="small text-danger mt-2 mb-0">{{ errors.confirmPassword }}</p>
          </div>

          <BButton variant="primary" class="w-100 mt-2 small shadow-sm" type="submit" :disabled="isPending">
            {{ isPending ? 'Đang lưu...' : 'Đặt lại mật khẩu' }}
          </BButton>
        </form>
      </template>
    </BCard>
  </BContainer>
</template>
