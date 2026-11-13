<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PagerBar from '@/components/common/PagerBar.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import { useCertificatesQuery, useCreateCertificateMutation } from '@/composables/useCertificates'
import { useGreenhousesQuery } from '@/composables/useGreenhouses'
import {
  CERT_TYPE_LABELS,
  type CatalogQuery,
  type Certificate,
  type CertificateInput,
  type CertificateQuery,
  type PageResponse,
} from '@/api/types/catalog'

const params = ref<CertificateQuery>({ page: 0, size: 10 })

const { data, isLoading, isError, error } = useCertificatesQuery(params)
const { mutate: create, isPending: isSaving, error: saveError, reset: resetSaveError } =
  useCreateCertificateMutation()

const greenhouseOptions = ref<CatalogQuery>({ page: 0, size: 100 })
const { data: greenhousePage } = useGreenhousesQuery(greenhouseOptions)
const greenhouses = computed(() => greenhousePage.value?.content ?? [])

const showModal = ref(false)
const greenhouseFilter = ref('')
const expiringFilter = ref('')
const form = reactive({
  greenhouseId: '',
  certType: 'VIETGAP',
  certNo: '',
  validFrom: '',
  validTo: '',
  fileKey: '',
})

const certTypes = Object.entries(CERT_TYPE_LABELS)

const page = computed<PageResponse<Certificate>>(
  () =>
    data.value ?? { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
)

const daysLeft = (validTo: string | null): number | null => {
  if (!validTo) return null
  const to = new Date(`${validTo}T00:00:00`)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return Math.round((to.getTime() - now.getTime()) / 86_400_000)
}

const expiryBadge = (validTo: string | null) => {
  const days = daysLeft(validTo)
  if (days == null) return { variant: 'muted' as const, label: '—' }
  if (days < 0) return { variant: 'danger' as const, label: 'Hết hạn' }
  if (days <= 30) return { variant: 'warning' as const, label: `Còn ${days} ngày` }
  return { variant: 'success' as const, label: `Còn ${days} ngày` }
}

const greenhouseLabel = (id: number | null): string => {
  if (id == null) return '—'
  const greenhouse = greenhouses.value.find((item) => item.id === id)
  return greenhouse ? `${greenhouse.code} — ${greenhouse.name}` : `#${id}`
}

const openCreate = () => {
  form.greenhouseId = ''
  form.certType = 'VIETGAP'
  form.certNo = ''
  form.validFrom = ''
  form.validTo = ''
  form.fileKey = ''
  resetSaveError()
  showModal.value = true
}

const submit = () => {
  const payload: CertificateInput = {
    greenhouseId: form.greenhouseId === '' ? null : Number(form.greenhouseId),
    certType: form.certType,
    certNo: form.certNo.trim(),
    validFrom: form.validFrom,
    validTo: form.validTo,
    fileKey: form.fileKey.trim() || undefined,
  }
  create(payload, {
    onSuccess: () => {
      showModal.value = false
    },
  })
}

watch(greenhouseFilter, () => {
  params.value = {
    ...params.value,
    greenhouseId: greenhouseFilter.value === '' ? undefined : Number(greenhouseFilter.value),
    page: 0,
  }
})

watch(expiringFilter, () => {
  params.value = {
    ...params.value,
    expiringInDays: expiringFilter.value === '' ? undefined : Number(expiringFilter.value),
    page: 0,
  }
})

const resetFilter = () => {
  greenhouseFilter.value = ''
  expiringFilter.value = ''
  params.value = { page: 0, size: params.value.size }
}

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}
</script>

<template>
  <div>
    <PageHeader
      title="Quản lý chứng nhận"
      subtitle="Chứng nhận VietGAP, GlobalGAP và Organic gắn với từng nhà màng, kèm cảnh báo hết hạn."
      :breadcrumb="['Trang chủ', 'Quản lý danh mục', 'Chứng nhận']"
    >
      <template #actions>
        <BButton variant="primary" @click="openCreate">+ Thêm chứng nhận</BButton>
      </template>
    </PageHeader>

    <div class="ef-card ef-card-pad mb-3">
      <div class="ef-filter-grid">
        <div>
          <label class="ef-label" for="c-greenhouse">Nhà màng</label>
          <BFormSelect id="c-greenhouse" v-model="greenhouseFilter">
            <option value="">Tất cả nhà màng</option>
            <option v-for="item in greenhouses" :key="item.id" :value="String(item.id)">
              {{ item.code }} — {{ item.name }}
            </option>
          </BFormSelect>
        </div>
        <div>
          <label class="ef-label" for="c-expiring">Thời hạn</label>
          <BFormSelect id="c-expiring" v-model="expiringFilter">
            <option value="">Tất cả thời hạn</option>
            <option value="30">Sắp hết hạn (30 ngày tới)</option>
            <option value="90">Sắp hết hạn (90 ngày tới)</option>
          </BFormSelect>
        </div>
        <div class="d-flex gap-2">
          <BButton variant="primary" @click="resetFilter">Đặt lại</BButton>
        </div>
      </div>
    </div>

    <div class="ef-card">
      <div class="ef-card-pad d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <div>
          <p class="mb-1 fw-semibold" style="color: #0f172a">Danh sách chứng nhận</p>
          <p class="ef-stat-hint mb-0">
            Hệ thống cảnh báo khi chứng nhận còn dưới 30 ngày hiệu lực.
          </p>
        </div>
        <SoftBadge variant="info" :label="`${page.totalElements} chứng nhận`" />
      </div>

      <BAlert v-if="isError" variant="danger" class="mx-3 mt-3 mb-0">
        Không tải được danh sách chứng nhận: {{ error?.message }}
      </BAlert>

      <div v-if="isLoading" class="d-flex justify-content-center py-5">
        <BSpinner />
      </div>

      <div v-else class="table-responsive">
        <table class="ef-table">
          <thead>
            <tr>
              <th scope="col">Số chứng nhận</th>
              <th scope="col">Loại</th>
              <th scope="col">Nhà màng</th>
              <th scope="col">Hiệu lực</th>
              <th scope="col">Hết hạn</th>
              <th scope="col">Trạng thái</th>
              <th scope="col">Tệp PDF</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="page.content.length === 0">
              <td colspan="7" class="text-center text-body-secondary py-4">
                Chưa có chứng nhận nào
              </td>
            </tr>
            <tr v-for="row in page.content" :key="row.id">
              <td class="fw-semibold">{{ row.certNo }}</td>
              <td>
                <SoftBadge variant="info" :label="CERT_TYPE_LABELS[row.certType] ?? row.certType" />
              </td>
              <td>{{ greenhouseLabel(row.greenhouseId) }}</td>
              <td class="text-nowrap">{{ row.validFrom ?? '—' }}</td>
              <td class="text-nowrap">{{ row.validTo ?? '—' }}</td>
              <td>
                <SoftBadge
                  :variant="expiryBadge(row.validTo).variant"
                  :label="expiryBadge(row.validTo).label"
                />
              </td>
              <td>
                <a
                  v-if="row.downloadUrl"
                  :href="row.downloadUrl"
                  target="_blank"
                  rel="noopener"
                  class="small fw-semibold text-decoration-none"
                >
                  Tải PDF
                </a>
                <span v-else class="ef-stat-hint d-inline-block text-truncate" style="max-width: 170px">
                  {{ row.fileKey ?? '—' }}
                </span>
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
        unit="chứng nhận"
        @update:page="goToPage"
      />
    </div>

    <BModal v-model="showModal" title="Thêm chứng nhận">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-greenhouse">Nhà màng</label>
          <BFormSelect id="cert-greenhouse" v-model="form.greenhouseId" required>
            <option value="" disabled>Chọn nhà màng</option>
            <option v-for="item in greenhouses" :key="item.id" :value="String(item.id)">
              {{ item.code }} — {{ item.name }}
            </option>
          </BFormSelect>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-type">Loại chứng nhận</label>
          <BFormSelect id="cert-type" v-model="form.certType">
            <option v-for="[value, label] in certTypes" :key="value" :value="value">
              {{ label }}
            </option>
          </BFormSelect>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-no">Số chứng nhận</label>
          <BFormInput id="cert-no" v-model="form.certNo" required maxlength="64" placeholder="VD: VG-DL-2026-001" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-medium mb-1" for="cert-from">Ngày hiệu lực</label>
            <BFormInput id="cert-from" v-model="form.validFrom" type="date" required />
          </div>
          <div class="col-6">
            <label class="form-label fw-medium mb-1 small" for="cert-to">Ngày hết hạn</label>
            <BFormInput id="cert-to" v-model="form.validTo" type="date" required />
          </div>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-file">Tệp PDF</label>
          <BFormInput
            id="cert-file"
            v-model="form.fileKey"
            maxlength="255"
            placeholder="Khóa tệp PDF đã tải lên MinIO (nếu có)"
          />
          <p class="small text-body-secondary mt-2 mb-0">
            Tải tệp lên MinIO trước, sau đó nhập khóa tệp (fileKey) vào đây.
          </p>
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
