<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useCertificatesQuery, useCreateCertificateMutation } from '@/composables/useCertificates'
import { useGreenhousesQuery } from '@/composables/useGreenhouses'
import {
  CERT_TYPE_LABELS,
  type Certificate,
  type CertificateInput,
  type CertificateQuery,
  type CatalogQuery,
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
const totalPages = computed(() => page.value.totalPages || 1)

const daysLeft = (validTo: string | null): number | null => {
  if (!validTo) return null
  const to = new Date(`${validTo}T00:00:00`)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return Math.round((to.getTime() - now.getTime()) / 86_400_000)
}

const expiryBadge = (validTo: string | null): { variant: string; label: string } => {
  const days = daysLeft(validTo)
  if (days == null) return { variant: 'secondary', label: '—' }
  if (days < 0) return { variant: 'danger', label: 'Hết hạn' }
  if (days <= 30) return { variant: 'warning', label: `Còn ${days} ngày` }
  return { variant: 'success', label: `Còn ${days} ngày` }
}

const greenhouseLabel = (id: number | null): string => {
  if (id == null) return '—'
  const greenhouse = greenhouses.value.find((g) => g.id === id)
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

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}
</script>

<template>
  <div>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
      <h2 class="mb-0">Chứng nhận</h2>
      <BButton variant="primary" @click="openCreate">Thêm chứng nhận</BButton>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-3">
      <BFormSelect
        v-model="greenhouseFilter"
        class="w-auto"
        aria-label="Lọc theo nhà màng"
      >
        <option value="">Tất cả nhà màng</option>
        <option v-for="g in greenhouses" :key="g.id" :value="String(g.id)">
          {{ g.code }} — {{ g.name }}
        </option>
      </BFormSelect>
      <BFormSelect
        v-model="expiringFilter"
        class="w-auto"
        aria-label="Lọc theo thời hạn"
      >
        <option value="">Tất cả thời hạn</option>
        <option value="30">Sắp hết hạn (30 ngày tới)</option>
      </BFormSelect>
    </div>

    <BAlert v-if="isError" variant="danger" dismissible>
      Không tải được danh sách chứng nhận: {{ error?.message }}
    </BAlert>

    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <template v-else>
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead>
            <tr>
              <th scope="col">Số chứng nhận</th>
              <th scope="col">Loại</th>
              <th scope="col">Nhà màng</th>
              <th scope="col">Hiệu lực từ</th>
              <th scope="col">Hết hạn</th>
              <th scope="col">Trạng thái hiệu lực</th>
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
              <td class="fw-medium">{{ row.certNo }}</td>
              <td>
                <span class="badge text-bg-info">{{ CERT_TYPE_LABELS[row.certType] ?? row.certType }}</span>
              </td>
              <td>{{ greenhouseLabel(row.greenhouseId) }}</td>
              <td>{{ row.validFrom ?? '—' }}</td>
              <td>{{ row.validTo ?? '—' }}</td>
              <td>
                <span class="badge" :class="`text-bg-${expiryBadge(row.validTo).variant}`">
                  {{ expiryBadge(row.validTo).label }}
                </span>
              </td>
              <td>
                <a
                  v-if="row.downloadUrl"
                  :href="row.downloadUrl"
                  target="_blank"
                  rel="noopener"
                  class="small fw-medium"
                >
                  Tải PDF
                </a>
                <span v-else class="small text-body-secondary text-truncate d-inline-block" style="max-width: 180px;">
                  {{ row.fileKey ?? '—' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="d-flex justify-content-between align-items-center">
        <span class="small text-body-secondary">
          Trang {{ page.page + 1 }}/{{ totalPages }} · {{ page.totalElements }} chứng nhận
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

    <BModal v-model="showModal" title="Thêm chứng nhận">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError.message }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-greenhouse">Nhà màng</label>
          <BFormSelect id="cert-greenhouse" v-model="form.greenhouseId" required>
            <option value="" disabled>Chọn nhà màng</option>
            <option v-for="g in greenhouses" :key="g.id" :value="String(g.id)">
              {{ g.code }} — {{ g.name }}
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
            <label class="form-label small fw-medium mb-1" for="cert-to">Ngày hết hạn</label>
            <BFormInput id="cert-to" v-model="form.validTo" type="date" required />
          </div>
        </div>
        <div>
          <label class="form-label small fw-medium mb-1" for="cert-file">Tệp PDF</label>
          <BFormInput id="cert-file" v-model="form.fileKey" maxlength="255" placeholder="Khóa tệp PDF đã tải lên MinIO (nếu có)" />
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
