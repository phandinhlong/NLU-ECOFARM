<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useSaveVarietyMutation, useVarietiesQuery } from '@/composables/useVarieties'
import type { PageResponse, Variety, VarietyInput } from '@/api/types/catalog'

const params = ref<{ page: number; size: number; q?: string }>({ page: 0, size: 10 })

const { data, isLoading, isError, error } = useVarietiesQuery(params)
const { mutate: save, isPending: isSaving, error: saveError, reset: resetSaveError } =
  useSaveVarietyMutation()

const showModal = ref(false)
const editing = ref<Variety | null>(null)
const searchText = ref('')
const form = reactive({
  code: '',
  name: '',
  daysPollinationToHarvest: '',
  minBrix: '',
  minIsolationDays: '',
})

const page = computed<PageResponse<Variety>>(
  () =>
    data.value ?? { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
)
const totalPages = computed(() => page.value.totalPages || 1)

const resetForm = () => {
  form.code = ''
  form.name = ''
  form.daysPollinationToHarvest = ''
  form.minBrix = ''
  form.minIsolationDays = ''
}

const openCreate = () => {
  editing.value = null
  resetForm()
  resetSaveError()
  showModal.value = true
}

const openEdit = (row: Variety) => {
  editing.value = row
  form.code = row.code
  form.name = row.name
  form.daysPollinationToHarvest = row.daysPollinationToHarvest?.toString() ?? ''
  form.minBrix = row.minBrix?.toString() ?? ''
  form.minIsolationDays = row.minIsolationDays?.toString() ?? ''
  resetSaveError()
  showModal.value = true
}

const toNumberOrNull = (value: string) => (value === '' ? null : Number(value))

const submit = () => {
  const payload: VarietyInput = {
    code: form.code.trim(),
    name: form.name.trim(),
    daysPollinationToHarvest: toNumberOrNull(form.daysPollinationToHarvest),
    minBrix: toNumberOrNull(form.minBrix),
    minIsolationDays: toNumberOrNull(form.minIsolationDays),
  }
  if (editing.value) {
    payload.version = editing.value.version
  }
  save(
    { id: editing.value?.id, data: payload },
    {
      onSuccess: () => {
        showModal.value = false
      },
    },
  )
}

const applySearch = () => {
  params.value = { ...params.value, q: searchText.value.trim() || undefined, page: 0 }
}

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}
</script>

<template>
  <div>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
      <h2 class="mb-0">Giống dưa</h2>
      <BButton variant="primary" @click="openCreate">Thêm giống</BButton>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-3">
      <form class="d-flex gap-2" @submit.prevent="applySearch">
        <BFormInput
          v-model="searchText"
          class="w-auto"
          placeholder="Tìm theo mã hoặc tên"
          aria-label="Tìm giống dưa"
        />
        <BButton variant="outline-secondary" type="submit">Tìm</BButton>
      </form>
    </div>

    <BAlert v-if="isError" variant="danger" dismissible>
      Không tải được danh sách giống: {{ error?.message }}
    </BAlert>

    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <template v-else>
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead>
            <tr>
              <th scope="col">Mã</th>
              <th scope="col">Tên</th>
              <th scope="col" class="text-end">Ngày thụ phấn → thu hoạch</th>
              <th scope="col" class="text-end">Ngưỡng Brix tối thiểu (°Bx)</th>
              <th scope="col" class="text-end">Ngày cách ly tối thiểu</th>
              <th scope="col" class="text-end">Số lô</th>
              <th scope="col" class="text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="page.content.length === 0">
              <td colspan="7" class="text-center text-body-secondary py-4">
                Chưa có giống dưa nào
              </td>
            </tr>
            <tr v-for="row in page.content" :key="row.id">
              <td class="fw-medium">{{ row.code }}</td>
              <td>{{ row.name }}</td>
              <td class="text-end">{{ row.daysPollinationToHarvest ?? '—' }}</td>
              <td class="text-end">{{ row.minBrix ?? '—' }}</td>
              <td class="text-end">{{ row.minIsolationDays ?? '—' }}</td>
              <td class="text-end">{{ row.batchCount }}</td>
              <td class="text-end">
                <BButton size="sm" variant="outline-primary" @click="openEdit(row)">
                  Sửa
                </BButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="d-flex justify-content-between align-items-center">
        <span class="small text-body-secondary">
          Trang {{ page.page + 1 }}/{{ totalPages }} · {{ page.totalElements }} giống
        </span>
        <div class="d-flex gap-2">
          <BButton
            size="sm"
            variant="outline-secondary"
            :disabled="page.page === 0"
            @click="goToPage(page.page - 1)"
          >
            Trước
          </BButton>
          <BButton
            size="sm"
            variant="outline-secondary"
            :disabled="page.page + 1 >= totalPages"
            @click="goToPage(page.page + 1)"
          >
            Sau
          </BButton>
        </div>
      </div>
    </template>

    <BModal v-model="showModal" :title="editing ? 'Sửa giống dưa' : 'Thêm giống dưa'">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-medium mb-1" for="v-code">Mã giống</label>
          <BFormInput id="v-code" v-model="form.code" required maxlength="32" />
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="v-name">Tên giống</label>
          <BFormInput id="v-name" v-model="form.name" required maxlength="128" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-medium mb-1" for="v-days">
              Ngày thụ phấn → thu hoạch
            </label>
            <BFormInput id="v-days" v-model="form.daysPollinationToHarvest" type="number" min="1" max="365" />
          </div>
          <div class="col-6">
            <label class="form-label small fw-medium mb-1" for="v-isolation">
              Ngày cách ly tối thiểu
            </label>
            <BFormInput id="v-isolation" v-model="form.minIsolationDays" type="number" min="0" max="365" />
          </div>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="v-brix">
            Ngưỡng Brix tối thiểu (°Bx)
          </label>
          <BFormInput id="v-brix" v-model="form.minBrix" type="number" step="0.1" min="0" max="30" />
        </div>
      </form>
      <template #footer>
        <BButton variant="outline-secondary" :disabled="isSaving" @click="showModal = false">
          Hủy
        </BButton>
        <BButton variant="primary" :disabled="isSaving" @click="submit">
          {{ isSaving ? 'Đang lưu...' : 'Lưu' }}
        </BButton>
      </template>
    </BModal>
  </div>
</template>
