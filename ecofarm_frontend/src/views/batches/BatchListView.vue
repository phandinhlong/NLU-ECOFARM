<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import PagerBar from '@/components/common/PagerBar.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import {
  useAssignWorkersMutation,
  useBatchLookupQuery,
  useBatchesQuery,
  useCreateBatchMutation,
} from '@/composables/useBatches'
import { useUserStore } from '@/stores'
import {
  STATUS_META,
  formatDate,
  previewBatchCode,
  type Batch,
  type BatchCreateInput,
  type BatchListParams,
  type BatchStatus,
  type Problem,
} from '@/api/types/batch'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roles = computed(() => userStore.user?.role ?? [])
const isEngineer = computed(() => roles.value.includes('ENGINEER'))
const isWorker = computed(() => roles.value.includes('WORKER'))

const toNum = (value: unknown): number | undefined => {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
}

const params = ref<BatchListParams>({
  page: Number(route.query.page) || 0,
  size: Number(route.query.size) || 10,
  status: (route.query.status as BatchStatus) || undefined,
  greenhouseId: toNum(route.query.greenhouseId),
  q: (route.query.q as string) || undefined,
  mine: route.query.mine === '1',
})

const searchText = ref(params.value.q ?? '')
const statusFilter = ref<BatchStatus | ''>(params.value.status ?? '')
const greenhouseFilter = ref<number | ''>(params.value.greenhouseId ?? '')
const mineFilter = ref(params.value.mine ?? false)

const syncUrl = () => {
  const query: Record<string, string> = { page: String(params.value.page), size: String(params.value.size) }
  if (params.value.status) query.status = params.value.status
  if (params.value.greenhouseId) query.greenhouseId = String(params.value.greenhouseId)
  if (params.value.q) query.q = params.value.q
  if (params.value.mine) query.mine = '1'
  router.replace({ query })
}

watch(params, syncUrl, { deep: true })

const { data, isLoading, isError, error, refetch } = useBatchesQuery(params)
const lookups = useBatchLookupQuery()
const createMutation = useCreateBatchMutation()
const assignMutation = useAssignWorkersMutation()

const page = computed(
  () => data.value ?? { content: [], page: 0, size: params.value.size, totalElements: 0, totalPages: 0 },
)
const hasFilters = computed(
  () =>
    Boolean(params.value.q) ||
    Boolean(params.value.status) ||
    Boolean(params.value.greenhouseId) ||
    Boolean(params.value.mine),
)
const greenhouseOptions = computed(() => lookups.data.value?.greenhouses ?? [])
const varietyOptions = computed(() => lookups.data.value?.varieties ?? [])
const workerOptions = computed(() => lookups.data.value?.workers ?? [])
const statuses = Object.entries(STATUS_META) as [BatchStatus, (typeof STATUS_META)[BatchStatus]][]

const badgeTone = (status: BatchStatus) => STATUS_META[status].tone
const statusLabel = (status: BatchStatus) => STATUS_META[status].label

const expectedOf = (row: Batch) => formatDate(row.expectedHarvestDate)

const applyFilter = () => {
  params.value = {
    ...params.value,
    q: searchText.value.trim() || undefined,
    status: statusFilter.value || undefined,
    greenhouseId: greenhouseFilter.value || undefined,
    mine: mineFilter.value || undefined,
    page: 0,
  }
}

const resetFilter = () => {
  searchText.value = ''
  statusFilter.value = ''
  greenhouseFilter.value = ''
  mineFilter.value = false
  params.value = { page: 0, size: params.value.size }
}

const goToPage = (next: number) => {
  params.value = { ...params.value, page: next }
}

const changeSize = (value: unknown) => {
  params.value = { ...params.value, size: Number(value) || 10, page: 0 }
}

const showCreate = ref(false)
const formErrors = ref<Record<string, string>>({})
const generalError = ref('')
const form = reactive<{ greenhouseId: number | ''; varietyId: number | ''; sowDate: string; workerIds: number[] }>({
  greenhouseId: '',
  varietyId: '',
  sowDate: '',
  workerIds: [],
})

const selectedVariety = computed(() => varietyOptions.value.find((item) => item.id === form.varietyId))
const selectedGreenhouse = computed(() => greenhouseOptions.value.find((item) => item.id === form.greenhouseId))
const codePreview = computed(() => {
  if (!selectedGreenhouse.value || !form.sowDate) return '—'
  return previewBatchCode(
    selectedGreenhouse.value.code,
    form.sowDate,
    page.value.content.map((row) => row.batchCode),
  )
})

const openCreate = () => {
  form.greenhouseId = ''
  form.varietyId = ''
  form.sowDate = ''
  form.workerIds = []
  formErrors.value = {}
  generalError.value = ''
  showCreate.value = true
}

const mapProblem = (err: unknown) => {
  const problem = err as Problem
  generalError.value = problem?.detail || 'Đã xảy ra lỗi. Vui lòng thử lại.'
  const mapped: Record<string, string> = {}
  for (const item of problem?.errors ?? []) mapped[item.field] = item.message
  formErrors.value = mapped
}

const submitCreate = () => {
  if (createMutation.isPending.value) return
  formErrors.value = {}
  generalError.value = ''
  const input: BatchCreateInput = {
    greenhouseId: Number(form.greenhouseId) || 0,
    varietyId: Number(form.varietyId) || 0,
    sowDate: form.sowDate,
  }
  createMutation.mutate(input, {
    onSuccess: async (batch) => {
      if (form.workerIds.length > 0) {
        try {
          await assignMutation.mutateAsync({ batchId: batch.id, userIds: form.workerIds })
        } catch {
          showCreate.value = false
          router.push({ name: 'batch-detail', params: { id: batch.id }, query: { warn: 'assign' } })
          return
        }
      }
      showCreate.value = false
      router.push({ name: 'batch-detail', params: { id: batch.id } })
    },
    onError: (err) => mapProblem(err),
  })
}
</script>

<template>
  <div>
    <PageHeader title="Quản lý lô dưa" :breadcrumb="['Trang chủ', 'Quản lý lô dưa']">
      <template #actions>
        <BButton v-if="isEngineer" variant="primary" @click="openCreate">
          <FontAwesomeIcon icon="plus" aria-hidden="true" /> Tạo lô mới
        </BButton>
      </template>
    </PageHeader>

    <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <div class="row g-3 align-items-end">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="bat-search">
            <FontAwesomeIcon icon="magnifying-glass" aria-hidden="true" /> Tìm kiếm
          </label>
          <BFormInput
            id="bat-search"
            v-model="searchText"
            placeholder="Nhập mã lô"
            @keyup.enter="applyFilter"
          />
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="bat-gh">Nhà màng</label>
          <BFormSelect id="bat-gh" v-model="greenhouseFilter">
            <option value="">Tất cả nhà màng</option>
            <option v-for="gh in greenhouseOptions" :key="gh.id" :value="gh.id">{{ gh.code }}</option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="bat-status">Trạng thái</label>
          <BFormSelect id="bat-status" v-model="statusFilter">
            <option value="">Tất cả trạng thái</option>
            <option v-for="[value, meta] in statuses" :key="value" :value="value">{{ meta.label }}</option>
          </BFormSelect>
        </div>
        <div class="col-12 col-md-2">
          <div class="form-check">
            <input id="bat-mine" v-model="mineFilter" class="form-check-input" type="checkbox" />
            <label class="form-check-label small" for="bat-mine">Lô của tôi</label>
          </div>
        </div>
        <div class="col-12 d-flex gap-2">
          <BButton variant="primary" @click="applyFilter">
            <FontAwesomeIcon icon="filter" aria-hidden="true" /> Lọc dữ liệu
          </BButton>
          <BButton variant="outline-secondary" @click="resetFilter">
            <FontAwesomeIcon icon="arrow-rotate-left" aria-hidden="true" /> Đặt lại
          </BButton>
        </div>
      </div>
    </section>

    <section class="border rounded-3 bg-body">
      <div class="p-3 border-bottom d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <p class="mb-0 fw-semibold">Danh sách lô dưa</p>
        <SoftBadge variant="info" :label="`${page.totalElements} lô`" />
      </div>

      <BAlert v-if="isError" variant="danger" class="mx-3 mt-3 mb-0 d-flex gap-2 align-items-center">
        <span>Không tải được danh sách lô: {{ error?.message }}</span>
        <BButton size="sm" variant="outline-danger" @click="refetch()">
          <FontAwesomeIcon icon="rotate" aria-hidden="true" /> Thử lại
        </BButton>
      </BAlert>

      <div v-if="isLoading" class="p-3 d-flex flex-column gap-2">
        <div v-for="n in 5" :key="n" class="bg-body-secondary rounded" style="height: 38px" />
      </div>

      <template v-else-if="page.content.length === 0">
        <div class="text-center text-body-secondary py-5 px-3">
          <template v-if="hasFilters">
            <p class="mb-2"><FontAwesomeIcon icon="magnifying-glass" aria-hidden="true" /> Không tìm thấy lô phù hợp</p>
            <BButton size="sm" variant="outline-secondary" @click="resetFilter">Xóa lọc</BButton>
          </template>
          <template v-else-if="isWorker">
            <p class="mb-0"><FontAwesomeIcon icon="user-group" aria-hidden="true" /> Bạn chưa được giao lô nào</p>
          </template>
          <template v-else>
            <p class="mb-2"><FontAwesomeIcon icon="inbox" aria-hidden="true" /> Chưa có lô nào</p>
            <BButton v-if="isEngineer" size="sm" variant="primary" @click="openCreate">
              <FontAwesomeIcon icon="plus" aria-hidden="true" /> Tạo lô mới
            </BButton>
          </template>
        </div>
      </template>

      <template v-else>
        <div class="table-responsive d-none d-md-block">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th scope="col">Mã lô</th>
                <th scope="col">Nhà màng</th>
                <th scope="col">Giống</th>
                <th scope="col">Ngày xuống giống</th>
                <th scope="col">Thu hoạch dự kiến</th>
                <th scope="col">Trạng thái</th>
                <th scope="col">Công nhân</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in page.content" :key="row.id">
                <td>
                  <RouterLink
                    :to="{ name: 'batch-detail', params: { id: row.id } }"
                    class="fw-semibold text-decoration-none"
                  >
                    {{ row.batchCode }}
                  </RouterLink>
                </td>
                <td>{{ row.greenhouseCode }}</td>
                <td>{{ row.varietyName }}</td>
                <td class="text-nowrap">{{ formatDate(row.sowDate) }}</td>
                <td class="text-nowrap">{{ expectedOf(row) }}</td>
                <td>
                  <SoftBadge :variant="badgeTone(row.status)" :label="statusLabel(row.status)" />
                </td>
                <td><FontAwesomeIcon icon="users" aria-hidden="true" /> {{ row.workerCount }} người</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="d-md-none d-flex flex-column">
          <div v-for="row in page.content" :key="row.id" class="p-3 border-bottom">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <RouterLink
                :to="{ name: 'batch-detail', params: { id: row.id } }"
                class="fw-semibold text-decoration-none"
              >
                {{ row.batchCode }}
              </RouterLink>
              <SoftBadge :variant="badgeTone(row.status)" :label="statusLabel(row.status)" />
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Nhà màng</span><span class="text-body">{{ row.greenhouseCode }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Giống</span><span class="text-body">{{ row.varietyName }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Ngày xuống giống</span><span class="text-body">{{ formatDate(row.sowDate) }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Thu hoạch dự kiến</span><span class="text-body">{{ expectedOf(row) }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Công nhân</span><span class="text-body"><FontAwesomeIcon icon="users" aria-hidden="true" /> {{ row.workerCount }} người</span>
            </div>
          </div>
        </div>
      </template>

      <div class="d-flex flex-wrap gap-2 align-items-center px-3 py-2 border-top">
        <label class="small text-body-secondary mb-0" for="bat-size">
          <FontAwesomeIcon icon="list-check" aria-hidden="true" /> Số dòng
        </label>
        <BFormSelect id="bat-size" :model-value="page.size" class="w-auto" @update:model-value="changeSize">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </BFormSelect>
      </div>

      <PagerBar
        :page="page.page"
        :total-pages="page.totalPages"
        :total-elements="page.totalElements"
        :size="page.size"
        unit="lô"
        @update:page="goToPage"
      />
    </section>

    <BModal v-model="showCreate" title="Tạo lô dưa mới">
      <BAlert v-if="generalError" variant="danger" dismissible @close="generalError = ''">{{ generalError }}</BAlert>
      <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submitCreate">
        <div>
          <label class="form-label small fw-semibold mb-1" for="new-gh">Nhà màng</label>
          <BFormSelect id="new-gh" v-model="form.greenhouseId">
            <option value="">Chọn nhà màng</option>
            <option v-for="gh in greenhouseOptions" :key="gh.id" :value="gh.id">{{ gh.code }} · {{ gh.name }}</option>
          </BFormSelect>
          <p v-if="formErrors.greenhouseId" class="small text-danger mt-1 mb-0">{{ formErrors.greenhouseId }}</p>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="new-variety">Giống dưa</label>
          <BFormSelect id="new-variety" v-model="form.varietyId">
            <option value="">Chọn giống dưa</option>
            <option v-for="v in varietyOptions" :key="v.id" :value="v.id">{{ v.name }}</option>
          </BFormSelect>
          <p v-if="selectedVariety" class="small text-body-secondary mt-1 mb-0">
            Thu hoạch sau thụ phấn: {{ selectedVariety.daysPollinationToHarvest }} ngày
          </p>
          <p v-if="formErrors.varietyId" class="small text-danger mt-1 mb-0">{{ formErrors.varietyId }}</p>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="new-sow">Ngày xuống giống</label>
          <BFormInput id="new-sow" v-model="form.sowDate" type="date" />
          <p v-if="formErrors.sowDate" class="small text-danger mt-1 mb-0">{{ formErrors.sowDate }}</p>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="new-workers">Công nhân phụ trách</label>
          <select id="new-workers" v-model="form.workerIds" class="form-select" multiple size="4">
            <option v-for="w in workerOptions" :key="w.id" :value="w.id">{{ w.fullName }}</option>
          </select>
        </div>
        <div>
          <label class="form-label small fw-semibold mb-1" for="new-code">Mã lô (dự kiến)</label>
          <BFormInput id="new-code" :model-value="codePreview" readonly />
          <p class="small text-body-secondary mt-1 mb-0">dự kiến, mã chính thức do hệ thống cấp</p>
        </div>
      </form>
      <template #footer>
        <BButton variant="outline-secondary" :disabled="createMutation.isPending.value" @click="showCreate = false">
          Hủy
        </BButton>
        <BButton variant="primary" :disabled="createMutation.isPending.value" @click="submitCreate">
          {{ createMutation.isPending.value ? 'Đang lưu...' : 'Lưu' }}
        </BButton>
      </template>
    </BModal>
  </div>
</template>
