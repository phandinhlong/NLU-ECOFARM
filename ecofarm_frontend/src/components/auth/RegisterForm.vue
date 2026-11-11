<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BrandHeader from '@/components/common/BrandHeader.vue'
import { useRegisterMutation } from '@/composables/useAuth'
import type { RegisterRequest } from '@/api/types/auth'

const emit = defineEmits<{ 'switch-to-login': [] }>()

const registerForm = reactive<RegisterRequest>({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})
const registerErrors = reactive({ fullName: '', email: '', password: '', confirmPassword: '', role: '' })
const registerSuccess = ref(false)

const { mutate: register, isPending, isError, reset } = useRegisterMutation()
const errorMessage = 'Lỗi hệ thống'

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/

const validate = () => {
  registerErrors.fullName = !registerForm.fullName ? 'Vui lòng nhập họ và tên' : ''
  registerErrors.email = !registerForm.email
      ? 'Vui lòng nhập email'
      : !emailRegex.test(registerForm.email) ? 'Email không hợp lệ' : ''
  registerErrors.password = !registerForm.password
      ? 'Vui lòng nhập mật khẩu'
      : !passwordRegex.test(registerForm.password)
        ? 'Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường và 1 số'
        : ''
  registerErrors.confirmPassword = !registerForm.confirmPassword
      ? 'Vui lòng xác nhận mật khẩu'
      : registerForm.confirmPassword !== registerForm.password ? 'Mật khẩu xác nhận không khớp' : ''
  return !registerErrors.fullName && !registerErrors.email && !registerErrors.password
      && !registerErrors.confirmPassword
}

const submit = () => {
  if (!validate()) return
  register(registerForm, { onSuccess: () => { registerSuccess.value = true } })
}

watch(() => registerForm.fullName, () => { registerErrors.fullName = '' })
watch(() => registerForm.email, () => { registerErrors.email = '' })
watch(() => registerForm.password, () => {
  registerErrors.password = ''
  registerErrors.confirmPassword = ''
})
watch(() => registerForm.confirmPassword, () => { registerErrors.confirmPassword = '' })

watch(registerForm, () => {
  if (isError.value) reset()
})
</script>

<template>
  <div v-if="registerSuccess" class="text-center">
    <div class="d-flex align-items-center justify-content-center rounded-circle bg-success-subtle text-success mx-auto mb-3" style="width: 3.5rem; height: 3.5rem">
      <svg width="32" height="32" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24">
        <path d="M20 6 9 17l-5-5"></path>
      </svg>
    </div>
    <h2 class="fs-5 fw-bold mt-3 mb-2">Đã gửi yêu cầu</h2>
    <p class="small text-body-secondary mb-4">
      Quản trị viên sẽ liên hệ với bạn qua email khi tài khoản được tạo.
    </p>
    <BButton variant="secondary" class="w-100 small" type="button" @click="emit('switch-to-login')">
      Quay lại đăng nhập
    </BButton>
  </div>

  <template v-else>
    <BrandHeader title="Đăng ký tài khoản" subtitle="Gửi yêu cầu để quản trị viên cấp tài khoản cho bạn" />

    <form class="d-flex flex-column gap-3 mt-4" novalidate @submit.prevent="submit">
      <div>
        <label class="form-label small fw-medium mb-1" for="fullName">Họ và tên</label>
        <BFormInput
            id="fullName"
            v-model="registerForm.fullName"
            class="border-0 shadow-sm small"
            name="fullName"
            placeholder="Nguyễn Văn A"
            type="text"
            required
        />
        <p v-if="registerErrors.fullName" class="small text-danger mt-2 mb-0">{{ registerErrors.fullName }}</p>
      </div>

      <div>
        <label class="form-label small fw-medium mb-1" for="registerEmail">Email</label>
        <BFormInput
            id="registerEmail"
            v-model="registerForm.email"
            class="border-0 shadow-sm small"
            name="email"
            placeholder="nguyenvana@ecofarm.vn"
            type="email"
            required
        />
        <p v-if="registerErrors.email" class="small text-danger mt-2 mb-0">{{ registerErrors.email }}</p>
      </div>

      <div>
        <label class="form-label small fw-medium mb-1" for="registerPassword">Mật khẩu</label>
        <BFormInput
            id="registerPassword"
            v-model="registerForm.password"
            class="border-0 shadow-sm small"
            name="password"
            placeholder="Nhập mật khẩu"
            type="password"
            required
        />
        <p v-if="registerErrors.password" class="small text-danger mt-2 mb-0">{{ registerErrors.password }}</p>
      </div>

      <div>
        <label class="form-label small fw-medium mb-1" for="confirmPassword">Xác nhận mật khẩu</label>
        <BFormInput
            id="confirmPassword"
            v-model="registerForm.confirmPassword"
            class="border-0 shadow-sm small"
            name="confirmPassword"
            placeholder="Nhập lại mật khẩu"
            type="password"
            required
        />
        <p v-if="registerErrors.confirmPassword" class="small text-danger mt-2 mb-0">{{ registerErrors.confirmPassword }}</p>
      </div>

      <p v-if="isError" class="font-monospace small text-danger mb-0">{{ errorMessage }}</p>

      <BButton variant="primary" class="w-100 mt-2 small shadow-sm" type="submit" :disabled="isPending">
        {{ isPending ? 'Đang gửi...' : 'Gửi yêu cầu' }}
      </BButton>

      <div class="text-center mt-3 fs-xs text-body-secondary">
        Đã có tài khoản?
        <a class="fw-medium link-primary link-underline-opacity-0 link-underline-opacity-100-hover" href="#" @click.prevent="emit('switch-to-login')">Đăng nhập</a>
      </div>
    </form>
  </template>
</template>
