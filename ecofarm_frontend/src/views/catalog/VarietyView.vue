<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PagerBar from '@/components/common/PagerBar.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
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
const isCodeLocked = computed(() => editing.value != null && editing.value.batchCount > 0)

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

const applyFilter = () => {
  params.value = { ...params.value, q: searchText.value.trim() || undefined, page: 0 }
}

const resetFilter = () => {
  searchText.value = ''
  params.value = { page: 0, size: params.value.size }
}

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}

const formatDays = (value: number | null) => (value == null ? '—' : `${value} ngày`)
const formatBrix = (value: number | null) => (value == null ? '—' : `${value} °Bx`)

const brixVariant = (value: number | null): 'success' | 'warning' | 'muted' => {
  if (value == null) return 'muted'
  if (value >= 10) return 'success'
  return 'warning'
}
</script>

<template>
  <div>
    <PageHeader
      title="Quản lý giống dưa"
      subtitle="Thông tin canh tác của từng giống: thời gian sinh trưởng, độ Brix và ngày cách ly."
      :breadcrumb="['Trang chủ', 'Quản lý danh mục', 'Giống dưa']"
    >
      <template #actions>
        <BButton variant="primary" @click="openCreate">+ Thêm giống</BButton>
      </template>
    </PageHeader>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="v-search">
            Tìm kiếm
          </label>
          <BFormInput
            id="v-search"
            v-model="searchText"
            placeholder="Nhập mã hoặc tên giống"
            @keyup.enter="applyFilter"
          />
        </div>
        <div class="col-12 col-md-5 d-flex gap-2">
          <BButton variant="primary" @click="applyFilter">Lọc dữ liệu</BButton>
          <BButton variant="outline-secondary" @click="resetFilter">Đặt lại</BButton>
        </div>
      </div>
    </section>

    <section class="border rounded-3 bg-body">
      <div class="p-3 border-bottom d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <div>
          <p class="mb-1 fw-semibold">Danh sách giống dưa</p>
          <p class="small text-body-secondary mb-0">
            Mã giống không thể đổi khi đã có lô tham chiếu.
          </p>
        </div>
        <SoftBadge variant="info" :label="`${page.totalElements} giống`" />
      </div>

      <BAlert v-if="isError" variant="danger" class="mx-3 mt-3 mb-0">
        Không tải được danh sách giống: {{ error?.message }}
      </BAlert>

      <div v-if="isLoading" class="d-flex justify-content-center py-5">
        <BSpinner />
      </div>

      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Mã giống</th>
              <th scope="col">Tên giống</th>
              <th scope="col" class="text-end">Thụ phấn → thu hoạch</th>
              <th scope="col" class="text-end">Brix tối thiểu</th>
              <th scope="col" class="text-end">Ngày cách ly</th>
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
              <td class="fw-semibold">{{ row.code }}</td>
              <td>
                <div class="fw-semibold">{{ row.name }}</div>
                <div class="small text-body-secondary">ID #{{ row.id }} · phiên bản {{ row.version }}</div>
              </td>
              <td class="text-end text-nowrap">{{ formatDays(row.daysPollinationToHarvest) }}</td>
              <td class="text-end">
                <SoftBadge :variant="brixVariant(row.minBrix)" :label="formatBrix(row.minBrix)" />
              </td>
              <td class="text-end text-nowrap">{{ formatDays(row.minIsolationDays) }}</td>
              <td class="text-end">{{ row.batchCount ?? 0 }}</td>
              <td class="text-end">
                <BButton size="sm" variant="outline-primary" @click="openEdit(row)">Sửa</BButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PagerBar
        :page="page.page"
        :total-pages="page.totalPages"
        :total-elements="page.totalElements"
        :size="page.size"
        unit="giống"
        @update:page="goToPage"
      />
    </section>

    <BModal v-model="showModal" :title="editing ? 'Sửa giống dưa' : 'Thêm giống dưa'">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-semibold mb-1" for="v-code">Mã giống</label>
          <BFormInput id="v-code" v-model="form.code" required maxlength="32" :disabled="isCodeLocked" />
          <p v-if="isCodeLocked" class="small text-body-secondary mt-2 mb-0">
            Không thể đổi mã khi giống đã có lô tham chiếu.
          </p>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="v-name">Tên giống</label>
          <BFormInput id="v-name" v-model="form.name" required maxlength="128" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="v-days">
              Ngày thụ phấn → thu hoạch
            </label>
            <BFormInput id="v-days" v-model="form.daysPollinationToHarvest" type="number" min="1" max="365" />
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="v-isolation">
              Ngày cách ly tối thiểu
            </label>
            <BFormInput id="v-isolation" v-model="form.minIsolationDays" type="number" min="0" max="365" />
          </div>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="v-brix">
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
