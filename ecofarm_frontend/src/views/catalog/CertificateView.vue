<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import { catalogService } from '@/api/services/catalog.service'
import { useCertificateList, useCreateCertificateMutation } from '@/composables/useCertificates'
import {
  CERT_TYPE_LABELS,
  type CertificateInput,
  type CertificateQuery,
  type Greenhouse,
} from '@/api/types/catalog'

const params = ref<CertificateQuery>({})

const { items, hasMore, loading, error, load, loadMore } = useCertificateList(params)
const { create, isSaving, saveError, resetSaveError } = useCreateCertificateMutation()

const greenhouses = ref<Greenhouse[]>([])

onMounted(async () => {
  load()
  try {
    greenhouses.value = await catalogService.fetchAllGreenhouses()
  } catch {
    // Bỏ qua — danh sách nhà màng để trống, bảng vẫn hiển thị bình thường
  }
})

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

const submit = async () => {
  const payload: CertificateInput = {
    greenhouseId: form.greenhouseId === '' ? null : Number(form.greenhouseId),
    certType: form.certType,
    certNo: form.certNo.trim(),
    validFrom: form.validFrom,
    validTo: form.validTo,
    fileKey: form.fileKey.trim() || undefined,
  }
  try {
    await create(payload)
    showModal.value = false
    await load()
  } catch {
    // Lỗi đã được gán vào saveError để hiển thị trong modal
  }
}

watch(greenhouseFilter, () => {
  params.value = {
    ...params.value,
    greenhouseId: greenhouseFilter.value === '' ? undefined : Number(greenhouseFilter.value),
  }
  load()
})

watch(expiringFilter, () => {
  params.value = {
    ...params.value,
    expiringInDays: expiringFilter.value === '' ? undefined : Number(expiringFilter.value),
  }
  load()
})

const resetFilter = () => {
  greenhouseFilter.value = ''
  expiringFilter.value = ''
  params.value = {}
  load()
}
</script>

<template>
  <div>
    <PageHeader title="Quản lý chứng nhận">
      <template #actions>
        <BButton variant="primary" @click="openCreate">+ Thêm chứng nhận</BButton>
      </template>
    </PageHeader>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="c-greenhouse">
            Nhà màng
          </label>
          <BFormSelect id="c-greenhouse" v-model="greenhouseFilter">
            <option value="">Tất cả nhà màng</option>
            <option v-for="item in greenhouses" :key="item.id" :value="String(item.id)">
              {{ item.code }} — {{ item.name }}
            </option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="c-expiring">
            Thời hạn
          </label>
          <BFormSelect id="c-expiring" v-model="expiringFilter">
            <option value="">Tất cả thời hạn</option>
            <option value="30">Sắp hết hạn (30 ngày tới)</option>
            <option value="90">Sắp hết hạn (90 ngày tới)</option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-5 d-flex gap-2">
          <BButton variant="outline-secondary" @click="resetFilter">Đặt lại</BButton>
        </div>
      </div>
    </section>

    <section class="border rounded-3 bg-body">
      <div class="p-3 border-bottom d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <div>
          <p class="mb-1 fw-semibold">Danh sách chứng nhận</p>
          <p class="small text-body-secondary mb-0">
            Hệ thống cảnh báo khi chứng nhận còn dưới 30 ngày hiệu lực.
          </p>
        </div>
        <SoftBadge variant="info" :label="`${items.length}${hasMore ? '+' : ''} chứng nhận`" />
      </div>

      <BAlert v-if="error" variant="danger" class="mx-3 mt-3 mb-0">
        Không tải được danh sách chứng nhận: {{ error }}
      </BAlert>

      <div v-if="loading && items.length === 0" class="d-flex justify-content-center py-5">
        <BSpinner />
      </div>

      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
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
            <tr v-if="items.length === 0">
              <td colspan="7" class="text-center text-body-secondary py-4">
                Chưa có chứng nhận nào
              </td>
            </tr>
            <tr v-for="row in items" :key="row.id">
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
                <span v-else class="small text-body-secondary text-break">
                  {{ row.fileKey ?? '—' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="hasMore" class="p-3 border-top text-center">
        <BButton variant="outline-secondary" :disabled="loading" @click="loadMore">
          {{ loading ? 'Đang tải...' : 'Tải thêm' }}
        </BButton>
      </div>
    </section>

    <BModal v-model="showModal" title="Thêm chứng nhận">
      <BAlert v-if="saveError" variant="danger" dismissible>
        {{ saveError }}
      </BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submit">
        <div>
          <label class="form-label small fw-semibold mb-1" for="cert-greenhouse">Nhà màng</label>
          <BFormSelect id="cert-greenhouse" v-model="form.greenhouseId" required>
            <option value="" disabled>Chọn nhà màng</option>
            <option v-for="item in greenhouses" :key="item.id" :value="String(item.id)">
              {{ item.code }} — {{ item.name }}
            </option>
          </BFormSelect>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="cert-type">Loại chứng nhận</label>
          <BFormSelect id="cert-type" v-model="form.certType">
            <option v-for="[value, label] in certTypes" :key="value" :value="value">
              {{ label }}
            </option>
          </BFormSelect>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="cert-no">Số chứng nhận</label>
          <BFormInput id="cert-no" v-model="form.certNo" required maxlength="64" placeholder="VD: VG-DL-2026-001" />
        </div>
        <div class="row g-3">
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="cert-from">Ngày hiệu lực</label>
            <BFormInput id="cert-from" v-model="form.validFrom" type="date" required />
          </div>
          <div class="col-6">
            <label class="form-label small fw-semibold mb-1" for="cert-to">Ngày hết hạn</label>
            <BFormInput id="cert-to" v-model="form.validTo" type="date" required />
          </div>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="cert-file">Tệp PDF</label>
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
