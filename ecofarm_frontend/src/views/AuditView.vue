<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { useAuditListStore } from '@/stores'
import type { AuditLog } from '@/api/types/audit'

const store = useAuditListStore()
const selected = ref<AuditLog | null>(null)
const showDetail = ref(false)

const openDetail = (row: AuditLog) => {
  selected.value = row
  showDetail.value = true
}

onMounted(() => store.load(0, true))
</script>

<template>
  <div>
    <PageHeader title="Quản lý audit" />
    <p class="text-body-secondary small">Nhật ký kiểm toán chỉ đọc. Ngày giờ được hiển thị và lọc theo múi giờ trên thiết bị.</p>

    <form class="border rounded-3 bg-body p-3 p-lg-4 mb-3" novalidate @submit.prevent="store.applyFilters">
      <fieldset :disabled="store.loading">
        <legend class="h6">Bộ lọc nhật ký</legend>
        <div class="row g-3">
          <div class="col-12 col-md-4">
            <label for="audit-actor" class="form-label small fw-semibold">ID người thực hiện</label>
            <input id="audit-actor" v-model="store.form.actorId" class="form-control" inputmode="numeric" placeholder="Số nguyên dương" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-action" class="form-label small fw-semibold">Hành động</label>
            <input id="audit-action" v-model="store.form.action" class="form-control" placeholder="VD: CREATE" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-entity" class="form-label small fw-semibold">Đối tượng</label>
            <input id="audit-entity" v-model="store.form.entity" class="form-control" placeholder="Tên loại đối tượng" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-entity-id" class="form-label small fw-semibold">ID đối tượng</label>
            <input id="audit-entity-id" v-model="store.form.entityId" class="form-control" inputmode="numeric" placeholder="Số nguyên dương" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-from" class="form-label small fw-semibold">Từ thời điểm</label>
            <input id="audit-from" v-model="store.form.from" class="form-control" type="datetime-local" />
          </div>
          <div class="col-12 col-md-4">
            <label for="audit-to" class="form-label small fw-semibold">Đến thời điểm</label>
            <input id="audit-to" v-model="store.form.to" class="form-control" type="datetime-local" />
          </div>
          <div class="col-12 d-flex gap-2">
            <button class="btn btn-primary" type="submit">Lọc dữ liệu</button>
            <button class="btn btn-outline-secondary" type="button" @click="store.reset">Đặt lại</button>
          </div>
        </div>
      </fieldset>
      <div v-if="store.validationError" class="alert alert-danger mt-3 mb-0" role="alert">{{ store.validationError }}</div>
    </form>

    <section class="border rounded-3 bg-body" :aria-busy="store.loading">
      <div class="p-3 border-bottom d-flex flex-wrap justify-content-between gap-2">
        <h2 class="h6 mb-0">Nhật ký kiểm toán</h2>
        <span class="small text-body-secondary">{{ store.rows.length }} bản ghi trên trang · Tổng theo bộ lọc: {{ store.total == null ? '—' : store.total.toLocaleString('vi-VN') }}</span>
      </div>
      <div v-if="store.error || store.countError" class="alert alert-danger m-3" role="alert">
        <p v-if="store.error" class="mb-1">{{ store.error }}</p>
        <p v-if="store.countError" class="mb-1">{{ store.countError }}</p>
        <button class="btn btn-sm btn-outline-danger" :disabled="store.loading" @click="store.load(store.pageIndex, true)">Thử lại</button>
      </div>
      <div v-if="store.loading" class="text-center p-4" role="status"><BSpinner small /> Đang tải nhật ký...</div>
      <div v-else-if="!store.rows.length && !store.error" class="text-center text-body-secondary p-5">Không có nhật ký phù hợp.</div>
      <div v-else-if="store.rows.length" class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Thời gian</th>
              <th scope="col">Người thực hiện</th>
              <th scope="col">Hành động</th>
              <th scope="col">Đối tượng / ID</th>
              <th scope="col">IP</th>
              <th scope="col">Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in store.rows" :key="row.id">
              <td class="text-nowrap">{{ store.formatTime(row.createdAt) }}</td>
              <td>
                <div>{{ row.actorName || row.actorEmail || 'Không xác định' }}</div>
                <div v-if="row.actorName && row.actorEmail" class="small text-body-secondary">{{ row.actorEmail }}</div>
                <div class="small text-body-secondary">ID: {{ row.actorId ?? '—' }}</div>
              </td>
              <td class="text-break">{{ row.action }}</td>
              <td class="text-break">{{ row.entity }} · {{ row.entityId === 0 ? 'Tập hợp / không có ID số' : `#${row.entityId}` }}</td>
              <td>{{ row.ipAddr || '—' }}</td>
              <td><button class="btn btn-sm btn-outline-secondary text-nowrap" @click="openDetail(row)">Xem chi tiết</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="p-3 border-top d-flex flex-wrap gap-2 justify-content-between align-items-center">
        <button class="btn btn-sm btn-outline-secondary" :disabled="store.loading || !store.hasPrev" @click="store.load(0)">Trang đầu</button>
        <button class="btn btn-sm btn-outline-secondary" :disabled="store.loading || !store.hasPrev" @click="store.load(store.pageIndex - 1)">Trang trước</button>
        <span class="small text-body-secondary">Trang {{ store.pageIndex + 1 }}</span>
        <button class="btn btn-sm btn-outline-secondary" :disabled="store.loading || !store.hasMore" @click="store.load(store.pageIndex + 1)">Trang sau</button>
      </div>
    </section>

    <BModal v-model="showDetail" title="Chi tiết nhật ký kiểm toán" size="lg" @hidden="selected = null">
      <template v-if="selected">
        <p class="small text-body-secondary">#{{ selected.id }} · {{ store.formatTime(selected.createdAt) }} · {{ selected.action }} · {{ selected.entity }}</p>
        <pre class="audit-detail border rounded bg-body-tertiary p-3 mb-0">{{ selected.detail || 'Không có nội dung chi tiết.' }}</pre>
      </template>
      <template #footer><BButton variant="secondary" @click="showDetail = false">Đóng</BButton></template>
    </BModal>
  </div>
</template>

<style scoped>
.audit-detail {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  max-height: 60vh;
  overflow: auto;
}
</style>
