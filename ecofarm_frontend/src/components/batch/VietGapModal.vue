<script setup lang="ts">
defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()
</script>

<template>
  <BModal
    :model-value="modelValue"
    size="lg"
    title="Quy chuẩn VietGAP - Trồng & Thu hoạch Dưa lưới"
    ok-only
    ok-title="Đóng"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="d-flex flex-column gap-3 small">
      <div class="p-3 bg-body-secondary rounded-3 border">
        <h6 class="fw-bold text-success-emphasis mb-2">Tiêu chuẩn Quốc gia TCVN 11892-1:2017 (VietGAP)</h6>
        <p class="mb-0 text-body-secondary">
          Áp dụng cho quy trình thực hành nông nghiệp tốt trong trồng trọt dưa lưới nhà màng công nghệ cao tại EcoFarm.
        </p>
      </div>

      <div class="table-responsive">
        <table class="table table-bordered table-sm mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 25%;">Chỉ tiêu</th>
              <th style="width: 35%;">Yêu cầu VietGAP</th>
              <th style="width: 40%;">Thực tế lô dưa</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="fw-semibold">Độ ngọt Brix</td>
              <td>≥ 13.5° Bx</td>
              <td><span class="badge bg-success">14.2° Bx</span> (Vượt tiêu chuẩn)</td>
            </tr>
            <tr>
              <td class="fw-semibold">Thời gian cách ly (PHI)</td>
              <td>Tối thiểu 14 ngày</td>
              <td><span class="badge bg-success">17 ngày</span> (Cách ly an toàn 100%)</td>
            </tr>
            <tr>
              <td class="fw-semibold">Dư lượng thuốc BVTV</td>
              <td>Không vượt ngưỡng MRLs</td>
              <td><span class="badge bg-success">Âm tính / Không phát hiện</span></td>
            </tr>
            <tr>
              <td class="fw-semibold">Nguồn nước tưới</td>
              <td>QCVN 08-MT:2015/BTNMT</td>
              <td>Hệ thống lọc RO khử khuẩn UV</td>
            </tr>
            <tr>
              <td class="fw-semibold">Tính bất biến nhật ký</td>
              <td>Lưu trữ không thể sửa đổi</td>
              <td><span class="badge bg-primary">Neo Merkle Polygon & Besu</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </BModal>
</template>
