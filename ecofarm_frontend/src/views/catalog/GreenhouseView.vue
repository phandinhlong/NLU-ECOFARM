<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PagerBar from '@/components/common/PagerBar.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
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
const statusFilter = ref('')
const form = reactive({
  code: '',
  name: '',
  gpsLat: '',
  gpsLng: '',
  areaM2: '',
  status: 'ACTIVE' as GreenhouseStatus,
})

const statuses = Object.entries(GREENHOUSE_STATUS_LABELS) as [GreenhouseStatus, string][]
const badgeVariant: Record<GreenhouseStatus, 'success' | 'muted' | 'warning'> = {
  ACTIVE: 'success',
  INACTIVE: 'muted',
  MAINTENANCE: 'warning',
}

const page = computed<PageResponse<Greenhouse>>(
  () =>
    data.value ?? { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
)
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

const applyFilter = () => {
  params.value = {
    ...params.value,
    q: searchText.value.trim() || undefined,
    status: statusFilter.value || undefined,
    page: 0,
  }
}

const resetFilter = () => {
  searchText.value = ''
  statusFilter.value = ''
  params.value = { page: 0, size: params.value.size }
}

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}

const formatGps = (lat: number | null, lng: number | null) =>
  lat != null && lng != null ? `${lat.toFixed(4)}, ${lng.toFixed(4)}` : '—'

const formatArea = (value: number | null) =>
  value == null ? '—' : `${value.toLocaleString('vi-VN')} m²`
</script>

<template>
  <div>
    <PageHeader
      title="Quản lý nhà màng"
      subtitle="Danh mục nhà màng phục vụ truy xuất nguồn gốc lô dưa lưới."
      :breadcrumb="['Trang chủ', 'Quản lý danh mục', 'Nhà màng']"
    >
      <template #actions>
        <BButton variant="primary" @click="openCreate">+ Thêm nhà màng</BButton>
      </template>
    </PageHeader>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="gh-search">
            Tìm kiếm
          </label>
          <BFormInput
            id="gh-search"
            v-model="searchText"
            placeholder="Nhập mã hoặc tên nhà màng"
            @keyup.enter="applyFilter"
          />
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="gh-status-filter">
            Trạng thái
          </label>
          <BFormSelect id="gh-status-filter" v-model="statusFilter">
            <option value="">Tất cả trạng thái</option>
            <option v-for="[value, label] in statuses" :key="value" :value="value">
              {{ label }}
            </option>
          </BFormSelect>
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
          <p class="mb-1 fw-semibold">Danh sách nhà màng</p>
          <p class="small text-body-secondary mb-0">
            Mã nhà màng không thể đổi khi đã có lô tham chiếu.
          </p>
        </div>
        <SoftBadge variant="info" :label="`${page.totalElements} nhà màng`" />
      </div>

      <BAlert v-if="isError" variant="danger" class="mx-3 mt-3 mb-0">
        Không tải được danh sách nhà màng: {{ error?.message }}
      </BAlert>

      <div v-if="isLoading" class="d-flex justify-content-center py-5">
        <BSpinner />
      </div>

      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Mã nhà màng</th>
              <th scope="col">Tên nhà màng</th>
              <th scope="col">Tọa độ GPS</th>
              <th scope="col" class="text-end">Diện tích</th>
              <th scope="col" class="text-end">Số lô</th>
              <th scope="col">Trạng thái</th>
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
              <td class="fw-semibold">{{ row.code }}</td>
              <td>
                <div class="fw-semibold">{{ row.name }}</div>
                <div class="small text-body-secondary">ID #{{ row.id }} · phiên bản {{ row.version }}</div>
              </td>
              <td class="text-nowrap">{{ formatGps(row.gpsLat, row.gpsLng) }}</td>
              <td class="text-end text-nowrap">{{ formatArea(row.areaM2) }}</td>
              <td class="text-end">{{ row.batchCount ?? 0 }}</td>
              <td>
                <SoftBadge
                  :variant="badgeVariant[row.status]"
                  :label="GREENHOUSE_STATUS_LABELS[row.status] ?? row.status"
                />
              </td>
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
        unit="nhà màng"
        @update:page="goToPage"
      />
    </section>

    <BModal v-model="showModal" :title="editing ? 'Sửa nhà màng' : 'Thêm nhà màng'">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-semibold mb-1" for="gh-code">Mã nhà màng</label>
          <BFormInput id="gh-code" v-model="form.code" required maxlength="32" :disabled="isCodeLocked" />
          <p v-if="isCodeLocked" class="small text-body-secondary mt-2 mb-0">
            Không thể đổi mã khi nhà màng đã có lô tham chiếu.
          </p>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="gh-name">Tên nhà màng</label>
          <BFormInput id="gh-name" v-model="form.name" required maxlength="128" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="gh-lat">Vĩ độ (GPS)</label>
            <BFormInput id="gh-lat" v-model="form.gpsLat" type="number" step="any" min="-90" max="90" />
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="gh-lng">Kinh độ (GPS)</label>
            <BFormInput id="gh-lng" v-model="form.gpsLng" type="number" step="any" min="-180" max="180" />
          </div>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="gh-area">Diện tích (m²)</label>
          <BFormInput id="gh-area" v-model="form.areaM2" type="number" step="any" min="0" />
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="gh-status">Trạng thái</label>
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
