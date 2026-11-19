<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SoftBadge from '@/components/common/SoftBadge.vue'
import TaskList from '@/components/batch/TaskList.vue'
import EventTimeline from '@/components/batch/EventTimeline.vue'
import { useBatchQuery, useDeleteBatchMutation, useUpdateBatchMutation } from '@/composables/useBatches'
import { useAssignedWorkersStore, useUserStore, useWorkerLookupStore } from '@/stores'
import {
  ALLOWED_NEXT,
  STATUS_META,
  formatDate,
  type BatchStatus,
  type Problem,
  type WorkerSummary,
} from '@/api/types/batch'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const assignedStore = useAssignedWorkersStore()
const workerStore = useWorkerLookupStore()

const id = computed(() => Number(route.params.id) || 0)
const { data, isLoading, isError, error, refetch } = useBatchQuery(id)

const assigned = computed(() => assignedStore.workers)

const roles = computed(() => userStore.user?.role ?? [])
const isEngineer = computed(() => roles.value.includes('ENGINEER'))
const isFarmer = computed(() => roles.value.includes('FARMER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const canManage = computed(() => isEngineer.value)

const problem = computed(() => error.value as (Error & { status?: number } & Partial<Problem>) | null)
const errorStatus = computed(() => problem.value?.status ?? 0)
const locked = computed(() => data.value?.locked ?? false)
const statusMeta = computed(() => (data.value ? STATUS_META[data.value.status] : null))
const nextStatuses = computed<BatchStatus[]>(() =>
  data.value ? (ALLOWED_NEXT[data.value.status] ?? []) : [],
)

const logOpen = ref(true)
const tasksOpen = ref(true)
const showAdd = ref(false)
const addSelected = ref<number[]>([])
const addError = ref('')
const addPending = ref(false)
const removePending = ref(false)
const workerOptions = computed(() => workerStore.workers)

onMounted(() => {
  if (id.value) assignedStore.load(id.value)
})

watch(id, (value) => {
  if (value) assignedStore.load(value)
})

const openAdd = () => {
  addSelected.value = []
  addError.value = ''
  showAdd.value = true
  workerStore.load()
}

const saveAdd = async () => {
  if (!data.value || addSelected.value.length === 0) return
  addError.value = ''
  addPending.value = true
  try {
    await assignedStore.add(addSelected.value)
    showAdd.value = false
    addSelected.value = []
  } catch (err) {
    addError.value = (err as Problem)?.message || 'Không phân công được. Vui lòng thử lại.'
  } finally {
    addPending.value = false
  }
}

const removeWorker = async (worker: WorkerSummary) => {
  if (!data.value) return
  removePending.value = true
  try {
    await assignedStore.remove(worker.id)
  } finally {
    removePending.value = false
  }
}

const showRemove = ref(false)
const removeTarget = ref<WorkerSummary | null>(null)
const taskListRef = ref<{ openCreate: () => void } | null>(null)

const askRemove = (worker: WorkerSummary) => {
  removeTarget.value = worker
  showRemove.value = true
}

const confirmRemove = async () => {
  if (!removeTarget.value) return
  await removeWorker(removeTarget.value)
  showRemove.value = false
  removeTarget.value = null
}

const showEdit = ref(false)
const editSowDate = ref('')
const editError = ref('')
const showDelete = ref(false)
const deleteError = ref('')
const updateMutation = useUpdateBatchMutation(id)
const deleteMutation = useDeleteBatchMutation()

const openEdit = () => {
  editSowDate.value = data.value?.sowDate?.slice(0, 10) ?? ''
  editError.value = ''
  showEdit.value = true
}

const submitEdit = () => {
  if (!data.value) return
  editError.value = ''
  updateMutation.mutate(
    { sowDate: editSowDate.value, version: data.value.version },
    {
      onSuccess: () => (showEdit.value = false),
      onError: (err) => (editError.value = (err as Problem)?.message || 'Cập nhật thất bại.'),
    },
  )
}

const confirmDelete = () => {
  if (!data.value) return
  deleteError.value = ''
  deleteMutation.mutate(data.value.id, {
    onSuccess: () => router.push({ name: 'batches' }),
    onError: (err) => (deleteError.value = (err as Problem)?.message || 'Xóa thất bại.'),
  })
}
</script>

<template>
  <div>
    <RouterLink :to="{ name: 'batches' }" class="small text-decoration-none d-inline-flex align-items-center gap-1">
      <FontAwesomeIcon icon="arrow-left" aria-hidden="true" /> Danh sách lô
    </RouterLink>

    <div v-if="isLoading" class="d-flex flex-column gap-2 py-3">
      <span class="placeholder placeholder-glow col-8 rounded" style="height: 20px"></span>
      <span class="placeholder placeholder-glow col-12 rounded" style="height: 14px"></span>
      <span class="placeholder placeholder-glow col-10 rounded" style="height: 14px"></span>
      <span class="placeholder placeholder-glow col-6 rounded" style="height: 14px"></span>
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
        <div class="d-flex justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0"><FontAwesomeIcon icon="seedling" aria-hidden="true" /> Thông tin lô</p>
          <div v-if="isEngineer" class="d-flex gap-1">
            <BButton v-if="!locked" size="sm" variant="outline-secondary" aria-label="Sửa lô" @click="openEdit">
              <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" />
            </BButton>
            <BButton v-if="data.status === 'DRAFT'" size="sm" variant="outline-danger" aria-label="Xóa lô" @click="showDelete = true">
              <FontAwesomeIcon icon="trash-can" aria-hidden="true" />
            </BButton>
          </div>
        </div>
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="warehouse" aria-hidden="true" /> Nhà màng</div>
            <div class="fw-semibold">{{ data.greenhouseCode }} · {{ data.greenhouseName }}</div>
          </div>
          <div class="col-12 col-md-6">
            <div class="small text-body-secondary"><FontAwesomeIcon icon="seedling" aria-hidden="true" /> Giống</div>
            <div class="fw-semibold">{{ data.varietyName }}</div>
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
        </div>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0"><FontAwesomeIcon icon="users" aria-hidden="true" /> Công nhân phụ trách</p>
          <BButton
            v-if="canManage && !locked"
            size="sm"
            variant="outline-primary"
            @click="openAdd"
          >
            <FontAwesomeIcon icon="plus" aria-hidden="true" /> Thêm công nhân
          </BButton>
        </div>
        <div class="worker-scroll d-flex flex-wrap gap-2">
          <span v-if="assigned.length === 0" class="small text-body-secondary">
            <FontAwesomeIcon icon="user-group" aria-hidden="true" /> Chưa phân công công nhân
          </span>
          <span
            v-for="u in assigned"
            :key="u.id"
            class="badge bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-semibold d-inline-flex align-items-center gap-1"
          >
            {{ u.fullName }}
            <BButton
              v-if="canManage && !locked"
              size="sm"
              variant="link"
              class="p-0 text-secondary-emphasis text-decoration-none"
              :disabled="removePending"
              :aria-label="`Gỡ ${u.fullName}`"
              @click="askRemove(u)"
            >
              &times;
            </BButton>
          </span>
        </div>
        <BButton
          v-if="assignedStore.hasMore"
          size="sm"
          variant="outline-secondary"
          class="mt-2"
          :disabled="assignedStore.loading"
          @click="assignedStore.loadMore()"
        >
          {{ assignedStore.loading ? 'Đang tải...' : 'Tải thêm' }}
        </BButton>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0">
            <FontAwesomeIcon icon="clock-rotate-left" aria-hidden="true" /> Nhật ký canh tác
            <BButton
              size="sm"
              variant="link"
              class="p-0 ms-1 text-body-secondary"
              :aria-label="logOpen ? 'Thu gọn nhật ký' : 'Mở rộng nhật ký'"
              @click="logOpen = !logOpen"
            >
              <FontAwesomeIcon :icon="logOpen ? 'chevron-up' : 'chevron-down'" aria-hidden="true" />
            </BButton>
          </p>
          <BButton
            v-if="(isEngineer || isFarmer) && !locked"
            size="sm"
            variant="outline-primary"
            @click="router.push({ name: 'batch-log', params: { id } })"
          >
            <FontAwesomeIcon icon="plus" aria-hidden="true" /> Ghi nhật ký
          </BButton>
          <span v-else-if="locked" :title="'Lô đã khóa'">
            <BButton size="sm" variant="outline-secondary" disabled>
              <FontAwesomeIcon icon="plus" aria-hidden="true" /> Ghi nhật ký
            </BButton>
          </span>
        </div>
        <div v-show="logOpen" class="log-scroll">
          <EventTimeline :batch-id="id" />
        </div>
      </section>

      <section class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-2">
          <p class="fw-semibold mb-0">
            <FontAwesomeIcon icon="list-check" aria-hidden="true" /> Công việc
            <BButton
              size="sm"
              variant="link"
              class="p-0 ms-1 text-body-secondary"
              :aria-label="tasksOpen ? 'Thu gọn công việc' : 'Mở rộng công việc'"
              @click="tasksOpen = !tasksOpen"
            >
              <FontAwesomeIcon :icon="tasksOpen ? 'chevron-up' : 'chevron-down'" aria-hidden="true" />
            </BButton>
          </p>
          <BButton v-if="isEngineer && !locked" size="sm" variant="outline-primary" @click="taskListRef?.openCreate()">
            <FontAwesomeIcon icon="plus" aria-hidden="true" /> Giao việc
          </BButton>
        </div>
        <div v-show="tasksOpen" class="log-scroll">
          <TaskList
            ref="taskListRef"
            :batch-id="id"
            :is-engineer="isEngineer"
            :is-farmer="isFarmer"
            :locked="locked"
            :workers="assigned"
          />
        </div>
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

    <BModal v-model="showEdit" title="Sửa lô">
      <template #title>
        <FontAwesomeIcon icon="pen-to-square" aria-hidden="true" /> Sửa lô
      </template>
      <BAlert v-if="editError" variant="danger">{{ editError }}</BAlert>
      <label class="form-label small fw-semibold mb-1" for="edit-sow">Ngày xuống giống</label>
      <BFormInput id="edit-sow" v-model="editSowDate" type="date" />
      <template #footer>
        <BButton variant="outline-secondary" @click="showEdit = false">Hủy</BButton>
        <BButton variant="primary" :disabled="updateMutation.isPending.value" @click="submitEdit">
          {{ updateMutation.isPending.value ? 'Đang lưu...' : 'Lưu' }}
        </BButton>
      </template>
    </BModal>

    <BModal v-model="showDelete" title="Xóa lô">
      <template #title>
        <FontAwesomeIcon icon="trash-can" aria-hidden="true" /> Xóa lô
      </template>
      <BAlert v-if="deleteError" variant="danger">{{ deleteError }}</BAlert>
      <p class="mb-0">Xóa lô <span class="fw-semibold">{{ data?.batchCode }}</span>? Hành động này không thể hoàn tác.</p>
      <template #footer>
        <BButton variant="outline-secondary" @click="showDelete = false">Hủy</BButton>
        <BButton variant="danger" :disabled="deleteMutation.isPending.value" @click="confirmDelete">Xóa</BButton>
      </template>
    </BModal>

    <BModal v-model="showRemove" title="Gỡ công nhân">
      <p class="mb-0">Gỡ {{ removeTarget?.fullName }} khỏi lô?</p>
      <template #footer>
        <BButton variant="outline-secondary" @click="showRemove = false">Hủy</BButton>
        <BButton variant="danger" :disabled="removePending" @click="confirmRemove">Gỡ</BButton>
      </template>
    </BModal>

    <BModal v-model="showAdd" title="Thêm công nhân">
      <template #title>
        <FontAwesomeIcon icon="user-plus" aria-hidden="true" /> Thêm công nhân
      </template>
      <BAlert v-if="addError" variant="danger">{{ addError }}</BAlert>
      <label class="form-label small fw-semibold mb-1" for="add-workers">Công nhân phụ trách</label>
      <select id="add-workers" v-model="addSelected" class="form-select" multiple size="5">
        <option v-for="w in workerOptions" :key="w.id" :value="w.id">{{ w.fullName }}</option>
      </select>
      <BButton
        v-if="workerStore.hasMore"
        size="sm"
        variant="outline-secondary"
        class="mt-2"
        :disabled="workerStore.loading"
        @click="workerStore.loadMore()"
      >
        {{ workerStore.loading ? 'Đang tải...' : 'Tải thêm' }}
      </BButton>
      <template #footer>
        <BButton variant="outline-secondary" :disabled="addPending" @click="showAdd = false">
          Hủy
        </BButton>
        <BButton variant="primary" :disabled="addPending" @click="saveAdd">
          <FontAwesomeIcon icon="plus" aria-hidden="true" />
          {{ addPending ? 'Đang lưu...' : 'Thêm' }}
        </BButton>
      </template>
    </BModal>
  </div>
</template>

<style scoped>
@media (min-width: 992px) {
  .log-scroll {
    max-height: 60vh;
    overflow-y: auto;
  }
  .worker-scroll {
    max-height: 8rem;
    overflow-y: auto;
  }
}
</style>
