<script setup lang="ts">
import { computed, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SoftBadge from '@/components/common/SoftBadge.vue'
import {
  useCreateShipmentMutation,
  useEligibleBatchesQuery,
  useReceiveShipmentMutation,
  useShipmentsByBatchQuery,
  useShipmentsByStatusQuery,
} from '@/composables/useShipments'
import { useUserStore } from '@/stores'
import {
  formatDateTime,
  shipmentStatusLabel,
  shipmentStatusTone,
  type Shipment,
} from '@/api/types/shipment'

const userStore = useUserStore()
const roles = computed(() => userStore.user?.role ?? [])
const canShip = computed(() => roles.value.some((role) => ['PACKER', 'ADMIN'].includes(role)))
const canReceive = computed(() => roles.value.some((role) => ['PARTNER_QC', 'ADMIN'].includes(role)))

const friendlyError = (err: unknown): string => {
  const raw = err instanceof Error ? err.message : ''
  const map: Record<string, string> = {
    'Batch not found': 'Không tìm thấy lô dưa.',
    'Batch is not eligible for shipping': 'Lô không đủ điều kiện xuất kho (lô đã bị thu hồi hoặc không ở trạng thái ACTIVE).',
    'Batch already has a shipment in progress': 'Lô này đang có phiếu xuất kho chưa hoàn tất, không thể tạo thêm.',
    'Shipment not found': 'Không tìm thấy phiếu xuất kho.',
    'Shipment already processed': 'Phiếu xuất kho này đã được xử lý trước đó.',
    'Condition note is required when rejecting a shipment': 'Từ chối nhận hàng phải kèm ghi chú lý do.',
  }
  return map[raw] ?? (raw || 'Đã xảy ra lỗi. Vui lòng thử lại.')
}

// ---------- Xuất kho ----------
const eligible = useEligibleBatchesQuery(() => canShip.value)
const batchOptions = computed(() => eligible.data.value ?? [])

const outbound = ref({
  batchId: '' as number | '',
  destination: '',
  vehiclePlate: '',
  quantity: '' as number | '',
  shippedAt: '',
})
const outboundErrors = ref<Record<string, string>>({})
const outboundError = ref('')
const outboundSuccess = ref('')

const selectedBatchId = computed<number | null>(() =>
  outbound.value.batchId === '' ? null : Number(outbound.value.batchId),
)
const selectedBatch = computed(() =>
  batchOptions.value.find((batch) => batch.id === selectedBatchId.value),
)
const batchHistory = useShipmentsByBatchQuery(selectedBatchId)

const createMutation = useCreateShipmentMutation()

const submitOutbound = () => {
  if (createMutation.isPending.value) return
  outboundErrors.value = {}
  outboundError.value = ''
  outboundSuccess.value = ''

  const batchId = Number(outbound.value.batchId)
  const quantity = Number(outbound.value.quantity)
  if (!batchId) outboundErrors.value.batchId = 'Chọn lô cần xuất kho'
  if (!outbound.value.destination.trim()) outboundErrors.value.destination = 'Nhập điểm đến'
  if (!outbound.value.vehiclePlate.trim()) outboundErrors.value.vehiclePlate = 'Nhập biển số xe'
  if (!Number.isFinite(quantity) || quantity <= 0) outboundErrors.value.quantity = 'Số lượng phải lớn hơn 0'
  if (Object.keys(outboundErrors.value).length > 0) return

  createMutation.mutate(
    {
      batchId,
      input: {
        destination: outbound.value.destination.trim(),
        vehiclePlate: outbound.value.vehiclePlate.trim(),
        quantity,
        shippedAt: outbound.value.shippedAt ? new Date(outbound.value.shippedAt).getTime() : undefined,
      },
    },
    {
      onSuccess: () => {
        outboundSuccess.value = 'Đã ghi xuất kho thành công, lô đã chuyển sang trạng thái Đang giao.'
        outbound.value = { batchId: '', destination: '', vehiclePlate: '', quantity: '', shippedAt: '' }
      },
      onError: (err) => {
        outboundError.value = friendlyError(err)
      },
    },
  )
}

// ---------- Nhận hàng ----------
const statusFilter = ref<string>('SHIPPED')
const shipments = useShipmentsByStatusQuery(statusFilter)

const statusOptions: { value: string; label: string }[] = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'RECEIVED', label: 'Đã nhận hàng' },
  { value: 'REJECTED', label: 'Bị từ chối' },
]

const showReceive = ref(false)
const receiveMode = ref<'accepted' | 'rejected'>('accepted')
const receiving = ref<Shipment | null>(null)
const receiveForm = ref({ receivedAt: '', conditionNote: '', photoUrls: '' })
const receiveError = ref('')
const receiveSuccess = ref('')

const receiveMutation = useReceiveShipmentMutation()
const pendingReceive = computed(() => receiveMutation.isPending.value)

const openReceive = (row: Shipment, mode: 'accepted' | 'rejected') => {
  receiving.value = row
  receiveMode.value = mode
  receiveForm.value = { receivedAt: '', conditionNote: '', photoUrls: '' }
  receiveError.value = ''
  showReceive.value = true
}

const submitReceive = () => {
  if (receiveMutation.isPending.value || !receiving.value) return
  receiveError.value = ''

  if (receiveMode.value === 'rejected' && !receiveForm.value.conditionNote.trim()) {
    receiveError.value = 'Phải nhập ghi chú lý do khi từ chối nhận hàng'
    return
  }

  const photoUrls = receiveForm.value.photoUrls
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)

  receiveMutation.mutate(
    {
      shipmentId: receiving.value.id,
      input: {
        accepted: receiveMode.value === 'accepted',
        receivedAt: receiveForm.value.receivedAt
          ? new Date(receiveForm.value.receivedAt).getTime()
          : undefined,
        conditionNote: receiveForm.value.conditionNote.trim() || undefined,
        photoUrls: photoUrls.length > 0 ? photoUrls : undefined,
      },
    },
    {
      onSuccess: () => {
        showReceive.value = false
        receiveSuccess.value =
          receiveMode.value === 'accepted'
            ? 'Đã xác nhận nhận hàng thành công.'
            : 'Đã từ chối nhận hàng, lô đã quay về trạng thái ACTIVE.'
      },
      onError: (err) => {
        receiveError.value = friendlyError(err)
      },
    },
  )
}
</script>

<template>
  <div>
    <PageHeader
      title="Xuất kho & Giao hàng"
      subtitle="Ghi xuất kho và xác nhận nhận hàng từ đối tác"
      :breadcrumb="['Vận hành', 'Xuất kho']"
    >
      <template #actions>
        <SoftBadge v-if="canShip" variant="info" label="Xuất kho" />
        <SoftBadge v-if="canReceive" variant="success" label="Nhận hàng" />
      </template>
    </PageHeader>

    <section v-if="canShip" class="border rounded-3 bg-body p-3 p-lg-4 mb-3">
      <p class="fw-semibold mb-3 d-flex align-items-center gap-2">
        <FontAwesomeIcon icon="warehouse" aria-hidden="true" /> Ghi xuất kho
      </p>

      <BAlert v-if="outboundError" variant="danger" dismissible @close="outboundError = ''">
        {{ outboundError }}
      </BAlert>
      <BAlert v-if="outboundSuccess" variant="success" dismissible @close="outboundSuccess = ''">
        {{ outboundSuccess }}
      </BAlert>

      <form class="row g-3" novalidate @submit.prevent="submitOutbound">
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="shp-batch">
            Lô dưa (ACTIVE)
          </label>
          <BFormSelect
            id="shp-batch"
            v-model="outbound.batchId"
            :disabled="eligible.isPending.value || createMutation.isPending.value"
          >
            <option value="">Chọn lô cần xuất</option>
            <option v-for="batch in batchOptions" :key="batch.id" :value="batch.id">
              {{ batch.batchCode }} · {{ batch.varietyName ?? '—' }}
            </option>
          </BFormSelect>
          <p v-if="outboundErrors.batchId" class="small text-danger mt-1 mb-0">{{ outboundErrors.batchId }}</p>
          <p v-else-if="selectedBatch" class="small text-body-secondary mt-1 mb-0">
            {{ selectedBatch.greenhouseName ?? '—' }} · {{ selectedBatch.greenhouseCode ?? '' }}
          </p>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="shp-dest">Điểm đến</label>
          <BFormInput
            id="shp-dest"
            v-model="outbound.destination"
            placeholder="VD: Cửa hàng trái cây An Bình, TP.HCM"
            :disabled="createMutation.isPending.value"
          />
          <p v-if="outboundErrors.destination" class="small text-danger mt-1 mb-0">{{ outboundErrors.destination }}</p>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="shp-plate">Biển số xe</label>
          <BFormInput
            id="shp-plate"
            v-model="outbound.vehiclePlate"
            placeholder="VD: 51F-123.45"
            :disabled="createMutation.isPending.value"
          />
          <p v-if="outboundErrors.vehiclePlate" class="small text-danger mt-1 mb-0">{{ outboundErrors.vehiclePlate }}</p>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="shp-qty">Số lượng (kg)</label>
          <BFormInput
            id="shp-qty"
            v-model="outbound.quantity"
            type="number"
            min="1"
            step="1"
            placeholder="VD: 120"
            :disabled="createMutation.isPending.value"
          />
          <p v-if="outboundErrors.quantity" class="small text-danger mt-1 mb-0">{{ outboundErrors.quantity }}</p>
        </div>
        <div class="col-12 col-md-4">
          <label class="form-label small fw-semibold text-body-secondary mb-1" for="shp-time">Thời gian xuất kho</label>
          <BFormInput
            id="shp-time"
            v-model="outbound.shippedAt"
            type="datetime-local"
            :disabled="createMutation.isPending.value"
          />
          <p class="small text-body-secondary mt-1 mb-0">để trống sẽ lấy thời điểm hiện tại</p>
        </div>
        <div class="col-12">
          <BButton
            variant="primary"
            :disabled="eligible.isPending.value || createMutation.isPending.value"
            @click="submitOutbound"
          >
            <FontAwesomeIcon v-if="createMutation.isPending.value" icon="rotate" spin aria-hidden="true" />
            {{ createMutation.isPending.value ? 'Đang lưu...' : 'Ghi xuất kho' }}
          </BButton>
        </div>
      </form>
    </section>

    <section v-if="selectedBatchId != null" class="border rounded-3 bg-body mb-3">
      <div class="p-3 border-bottom d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <p class="mb-0 fw-semibold">Lịch sử xuất kho của lô</p>
        <SoftBadge
          v-if="batchHistory.data.value?.length"
          variant="info"
          :label="`${batchHistory.data.value.length} phiếu`"
        />
      </div>

      <div v-if="batchHistory.isPending.value" class="p-3 d-flex flex-column gap-2">
        <div v-for="n in 3" :key="n" class="bg-body-secondary rounded" style="height: 38px" />
      </div>

      <div v-else-if="!batchHistory.data.value?.length" class="text-center text-body-secondary py-4 px-3">
        <p class="mb-0"><FontAwesomeIcon icon="inbox" aria-hidden="true" /> Lô này chưa có phiếu xuất kho nào</p>
      </div>

      <template v-else>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th scope="col">Mã phiếu</th>
                <th scope="col">Điểm đến</th>
                <th scope="col">Biển số</th>
                <th scope="col">Số lượng</th>
                <th scope="col">Xuất kho</th>
                <th scope="col">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in batchHistory.data.value" :key="row.id">
                <td class="fw-semibold">#{{ row.id }}</td>
                <td>{{ row.destination }}</td>
                <td>{{ row.vehiclePlate }}</td>
                <td>{{ row.quantity ?? '—' }} kg</td>
                <td class="text-nowrap">{{ formatDateTime(row.shippedAt) }}</td>
                <td>
                  <SoftBadge :variant="shipmentStatusTone(row.status)" :label="shipmentStatusLabel(row.status)" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </section>

    <section v-if="canReceive" class="border rounded-3 bg-body">
      <div class="p-3 border-bottom d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <p class="mb-0 fw-semibold d-flex align-items-center gap-2">
          <FontAwesomeIcon icon="list-check" aria-hidden="true" /> Xác nhận nhận hàng
        </p>
        <div class="d-flex align-items-center gap-2">
          <label class="small fw-semibold text-body-secondary mb-0" for="shp-status">Trạng thái</label>
          <BFormSelect id="shp-status" v-model="statusFilter" class="w-auto">
            <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </BFormSelect>
        </div>
      </div>

      <BAlert
        v-if="receiveSuccess"
        variant="success"
        dismissible
        class="mx-3 mt-3 mb-0"
        @close="receiveSuccess = ''"
      >
        {{ receiveSuccess }}
      </BAlert>

      <div v-if="shipments.isPending.value" class="p-3 d-flex flex-column gap-2">
        <div v-for="n in 5" :key="n" class="bg-body-secondary rounded" style="height: 38px" />
      </div>

      <div v-else-if="!shipments.data.value?.length" class="text-center text-body-secondary py-5 px-3">
        <p class="mb-0"><FontAwesomeIcon icon="inbox" aria-hidden="true" /> Không có phiếu xuất kho nào</p>
      </div>

      <template v-else>
        <div class="table-responsive d-none d-md-block">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th scope="col">Mã phiếu</th>
                <th scope="col">Mã lô</th>
                <th scope="col">Điểm đến</th>
                <th scope="col">Biển số</th>
                <th scope="col">Số lượng</th>
                <th scope="col">Xuất kho</th>
                <th scope="col">Trạng thái</th>
                <th scope="col">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in shipments.data.value" :key="row.id">
                <td class="fw-semibold">#{{ row.id }}</td>
                <td>{{ row.batchCode ?? '—' }}</td>
                <td>{{ row.destination }}</td>
                <td>{{ row.vehiclePlate }}</td>
                <td>{{ row.quantity ?? '—' }} kg</td>
                <td class="text-nowrap">{{ formatDateTime(row.shippedAt) }}</td>
                <td>
                  <SoftBadge :variant="shipmentStatusTone(row.status)" :label="shipmentStatusLabel(row.status)" />
                </td>
                <td>
                  <div v-if="row.status === 'SHIPPED'" class="d-flex gap-2">
                    <BButton size="sm" variant="success" @click="openReceive(row, 'accepted')">
                      <FontAwesomeIcon icon="list-check" aria-hidden="true" /> Xác nhận
                    </BButton>
                    <BButton size="sm" variant="outline-danger" @click="openReceive(row, 'rejected')">
                      <FontAwesomeIcon icon="ban" aria-hidden="true" /> Từ chối
                    </BButton>
                  </div>
                  <span v-else class="small text-body-secondary">{{ formatDateTime(row.receivedAt) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="d-md-none d-flex flex-column">
          <div v-for="row in shipments.data.value" :key="row.id" class="p-3 border-bottom">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <span class="fw-semibold">#{{ row.id }} · {{ row.batchCode ?? '—' }}</span>
              <SoftBadge :variant="shipmentStatusTone(row.status)" :label="shipmentStatusLabel(row.status)" />
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Điểm đến</span><span class="text-body">{{ row.destination }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Biển số</span><span class="text-body">{{ row.vehiclePlate }}</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Số lượng</span><span class="text-body">{{ row.quantity ?? '—' }} kg</span>
            </div>
            <div class="small text-body-secondary d-flex justify-content-between">
              <span>Xuất kho</span><span class="text-body">{{ formatDateTime(row.shippedAt) }}</span>
            </div>
            <div v-if="row.status === 'SHIPPED'" class="d-flex gap-2 mt-2">
              <BButton size="sm" variant="success" @click="openReceive(row, 'accepted')">
                <FontAwesomeIcon icon="list-check" aria-hidden="true" /> Xác nhận
              </BButton>
              <BButton size="sm" variant="outline-danger" @click="openReceive(row, 'rejected')">
                <FontAwesomeIcon icon="ban" aria-hidden="true" /> Từ chối
              </BButton>
            </div>
            <div v-else class="small text-body-secondary mt-2">
              {{ row.conditionNote ? `Ghi chú: ${row.conditionNote}` : `Nhận lúc ${formatDateTime(row.receivedAt)}` }}
            </div>
          </div>
        </div>
      </template>
    </section>

    <BModal
      v-model="showReceive"
      :title="receiveMode === 'accepted' ? 'Xác nhận nhận hàng' : 'Từ chối nhận hàng'"
    >
      <template v-if="receiving">
        <p class="small text-body-secondary mb-3">
          Phiếu #{{ receiving.id }} · Lô {{ receiving.batchCode ?? '—' }} · {{ receiving.destination }}
        </p>
        <BAlert v-if="receiveError" variant="danger" dismissible @close="receiveError = ''">
          {{ receiveError }}
        </BAlert>
        <form class="d-flex flex-column gap-3" novalidate @submit.prevent="submitReceive">
          <div>
            <label class="form-label small fw-semibold mb-1" for="rcv-time">Thời gian nhận hàng</label>
            <BFormInput id="rcv-time" v-model="receiveForm.receivedAt" type="datetime-local" />
            <p class="small text-body-secondary mt-1 mb-0">để trống sẽ lấy thời điểm hiện tại</p>
          </div>
          <div>
            <label class="form-label small fw-semibold mb-1" for="rcv-note">
              Ghi chú <span v-if="receiveMode === 'rejected'" class="text-danger">*</span>
            </label>
            <BFormTextarea
              id="rcv-note"
              v-model="receiveForm.conditionNote"
              rows="3"
              :placeholder="
                receiveMode === 'rejected'
                  ? 'VD: Hàng bị dập, không đạt chất lượng'
                  : 'VD: Hàng nguyên vẹn, đủ số lượng'
              "
            />
          </div>
          <div>
            <label class="form-label small fw-semibold mb-1" for="rcv-photos">Ảnh minh chứng (mỗi dòng một URL)</label>
            <BFormTextarea id="rcv-photos" v-model="receiveForm.photoUrls" rows="2" placeholder="https://..." />
          </div>
        </form>
      </template>
      <template #footer>
        <BButton variant="outline-secondary" :disabled="pendingReceive" @click="showReceive = false">
          Hủy
        </BButton>
        <BButton
          :variant="receiveMode === 'accepted' ? 'success' : 'danger'"
          :disabled="pendingReceive"
          @click="submitReceive"
        >
          <FontAwesomeIcon v-if="pendingReceive" icon="rotate" spin aria-hidden="true" />
          {{
            pendingReceive
              ? 'Đang xử lý...'
              : receiveMode === 'accepted'
                ? 'Xác nhận đã nhận'
                : 'Từ chối nhận hàng'
          }}
        </BButton>
      </template>
    </BModal>
  </div>
</template>
