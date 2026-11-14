<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SoftBadge from '@/components/common/SoftBadge.vue'
import {
  useAssignWorkersMutation,
  useBatchLookupQuery,
  useBatchQuery,
} from '@/composables/useBatches'
import { useUserStore } from '@/stores'
import {
  ALLOWED_NEXT,
  STATUS_META,
  formatDate,
  isLocked,
  type BatchStatus,
  type Problem,
} from '@/api/types/batch'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = computed(() => Number(route.params.id) || 0)
const { data, isLoading, isError, error, refetch } = useBatchQuery(id)
const lookups = useBatchLookupQuery()
const assignMutation = useAssignWorkersMutation()

const roles = computed(() => userStore.user?.role ?? [])
const isEngineer = computed(() => roles.value.includes('ENGINEER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))

const problem = computed(() => error.value as (Error & Partial<Problem>) | null)
const errorStatus = computed(() => problem.value?.status ?? 0)
const locked = computed(() => (data.value ? isLocked(data.value.status) : false))
const statusMeta = computed(() => (data.value ? STATUS_META[data.value.status] : null))
const nextStatuses = computed<BatchStatus[]>(() =>
  data.value ? (ALLOWED_NEXT[data.value.status] ?? []) : [],
)

const warn = ref(route.query.warn === 'assign')
const clearWarn = () => {
  warn.value = false
  const query = { ...route.query }
  delete query.warn
  router.replace({ query })
}

const showAssign = ref(false)
const selected = ref<number[]>([])
const assignError = ref('')
const workerOptions = computed(() => lookups.data.value?.workers ?? [])

const openAssign = () => {
  selected.value = [...(data.value?.assignedUserIds ?? [])]
  assignError.value = ''
  showAssign.value = true
}

const saveAssign = () => {
  if (!data.value) return
  assignError.value = ''
  assignMutation.mutate(
    { batchId: data.value.id, userIds: selected.value },
    {
      onSuccess: () => {
        showAssign.value = false
        refetch()
      },
      onError: (err) => {
        assignError.value = (err as unknown as Problem)?.detail || 'Không phân công được. Vui lòng thử lại.'
      },
    },
  )
}
</script>

<template>
  <div>
    <RouterLink :to="{ name: 'batches' }" class="small text-decoration-none d-inline-flex align-items-center gap-1">
      <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Danh sách lô
    </RouterLink>

    <div v-if="isLoading" class="d-flex justify-content-center py-5">
      <BSpinner />
    </div>

    <template v-else-if="isError">
      <div v-if="errorStatus === 403" class="text-center py-5">
        <p class="fw-semibold mb-3">Bạn không có quyền xem lô này</p>
        <RouterLink :to="{ name: 'batches' }" class="small d-inline-flex align-items-center gap-1">
          <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Danh sách lô
        </RouterLink>
      </div>
      <div v-else-if="errorStatus === 404" class="text-center py-5">
        <p class="fw-semibold mb-3">Không tìm thấy lô</p>
        <RouterLink :to="{ name: 'batches' }" class="small d-inline-flex align-items-center gap-1">
          <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Danh sách lô
        </RouterLink>
      </div>
      <BAlert v-else variant="danger" class="d-flex gap-2 align-items-center mt-3">
        <FontAwesomeIcon icon="circle-exclamation" aria-hidden="true" />
        <span>Không tải được lô: {{ problem?.message }}</span>
        <BButton size="sm" variant="outline-danger" @click="refetch()">Thử lại</BButton>
      </BAlert>
    </template>

    <template v-else-if="data">
      <BAlert v-if="warn" variant="warning" dismissible @close="clearWarn">
        <FontAwesomeIcon icon="circle-exclamation" aria-hidden="true" />
        Đã tạo lô nhưng chưa phân công được công nhân. Vui lòng thử lại ở trang chi tiết.
      </BAlert>

      <div class="d-flex flex-wrap gap-3 align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">{{ data.batchCode }}</h1>
        <SoftBadge v-if="statusMeta" :variant="statusMeta.tone" :label="statusMeta.label" />
      </div>

      <BAlert v-if="data.status === 'ANCHOR_FAILED'" variant="danger" class="d-flex gap-2 align-items-center">
        <FontAwesomeIcon icon="triangle-exclamation" aria-hidden="true" />
        <span class="fw-semibold">Neo blockchain lỗi</span>
        <BButton v-if="isAdmin" size="sm" variant="outline-danger" disabled>Thử lại</BButton>
      </BAlert>
      <BAlert v-else-if="data.status === 'RECALLED'" variant="danger">
        <FontAwesomeIcon icon="ban" aria-hidden="true" /> Lô đã bị thu hồi
      </BAlert>
      <BAlert v-else-if="locked" variant="warning">
        <FontAwesomeIcon icon="lock" aria-hidden="true" /> Lô đã khóa dữ liệu. Không thể thêm hoặc sửa nhật ký canh tác.
      </BAlert>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="warehouse" aria-hidden="true" /> Nhà màng</div>
            <div class="fw-semibold">{{ data.greenhouse.code }} · {{ data.greenhouse.name }}</div>
          </div>
          <div class="col-12 col-md-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="seedling" aria-hidden="true" /> Giống</div>
            <div class="fw-semibold">{{ data.variety.name }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="calendar-days" aria-hidden="true" /> Ngày xuống giống</div>
            <div>{{ formatDate(data.sowDate) }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="calendar-days" aria-hidden="true" /> Ngày thụ phấn</div>
            <div>{{ formatDate(data.pollinationDate) }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="hourglass-half" aria-hidden="true" /> Thu hoạch dự kiến</div>
            <div>{{ formatDate(data.expectedHarvestDate) }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="box-open" aria-hidden="true" /> Ngày thu hoạch thực tế</div>
            <div>{{ formatDate(data.harvestDate) }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="list-check" aria-hidden="true" /> Số sự kiện</div>
            <div>{{ data.eventCount }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" /> Chờ duyệt</div>
            <div>{{ data.pendingApprovalCount }}</div>
          </div>
        </div>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0"><FontAwesomeIcon icon="users" aria-hidden="true" /> Công nhân phụ trách</p>
          <BButton
            v-if="isEngineer && !locked"
            size="sm"
            variant="outline-primary"
            @click="openAssign"
          >
            <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" /> Phân công
          </BButton>
        </div>
        <div class="d-flex flex-wrap gap-2">
          <span v-if="data.assignedWorkerNames.length === 0" class="small text-body-secondary">
            <FontAwesomeIcon icon="user-group" aria-hidden="true" /> Chưa phân công công nhân
          </span>
          <span
            v-for="name in data.assignedWorkerNames"
            :key="name"
            class="badge bg-primary-subtle text-primary-emphasis border border-primary-subtle fw-semibold"
          >
            {{ name }}
          </span>
        </div>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0"><FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" /> Nhật ký canh tác</p>
          <span v-if="locked" :title="'Lô đã khóa'">
            <BButton size="sm" variant="outline-secondary" disabled>
              <FontAwesomeIcon icon="plus" aria-hidden="true" /> Thêm sự kiện
            </BButton>
          </span>
          <BButton v-else size="sm" variant="outline-secondary" disabled>
            <FontAwesomeIcon icon="plus" aria-hidden="true" /> Thêm sự kiện
          </BButton>
        </div>
        <p class="small text-body-secondary mb-0">Nhật ký canh tác sẽ hiển thị ở đây</p>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4">
        <p class="fw-semibold mb-2"><FontAwesomeIcon icon="shield-halved" aria-hidden="true" /> Hành động</p>
        <div class="d-flex flex-wrap gap-2">
          <span v-for="status in nextStatuses" :key="status" :title="'Thuộc module khác'">
            <BButton size="sm" variant="outline-secondary" disabled>
              {{ STATUS_META[status].label }}
            </BButton>
          </span>
          <span v-if="nextStatuses.length === 0" class="small text-body-secondary">
            Không có hành động khả dụng
          </span>
        </div>
      </section>
    </template>

    <BModal v-model="showAssign" title="Phân công công nhân">
      <template #title>
        <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" /> Phân công công nhân
      </template>
      <BAlert v-if="assignError" variant="danger">{{ assignError }}</BAlert>
      <label class="form-label small fw-semibold mb-1" for="assign-workers">Công nhân phụ trách</label>
      <select id="assign-workers" v-model="selected" class="form-select" multiple size="5">
        <option v-for="w in workerOptions" :key="w.id" :value="w.id">{{ w.fullName }}</option>
      </select>
      <template #footer>
        <BButton variant="outline-secondary" :disabled="assignMutation.isPending.value" @click="showAssign = false">
          Hủy
        </BButton>
        <BButton variant="primary" :disabled="assignMutation.isPending.value" @click="saveAssign">
          <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" />
          {{ assignMutation.isPending.value ? 'Đang lưu...' : 'Lưu' }}
        </BButton>
      </template>
    </BModal>
  </div>
</template>
