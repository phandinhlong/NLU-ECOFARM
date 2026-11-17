<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import { catalogService } from '@/api/services/catalog.service'
import { useCertificateList } from '@/composables/useCertificates'
import { CERT_TYPE_LABELS, type CertificateQuery, type Greenhouse } from '@/api/types/catalog'

const greenhouseFilter = ref('')
const windowDays = ref(30)

const greenhouses = ref<Greenhouse[]>([])

const expiringParams = computed<CertificateQuery>(() => ({
  expiringInDays: windowDays.value,
  greenhouseId: greenhouseFilter.value === '' ? undefined : Number(greenhouseFilter.value),
}))

const {
  items: expiringItems,
  hasMore,
  loading,
  error,
  load,
  loadMore,
} = useCertificateList(expiringParams)

const totalGreenhouses = ref(0)
const totalVarieties = ref(0)
const totalCertificates = ref(0)
const totalExpiring = ref(0)

const loadStats = async () => {
  const [gh, va, ce, ex] = await Promise.allSettled([
    catalogService.countGreenhouses({}),
    catalogService.countVarieties({}),
    catalogService.countCertificates({}),
    catalogService.countCertificates(expiringParams.value),
  ])
  if (gh.status === 'fulfilled') totalGreenhouses.value = gh.value
  if (va.status === 'fulfilled') totalVarieties.value = va.value
  if (ce.status === 'fulfilled') totalCertificates.value = ce.value
  if (ex.status === 'fulfilled') totalExpiring.value = ex.value
}

const refreshAll = () => {
  load()
  loadStats()
}

onMounted(async () => {
  refreshAll()
  try {
    greenhouses.value = await catalogService.fetchAllGreenhouses()
  } catch {
    // Bỏ qua — danh sách nhà màng để trống, bảng vẫn hiển thị bình thường
  }
})

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
  if (days <= 15) return { variant: 'danger' as const, label: `Còn ${days} ngày` }
  if (days <= 30) return { variant: 'warning' as const, label: `Còn ${days} ngày` }
  return { variant: 'success' as const, label: `Còn ${days} ngày` }
}

const greenhouseLabel = (id: number | null): string => {
  if (id == null) return '—'
  const greenhouse = greenhouses.value.find((item) => item.id === id)
  return greenhouse ? greenhouse.code : `#${id}`
}

const statCards = computed(() => [
  { label: 'Nhà màng', value: totalGreenhouses.value, hint: 'Đang hoạt động trong hệ thống' },
  { label: 'Giống dưa', value: totalVarieties.value, hint: 'Mã giống đang quản lý' },
  { label: 'Chứng nhận', value: totalCertificates.value, hint: 'VietGAP / GlobalGAP / Organic' },
  { label: 'Sắp hết hạn', value: totalExpiring.value, hint: `${windowDays.value} ngày tới` },
])

const applyFilter = () => {
  refreshAll()
}

const resetFilter = () => {
  greenhouseFilter.value = ''
  windowDays.value = 30
  refreshAll()
}
</script>

<template>
  <div>
    <PageHeader title="Tổng quan hệ thống EcoFarm">
      <template #actions>
        <BButton variant="outline-secondary" @click="refreshAll">Làm mới số liệu</BButton>
      </template>
    </PageHeader>

    <div class="row g-3 mb-3">
      <div v-for="card in statCards" :key="card.label" class="col-12 col-sm-6 col-xl-3">
        <div class="border rounded-3 bg-body p-3 h-100">
          <p class="small text-uppercase fw-bold text-body-secondary mb-1">{{ card.label }}</p>
          <p class="fs-3 fw-bold mb-1">{{ card.value }}</p>
          <p class="small text-body-secondary mb-0">{{ card.hint }}</p>
        </div>
      </div>
    </div>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="dash-greenhouse">
            Nhà màng
          </label>
          <BFormSelect id="dash-greenhouse" v-model="greenhouseFilter" @change="applyFilter">
            <option value="">Tất cả nhà màng</option>
            <option v-for="item in greenhouses" :key="item.id" :value="String(item.id)">
              {{ item.code }} — {{ item.name }}
            </option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="dash-window">
            Khoảng thời gian
          </label>
          <BFormSelect id="dash-window" v-model.number="windowDays" @change="applyFilter">
            <option :value="30">30 ngày tới</option>
            <option :value="60">60 ngày tới</option>
            <option :value="90">90 ngày tới</option>
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
          <p class="mb-1 fw-semibold">Chứng nhận sắp hết hạn</p>
          <p class="small text-body-secondary mb-0">
            Danh sách chứng nhận có ngày hết hạn trong {{ windowDays }} ngày tới.
          </p>
        </div>
        <SoftBadge variant="info" label="Chỉ đọc" />
      </div>

      <BAlert v-if="error" variant="danger" class="mx-3 mt-3 mb-0">
        Không tải được dữ liệu: {{ error }}
      </BAlert>

      <div v-if="loading && expiringItems.length === 0" class="d-flex justify-content-center py-5">
        <BSpinner />
      </div>

      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Nhà màng</th>
              <th scope="col">Số chứng nhận</th>
              <th scope="col">Loại</th>
              <th scope="col">Hiệu lực</th>
              <th scope="col">Hết hạn</th>
              <th scope="col">Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="expiringItems.length === 0">
              <td colspan="6" class="text-center text-body-secondary py-4">
                Không có chứng nhận nào sắp hết hạn trong {{ windowDays }} ngày tới
              </td>
            </tr>
            <tr v-for="row in expiringItems" :key="row.id">
              <td class="fw-semibold">{{ greenhouseLabel(row.greenhouseId) }}</td>
              <td>{{ row.certNo }}</td>
              <td>{{ CERT_TYPE_LABELS[row.certType] ?? row.certType }}</td>
              <td class="text-nowrap">
                {{ row.validFrom ?? '—' }} → {{ row.validTo ?? '—' }}
              </td>
              <td class="text-nowrap">{{ row.validTo ?? '—' }}</td>
              <td>
                <SoftBadge
                  :variant="expiryBadge(row.validTo).variant"
                  :label="expiryBadge(row.validTo).label"
                />
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
  </div>
</template>
