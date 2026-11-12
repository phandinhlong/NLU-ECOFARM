<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useGreenhousesQuery, useSaveGreenhouseMutation } from '@/composables/useGreenhouses'
import {
  GREENHOUSE_STATUS_LABELS,
  type Greenhouse,
  type GreenhouseInput,
  type GreenhouseStatus,
  type PageResponse,
} from '@/api/types/catalog'

const params = ref<{ page: number; size: number; q?: string; status?: string }>({
  page: 0,
  size: 10,
})

const { data, isLoading, isError, error } = useGreenhousesQuery(params)
const { mutate: save, isPending: isSaving, error: saveError, reset: resetSaveError } =
  useSaveGreenhouseMutation()

const showModal = ref(false)
const editing = ref<Greenhouse | null>(null)
const searchText = ref('')
const form = reactive({
  code: '',
  name: '',
  gpsLat: '',
  gpsLng: '',
  areaM2: '',
  status: 'ACTIVE' as GreenhouseStatus,
})

const statuses = Object.entries(GREENHOUSE_STATUS_LABELS) as [GreenhouseStatus, string][]
const badgeVariant: Record<GreenhouseStatus, string> = {
  ACTIVE: 'success',
  INACTIVE: 'secondary',
  MAINTENANCE: 'warning',
}

const page = computed<PageResponse<Greenhouse>>(
  () =>
    data.value ?? { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
)
const totalPages = computed(() => page.value.totalPages || 1)
const isCodeLocked = computed(() => editing.value != null && editing.value.batchCount > 0)

const resetForm = () => {
  form.code = ''
  form.name = ''
  form.gpsLat = ''
  form.gpsLng = ''
  form.areaM2 = ''
  form.status = 'ACTIVE'
}

const openCreate = () => {
  editing.value = null
  resetForm()
  resetSaveError()
  showModal.value = true
}

const openEdit = (row: Greenhouse) => {
  editing.value = row
  form.code = row.code
  form.name = row.name
  form.gpsLat = row.gpsLat?.toString() ?? ''
  form.gpsLng = row.gpsLng?.toString() ?? ''
  form.areaM2 = row.areaM2?.toString() ?? ''
  form.status = row.status
  resetSaveError()
  showModal.value = true
}

const submit = () => {
  const payload: GreenhouseInput = {
    code: form.code.trim(),
    name: form.name.trim(),
    gpsLat: form.gpsLat === '' ? null : Number(form.gpsLat),
    gpsLng: form.gpsLng === '' ? null : Number(form.gpsLng),
    areaM2: form.areaM2 === '' ? null : Number(form.areaM2),
    status: form.status,
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

watch(
  () => params.value.status,
  () => {
    params.value = { ...params.value, page: 0 }
  },
)

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}

const formatGps = (lat: number | null, lng: number | null) =>
  lat != null && lng != null ? `${lat}, ${lng}` : '—'
</script>

<template>
  <div>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
      <h2 class="mb-0">Nhà màng</h2>
      <BButton variant="primary" @click="openCreate">Thêm nhà màng</BButton>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-3">
      <form class="d-flex gap-2" @submit.prevent="applySearch">
        <BFormInput
          v-model="searchText"
          class="w-auto"
          placeholder="Tìm theo mã hoặc tên"
          aria-label="Tìm nhà màng"
        />
        <BButton variant="outline-secondary" type="submit">Tìm</BButton>
      </form>
      <BFormSelect
        v-model="params.status"
        class="w-auto"
        aria-label="Lọc theo trạng thái"
      >
        <option value="">Tất cả trạng thái</option>
        <option v-for="[value, label] in statuses" :key="value" :value="value">
          {{ label }}
        </option>
      </BFormSelect>
    </div>

    <BAlert v-if="isError" variant="danger" dismissible>
      Không tải được danh sách nhà màng: {{ error?.message }}
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
              <th scope="col">Tọa độ GPS</th>
              <th scope="col" class="text-end">Diện tích (m²)</th>
              <th scope="col">Trạng thái</th>
              <th scope="col" class="text-end">Số lô</th>
              <th scope="col" class="text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="page.content.length === 0">
              <td colspan="7" class="text-center text-body-secondary py-4">
                Chưa có nhà màng nào
              </td>
            </tr>
            <tr v-for="row in page.content" :key="row.id">
              <td class="fw-medium">{{ row.code }}</td>
              <td>{{ row.name }}</td>
              <td>{{ formatGps(row.gpsLat, row.gpsLng) }}</td>
              <td class="text-end">{{ row.areaM2 ?? '—' }}</td>
              <td>
                <span class="badge" :class="`text-bg-${badgeVariant[row.status]}`">
                  {{ GREENHOUSE_STATUS_LABELS[row.status] }}
                </span>
              </td>
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
          Trang {{ page.page + 1 }}/{{ totalPages }} · {{ page.totalElements }} nhà màng
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

    <BModal v-model="showModal" :title="editing ? 'Sửa nhà màng' : 'Thêm nhà màng'">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-medium mb-1" for="gh-code">Mã nhà màng</label>
          <BFormInput id="gh-code" v-model="form.code" required maxlength="32" :disabled="isCodeLocked" />
          <p v-if="isCodeLocked" class="small text-body-secondary mt-2 mb-0">
            Không thể đổi mã khi nhà màng đã có lô tham chiếu.
          </p>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="gh-name">Tên nhà màng</label>
          <BFormInput id="gh-name" v-model="form.name" required maxlength="128" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-medium mb-1" for="gh-lat">Vĩ độ (GPS)</label>
            <BFormInput id="gh-lat" v-model="form.gpsLat" type="number" step="any" min="-90" max="90" />
          </div>
          <div class="col-6">
            <label class="form-label small fw-medium mb-1" for="gh-lng">Kinh độ (GPS)</label>
            <BFormInput id="gh-lng" v-model="form.gpsLng" type="number" step="any" min="-180" max="180" />
          </div>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="gh-area">Diện tích (m²)</label>
          <BFormInput id="gh-area" v-model="form.areaM2" type="number" step="any" min="0" />
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="gh-status">Trạng thái</label>
          <BFormSelect id="gh-status" v-model="form.status">
            <option v-for="[value, label] in statuses" :key="value" :value="value">
              {{ label }}
            </option>
          </BFormSelect>
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
