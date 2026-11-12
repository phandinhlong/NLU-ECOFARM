# KẾ HOẠCH TRIỂN KHAI — SHP (Xuất kho, giao hàng) & VER (Tra cứu, xác thực)

**Chủ sở hữu:** 1 người (Phạm vi duy nhất: **SHP** + **VER**)  
**Bắt đầu:** 12/11/2026 · **Hạn chót:** 18/11/2026 · **Cập nhật:** 12/11/2026  
**Phạm vi kỹ thuật:** `ecofarm_backend` (Jakarta EE 10 / WildFly / Maven WAR / Liquibase) + `ecofarm_frontend` (Vue 3 / Vite)

> **Bản kế hoạch hoàn chỉnh (v2.1):** Chỉ phân công việc cho **một mình anh**. Toàn bộ chức năng thuộc lane khác (`AUTH`, `MST`, `BAT`, `LOG`, `QC`, `PCK`, `LDG`, `RPT`) **không nằm trong kế hoạch này** — chỉ được nêu ở dạng **hợp đồng giao tiếp (interface contract)** để biết cần gì, cần ai, cần trước hạn nào, kèm theo **phương án dự phòng (fallback)** để anh luôn chủ động hoàn thành và demo được đúng hạn 18/11 ngay cả khi thành viên khác bị trễ.

---

## 0. Các quyết định cần chốt ngay (5 phút, đọc mục 1 trước khi code)

| # | Câu hỏi | Khuyến nghị thực thi | Phương án tự xử lý nếu nhóm chưa chốt kịp |
|---|---|---|---|
| **Q1** | Frontend nền (`vue-router` + `axios` + `pinia` + đăng nhập) do ai làm? | Người phụ trách FE chung làm, **cần có trước 13/11** | Tự dựng bản tối thiểu trong nhánh riêng, chỉ dùng `vue-router` + `fetch` API |
| **Q2** | Ai đăng ký quy tắc hash/Merkle (`ADR-001`)? | Anh viết, **trưởng nhóm + người làm PCK/LDG ký** trước 13/11 | Anh tự ký tạm, ghi rõ trong biên bản họp và cung cấp sẵn package `common.crypto` cho cả nhóm import |
| **Q3** | Ai đổi `batches.salt_enc` từ `bigint` → hex 64 ký tự (`varchar(64)`)? | Người làm `BAT` đổi, **anh cung cấp `SaltUtil`** | Tự thêm changeset `ALTER` (có rủi ro conflict → ghi chú rõ trong PR và báo nhóm) |
| **Q4** | Có deploy được contract `BlockchainAnchoring.sol` lên Polygon Amoy trước 16/11 không? | Nếu không, chấp nhận đọc root từ `tx.input` | Cài sẵn cơ chế đọc kép: ưu tiên gọi contract, tự động fallback đọc `tx.input` (`ADR-002`) |
| **Q5** | Người làm `LOG`/`QC` có giao bảng `cultivation_events` + `qc_records` trước 16/11 không? | Nếu không, `VER` dùng fallback `outbox_events` | Dựng sẵn fallback đọc `outbox_events` (bảng này đã có sẵn trong repo) |

---

## 1. Phạm vi công việc & Yêu cầu kỹ thuật

### 1.1 Việc CỦA ANH (Toàn bộ phạm vi kế hoạch này)

| Nhóm | Mã FR / NFR | UC | Nội dung chi tiết |
|---|---|---|---|
| **SHP** | `FR-SHP-001` | UC-15 | Ghi xuất kho: điểm đến, biển số xe, thời gian, số lượng; chuyển lô `ACTIVE → SHIPPED` (sai trạng thái trả `409 Conflict` theo `FR-BAT-004`). |
| **SHP** | `FR-SHP-002` | UC-16 | Đối tác (QC đối tác) xác nhận nhận hàng: thời gian, tình trạng, đính kèm ảnh; trạng thái `RECEIVED` / `REJECTED` (nếu từ chối thì trả lô về `ACTIVE` để giao lại). |
| **VER** | `FR-VER-001` | UC-17 | `GET /api/v1/public/trace/{code}` → hành trình rút gọn (giống, nhà màng, mốc ngày chính, Brix, cân nặng, chứng nhận, trạng thái), **tuyệt đối không có PII hay sản lượng** (`NFR-PRV-001`). Kết hợp đếm số lần quét mã (`FR-PCK-006`). |
| **VER** | `FR-VER-002` | UC-17 | Hiển thị timeline dọc, thẻ chỉ số, trạng thái xác thực; cảnh báo rõ khi lô chưa `ACTIVE` hoặc đã bị thu hồi (`RECALLED` - `FR-AUD-003`). |
| **VER** | `FR-VER-003` | UC-18 | `POST /api/v1/public/trace/{code}/verify` → dựng lại Merkle Root và so với Polygon; trả `MATCH` / `MISMATCH` / `UNAVAILABLE`. |
| **VER** | `FR-VER-004` | UC-18 | Trả và hiển thị `txHash`, số `block`, thời điểm neo (`anchoredAt`) và link trực tiếp sang `PolygonScan`. |
| **VER** | `FR-VER-005` | UC-17/18 | Không yêu cầu cài ví hay đăng nhập; giới hạn tần suất (Rate Limit) theo IP (`429 Too Many Requests` + `Retry-After` - `NFR-SEC-005`). |
| **VER** | `FR-VER-006` | UC-18 | `GET /api/v1/batches/{id}/proof-bundle` cho QC đối tác/Thanh tra (cần đăng nhập): sự kiện đã khử PII (`actorRef`), `salt`, các `leaves`, `Merkle proof`, `anchor tx` + script tự kiểm độc lập (`scripts/verify_proof.py`). |
| **VER** | `FR-VER-007` | UC-18 | Khi Polygon RPC lỗi/timeout → vẫn trả hành trình bình thường, báo chưa kiểm chứng được (`UNAVAILABLE`), kết hợp cache bản ghi anchor đã đọc thành công. |
| **Hỗ trợ** | `FR-AUD-001` | UC-21 | Ghi Audit log bất biến cho **mọi thao tác ghi của SHP/VER** (ai, lúc nào, đối tượng, IP). |
| **NFR** | `NFR-PERF-001` | UC-17 | Trang tra cứu hiển thị nội dung chính **< 1,5 giây (p95)** trên 4G; tổng dung lượng trang **< 500 KB**, mỗi ảnh **< 150 KB**. |
| **NFR** | `NFR-USA-002/003` | UC-17/18 | Giao diện tiếng Việt, chữ `>= 16px`, tương phản tối thiểu `4,5:1` để đọc ngoài nắng; responsive tốt trên màn hình nhỏ `360px` (Chrome Android & Safari iOS). |

### 1.2 Việc KHÔNG thuộc phạm vi của anh (Thuộc lane của thành viên khác)

* `AUTH`: Đăng nhập, làm mới token, quản lý user, khóa tài khoản.
* `MST`: Quản lý danh mục nhà màng, giống dưa, chứng nhận VietGAP/GlobalGAP.
* `BAT`: Tạo lô, phân công công nhân, quản lý vòng đời trạng thái lô ban đầu, sinh salt khi tạo lô.
* `LOG`: Ghi/duyệt/đính chính nhật ký canh tác, đồng bộ offline.
* `QC`: Nhập độ Brix, cân nặng, kiểm tra ngày cách ly, ghi nhận thu hoạch.
* `PCK`: Kích hoạt lô, dựng Merkle lúc đóng gói, sinh và in tem QR PDF.
* `LDG`: Outbox worker, neo chuỗi Besu/Polygon, retry backoff, job đối soát hằng ngày, deploy smart contract.
* `RPT`: Xem dashboard, xuất báo cáo tổng hợp.
* `FE-base`: Cấu hình router/axios/pinia/màn hình đăng nhập chung.

> Anh **không giao** và **không nhận** các hạng mục trên. Mục 2 dưới đây chỉ mô tả "anh cần gì từ họ" để không bị chặn tiến độ.

---

## 2. Hợp đồng giao tiếp với người khác (Interface Contract & Fallbacks)

Đây là **đầu vào anh cần từ nhóm**, không phải việc của anh. Mỗi hạng mục đều có thời hạn và phương án dự phòng để anh **vẫn hoàn thành code và demo được** nếu thành viên khác trễ hạn.

| # | Đầu vào anh cần | Từ lane | Hạn chót | Nếu trễ → Phương án dự phòng (Fallback) của anh |
|---|---|---|---|---|
| **I1** | `BatchStatus` enum + luật chuyển trạng thái hợp lệ (đặc biệt `ACTIVE → SHIPPED`, `SHIPPED → ACTIVE`) | `BAT` | 13/11 | Dùng bảng luật cục bộ trong `BatchStatusRules` của anh (ghi chú `ADR-003` về rủi ro trùng logic). |
| **I2** | `batches.salt_enc` đổi sang **`varchar(64)` hex**, có giá trị hợp lệ (anh cấp sẵn `SaltUtil.generateHex()`) | `BAT` | 14/11 | Khi verify trả `UNAVAILABLE`, gói bằng chứng trả `salt=null`; hoặc anh tự chạy changeset `ALTER` trên môi trường demo. |
| **I3** | Bảng/entity `qr_codes` (`code` UNIQUE, `batch_id`, `serial`, `scan_count`) | `PCK` | 15/11 | Tạo sẵn changeset đọc dự phòng (`004-qr-codes-readonly.sql`); đồng thời cho phép `GET /public/trace/{code}` nhận luôn `batchCode` làm khoá tra cứu. |
| **I4** | Bảng/entity `cultivation_events` có cột `event_hash` (hex), `status`, `occurred_at`, `seq` | `LOG` | 16/11 | Dựng lại leaf và timeline trực tiếp từ bảng `outbox_events.event_data` (**bảng này đã có sẵn** trong repo). |
| **I5** | Bảng/entity `qc_records` (`brix`, `avg_weight_kg`) | `QC` | 16/11 | Đọc từ `outbox_events` (loại `DO_BRIX`) hoặc để thẻ chỉ số `null` kèm dữ liệu seed trong changeset `005-demo-data.sql`. |
| **I6** | `greenhouses` / `varieties` / `certificates` (chỉ cần đọc: mã, tên, hiệu lực) | `MST` | 16/11 | Trả giá trị mặc định từ seed demo hoặc `null` an toàn (không làm gãy trang tra cứu). |
| **I7** | Payload outbox của lô **chứa `merkleRoot` theo định dạng JSON chuẩn** (mục 4.4) | `LDG` | 16/11 | Đọc root từ bảng `merkle_snapshots` + `anchors` và coi là `UNAVAILABLE` khi chưa có dữ liệu trên chuỗi. |
| **I8** | Contract `BlockchainAnchoring.sol` đã deploy lên Amoy + cấu hình địa chỉ | `LDG` | 16/11 | Đọc trực tiếp root từ trường `tx.input` của giao dịch self-transfer trên Polygon (`ADR-002`). |
| **I9** | `FE-base`: `vue-router` + `axios` client + store đăng nhập + layout | `FE` | 13/11 | Tự dựng bản tối thiểu trong nhánh riêng (chỉ dùng `vue-router` + `fetch`, không phụ thuộc `pinia`). |
| **I10** | Role `PACKER`, `PARTNER_QC`, `REGULATOR` trong `RoleEnum` + seed DB | `AUTH` | 13/11 | Tự thêm 3 giá trị enum + changeset seed `004-seed-shipping-roles.sql` (rất nhỏ, ít xung đột). |

---

## 3. Hiện trạng Source Code & Phân tích Gap (Đã rà soát thực tế)

### 3.1 Tài liệu nguồn & Hiện trạng repo

| File / Thành phần | Vai trò | Tình trạng thực tế |
|---|---|---|
| `Chuc_Nang_He_Thong_NLU_ECOFARM.docx` | Tài liệu SRS (UC-01..23, FR, NFR, SCR, REST API) | **Đọc tốt** — nguồn đối chiếu chuẩn cho SHP & VER. |
| `Kien_Truc_He_Thong.png` | Sơ đồ kiến trúc tổng thể | Đọc tốt (Docker Compose, WildFly/Jakarta EE, PostgreSQL, Besu, Polygon Amoy). |
| `ecofarm_backend/NLU-ECOFARM-API.yaml` | Đặc tả OpenAPI 3 (1.459 dòng) | Hợp đồng API chuẩn — đã định nghĩa đầy đủ schema `Shipment`, `PublicTrace`, `VerifyResult`, `ProofBundle`. |
| `TRIỂN KHAI DỰ ÁN ECOFARM.docx` | Bản triển khai chi tiết (mục 3.3/3.4 quy tắc hash–Merkle) | **Hỏng không thể phục hồi nội dung** — xem chi tiết phân tích byte-level ở mục 3.2. |
| Source code hiện tại | 85 file Java backend + frontend Vite scaffold | Đã rà soát toàn bộ từng class và cấu hình. |

### 3.2 Sự cố file `TRIỂN KHAI DỰ ÁN ECOFARM.docx` & Hướng xử lý dứt điểm

Qua phân tích byte-level trên file local:
* **Magic number:** `FE FF 00 50 00 4B 00 03 00 04` → File gốc vốn là cấu trúc nén **ZIP** (`.docx`), nhưng đã bị ghi đè/mã hoá sang **UTF-16BE** (có BOM `FE FF`) rồi giải mã sai bằng codec thay thế.
* **Giải mã ngược:** Trong 507.252 ký tự có tới **220.314 ký tự là `U+FFFD`** (tỷ lệ hỏng **43,4%**) — các byte gốc đã bị mất vĩnh viễn.
* **Khôi phục cấu trúc ZIP:** Dù dựng lại được 20 local file header và giải nén nguyên vẹn 6/20 file ảnh `word/media/*.png`, nhưng luồng deflate của `word/document.xml` bị hỏng ngay từ byte đầu tiên → **0 byte văn bản** khôi phục được. File cũng chưa từng được commit vào Git hay lưu trong Local History.

**Hành động xử lý của anh (~30 phút ngày 12/11):**
1. Nhắn nhóm lấy lại bản gốc trên Google Docs/Drive bằng thao tác chuẩn: **File → Download → Microsoft Word (.docx)**.
2. Nếu nhóm không còn bản gốc: Anh ban hành ngay **`ADR-001`** (mục 4.2) dựa trên thông số đã khóa sẵn trong file `NLU-ECOFARM-API.yaml` (`keccak256` + `RFC8785`), lập biên bản họp nhóm ghi nhận sự cố hỏng file và dùng `ADR-001` làm chuẩn duy nhất cho cả Backend, Smart Contract và Script kiểm chứng.

### 3.3 Lỗ hổng bảo mật nghiêm trọng phát hiện trong code hiện tại (Ảnh hưởng trực tiếp FR-AUD-001)

Tại file `ecofarm_backend/src/main/java/jakarta/ecofarm_backend/modules/blockchain/rest/AllowAllSecurityFilter.java` (dòng 153–159):
```java
@Override
public void filter(ContainerRequestContext requestContext) {
    requestContext.setSecurityContext(new SecurityContext() {
        @Override
        public Principal getUserPrincipal() {
            return new Principal() {
                @Override
                public String getName() {
                    return "admin";
```
* **Nguyên nhân:** `AllowAllSecurityFilter` được gắn `@Provider` nhưng không có `@PreMatching` và `@Priority` → mặc định chạy ở `Priorities.USER` (`5000`), tức là chạy **sau** `RoleAuthorizationFilter` (`2000`).
* **Hệ quả:** Dù `@RequireRoles` vẫn chặn được ở bước trước, nhưng ngay sau đó mọi request hợp lệ đều bị **ghi đè `SecurityContext` thành user `"admin"`** với full quyền `ADMIN/PARTNER_QC/REGULATOR`. Mọi thao tác ghi dữ liệu trong Resource/Service khi lấy `SecurityContext` để lưu `AuditLog` sẽ ghi nhầm người thực hiện là `"admin"` → **Vi phạm trực tiếp `FR-AUD-001`** của phân hệ SHP/VER.
* **Cách khắc phục (Commit `S0-1`):** Xoá bỏ hoàn toàn file `AllowAllSecurityFilter.java` (vì hệ thống đã có `AuthTokenInterceptor` đảm nhiệm xác thực JWT thật).

### 3.4 Bảng tổng hợp Gap của phân hệ SHP (Xuất kho, giao hàng)

| Mã Gap | Hiện trạng trong Source Code (kèm dẫn chiếu) | Mức độ | Yêu cầu liên quan |
|---|---|---|---|
| **S-G1** | Chưa có Entity `Shipment`, `ShipmentRepository` và bảng `shipments`. | Chặn | `FR-SHP-001/002` |
| **S-G2** | `Batch.status` đang là `String` tự do, **không có Enum, không có máy trạng thái** → dễ dẫn đến lỗi xuất kho cho lô chưa `ACTIVE`. | Chặn | `FR-SHP-001`, `FR-BAT-004` |
| **S-G3** | Chưa có `BatchRepository` (Entity `Batch` đã có nhưng chưa có lớp truy xuất DB). | Chặn | `FR-SHP-001` |
| **S-G4** | Chưa có DTO + Bean Validation cho Shipment (`ValidationExceptionMapper` trả lỗi RFC 7807 đã có sẵn). | Cao | `FR-SHP-001/002` |
| **S-G5** | Chưa có REST endpoint cho Shipment (`BlockchainResource` hiện đang gộp chung cả prefix `/block-chain` lẫn `/admin/...`). | Chặn | `FR-SHP-001/002` |
| **S-G6** | `AuditLog` và `AuditLogRepository` **đã có sẵn** nhưng chưa được gọi trong luồng nghiệp vụ. | Cao | `FR-AUD-001` |
| **S-G7** | `RoleEnum` mới chỉ có `USER, ADMIN, FARMER, QUALITY_CONTROL` — thiếu `PACKER` (Nhân viên đóng gói/kho), `PARTNER_QC` (QC đối tác), `REGULATOR` (Thanh tra). | Cao | `FR-AUTH-002` |
| **S-G8** | `BlockchainResource.retryAnchor` và `recallBatch` đang gắn `@RequireRoles({"QUALITY_CONTROL","FARMER"})` → **Sai lệch phân quyền nghiêm trọng** (nông dân lại được quyền thu hồi lô và retry neo chuỗi). | Cao (Bảo mật) | `FR-AUTH-002` |
| **S-G9** | `ecofarm_frontend` mới chỉ là **scaffold mặc định của Vite** (`HelloWorld.vue`, `TheWelcome.vue`), chưa có router, client API và màn hình `SCR-11`. | Chặn | `UC-15`, `UC-16` |

### 3.5 Bảng tổng hợp Gap của phân hệ VER (Tra cứu, xác thực)

| Mã Gap | Hiện trạng trong Source Code (kèm dẫn chiếu) | Mức độ | Yêu cầu liên quan |
|---|---|---|---|
| **V-G1** | `VerificationResource.getPublicTrace` **đang trả dữ liệu giả (hardcode)**: `new BatchInfo("BATCH-EXAMPLE", "GROWING", ...)`. | Chặn | `FR-VER-001/002` |
| **V-G2** | `verifyTrace` **đang lấy bừa snapshot cuối cùng trong DB** (`snapshots.get(snapshots.size()-1)`) thay vì truy vấn theo `code` của lô → Sai hoàn toàn khi DB có từ 2 lô trở lên. | Chặn | `FR-VER-003` |
| **V-G3** | **Chưa có bất kỳ class nào tính toán Merkle Tree** (`MerkleService`, canonicalization) và `PolygonClient` đang gửi giao dịch **self-transfer** (`value = 0`, đính kèm text vào `data` tại dòng 84–89) thay vì gọi hàm smart contract. | Chặn (Lớn nhất) | `FR-VER-003/006` |
| **V-G4** | **Chưa có bảng `qr_codes`**, chưa có ánh xạ `code → batch` và bộ đếm số lần quét tem (`scan_count`). | Chặn | `FR-VER-001`, `FR-PCK-006` |
| **V-G5** | DTO `PublicTrace` hiện chỉ có 3 trường `code/batch/blockchain` — thiếu `metrics`, `timeline`, `certificates`, `shipment`, `recall` so với đặc tả trong `NLU-ECOFARM-API.yaml`. | Cao | `FR-VER-001/002` |
| **V-G6** | Chưa có bảng `cultivation_events` / `qc_records` để dựng timeline và thẻ chỉ số. | Chặn | `FR-VER-002` |
| **V-G7** | Chưa có bảng danh mục `greenhouses` / `varieties` / `certificates`. | Cao | `FR-VER-001` |
| **V-G8** | Chưa có bộ lọc giới hạn tần suất (Rate Limiting) theo IP cho các API `/public/**`. | Cao | `FR-VER-005`, `NFR-SEC-005` |
| **V-G9** | `getProofBundle` **đang trả giá trị giả**: `salt="0xfaceb00c"`, root fallback `"0xdeadbeefcafe1234"`, danh sách `leaves` và `events` rỗng. | Chặn | `FR-VER-006` |
| **V-G10** | `Batch.saltEnc` đang khai báo kiểu `Long` → **không thể lưu và khôi phục được salt 32-byte** để băm lại leaf/proof. | Cao | `FR-VER-006`, `FR-BAT-003` |
| **V-G11** | `PolygonClient` chưa có hàm gọi `getMerkleRoot` từ contract; URL explorer đang bị hardcode `amoy.polygonscan.com` ở cả `PolygonClient` và `BlockchainAnchoringService`. | Cao | `FR-VER-003/004`, `FR-LDG-006` |
| **V-G13** | Đường dẫn API bị lệch: Code hiện tại đặt `/api/v1/block-chain/public/trace/{code}`, trong khi OpenAPI spec và `AuthTokenInterceptor.PUBLIC_PATHS` quy định là `/api/v1/public/trace/{code}`. | Trung bình | `FR-VER-001` |
| **V-G14** | Trường `Anchor.anchoredAt` **không thống nhất đơn vị thời gian**: Constructor chia `1000` (lưu giây), nhưng hàm `checkPublicConfirmation` lại gán `System.currentTimeMillis()` (lưu mili-giây) → Hiển thị sai lệch thời điểm neo. | Cao | `FR-VER-004` |

---

## 4. Quyết định kỹ thuật cốt lõi (Architecture Decision Records)

### 4.1 Kiến trúc phân tầng chuẩn cho module SHP & VER

```
Resource (JAX-RS, DTO Validation, Rate Limit)
   ↓
Service (@Transactional, Business Logic, State Machine, Audit Log)
   ↓
Repository (JPA / Panache / EntityManager)  +  CryptoUtil / MerkleService / PolygonClient
```
* **Nguyên tắc:** Tuyệt đối không viết logic nghiệp vụ hay truy vấn DB trực tiếp trong tầng `Resource`.
* **Chuẩn trả lỗi:** Mọi lỗi nghiệp vụ trả về định dạng chuẩn `application/problem+json` (RFC 7807) với các mã trạng thái chuẩn: `400`, `401`, `403`, `404`, `409` (sai trạng thái lô), `422` (dữ liệu đầu vào vi phạm quy tắc), `429` (quá giới hạn gọi API).

### 4.2 ADR-001 — Quy tắc chuẩn hóa JSON, Băm & Cây Merkle (Thay thế mục 3.4 bị hỏng)

> **Cơ sở pháp lý kỹ thuật:** File đặc tả `NLU-ECOFARM-API.yaml` đã khóa cứng `hashAlgorithm: keccak256`, `canonicalization: RFC8785`, và `batchIdHash = keccak256(batchCode)`. Anh thiết lập module `jakarta.ecofarm_backend.common.crypto` tuân thủ tuyệt đối 3 thông số này và cung cấp cho cả nhóm dùng chung.

| Hạng mục | Quy tắc chuẩn hóa trong ADR-001 |
|---|---|
| **Thuật toán băm** | `keccak256` — sử dụng trực tiếp `org.web3j.crypto.Hash.sha3` (đã có sẵn trong dependency `org.web3j:core:4.10.0` của `pom.xml`). |
| **Chuẩn hóa JSON (`RFC 8785`)** | Sắp xếp toàn bộ khóa (keys) của JSON object theo thứ tự từ điển (lexicographical UTF-16 code units), loại bỏ toàn bộ khoảng trắng thừa, chuẩn hóa chuỗi UTF-8. |
| **`batchIdHash`** | `keccak256(utf8(batchCode))` → biểu diễn dạng chuỗi hex có tiền tố `0x` + 64 ký tự viết thường. |
| **`salt`** | 32 bytes sinh từ `SecureRandom`, lưu trữ dạng hex 64 ký tự (không có `0x` trong DB, thêm `0x` khi xuất bundle). Mỗi lô có duy nhất 1 `salt`. |
| **`eventHash`** | `keccak256(utf8(canonicalJson(eventPayload)))` (payload sự kiện tuân theo mẫu mục 1.4 SRS, dùng `actorRef` ẩn danh). |
| **Nút lá (`leaf`)** | `keccak256(concat(bytes32(eventHash), bytes32(salt)))` — nối mảng 32 byte của `eventHash` và 32 byte của `salt` (tổng 64 bytes) rồi băm `keccak256`. Các lá được sắp xếp tăng dần theo thứ tự `seq` của sự kiện trước khi dựng cây. |
| **Nút cha (`node`)** | `keccak256(concat(bytes32(left), bytes32(right)))` (64 bytes). |
| **Xử lý nút lẻ ở mỗi tầng** | **Promote (Đưa thẳng lên tầng kế tiếp)**, tuyệt đối **không** nhân đôi nút lẻ (tránh lỗ hổng giả mạo cặp lá trùng nhau CVE-2012-2459). |
| **`merkleRoot`** | Chuỗi hex `0x` + 64 ký tự. Nếu danh sách sự kiện rỗng, trả về `0x` + 64 số `0`. |

### 4.3 ADR-002 — Cơ chế đọc kép (Dual-Read) Merkle Root trên Polygon

Để hóa giải rủi ro `V-G3` (giao dịch hiện tại đang là self-transfer và contract có thể chưa kịp deploy):
1. **Đường đọc chính (Contract Call):** Nếu cấu hình `blockchain.polygon.contract-address` hợp lệ, dùng `PolygonClient` gọi `eth_call` tới hàm `getMerkleRoot(bytes32 batchIdHash)` của hợp đồng `BlockchainAnchoring.sol`.
2. **Đường đọc dự phòng (Transaction Input Fallback):** Nếu chưa cấu hình contract hoặc hàm trả về `0x0...0`, hệ thống dùng `txHash` lưu trong bảng `anchors`, gọi `eth_getTransactionByHash(txHash)`, đọc trường `input` (hex), giải mã UTF-8 để trích xuất `merkleRoot` từ chuỗi JSON payload.
3. **Bộ nhớ đệm (Cache - `FR-VER-007`):** Lưu kết quả đọc on-chain vào `ConcurrentHashMap<String, CachedAnchor>` (TTL 10 phút) để khi Polygon RPC bị đứt kết nối hoặc quá tải, API `/verify` và `/trace` vẫn phản hồi tức thì dưới 50ms.

### 4.4 Chuẩn hóa Payload Outbox cho sự kiện neo lô (Giao ước với lane LDG)

```json
{
  "batchIdHash": "0x...",
  "merkleRoot": "0x...",
  "seq": 12,
  "leafCount": 34
}
```

---

## 5. Lộ trình thực hiện chi tiết theo ngày (12/11/2026 – 18/11/2026)

| Ngày | Mục tiêu trọng tâm | Danh sách công việc (Commits) | Đầu ra kiểm chứng (Deliverables) |
|---|---|---|---|
| **12/11 (Thứ 5)** | Nền tảng bảo mật, Crypto & DB | `S0-1` .. `S0-4` | Xoá filter lỗi bảo mật; xong `ADR-001`; xong bộ `MerkleService` + Unit Test xanh; xong Liquibase bảng `shipments` & `qr_codes`. |
| **13/11 (Thứ 6)** | Backend SHP (Phần 1) | `S1-1` .. `S1-3` | Xong Entity/Repo `Shipment` & `BatchRepository`; xong DTO Validation; xong `ShipmentService.createShipment` (xuất kho). |
| **14/11 (Thứ 7)** | Backend SHP (Hoàn tất) | `S1-4` .. `S1-7` | Xong `receiveShipment` (nhận/từ chối hàng); xong `ShipmentResource`, Audit Log, sửa phân quyền RBAC và JUnit test cho SHP. |
| **15/11 (Chủ nhật)** | Buffer / Chuẩn bị FE nền | *Dự phòng (S3-1 sớm)* | Nghỉ ngơi / Rà soát tiến độ nhóm; nếu nhóm chưa giao `FE-base` (I9) thì dựng luôn khung `vue-router` + API client. |
| **16/11 (Thứ 2)** | Backend VER (Phần 1) | `S2-1` .. `S2-4` | Xong hàm đọc root Polygon + Cache; xong tra cứu `code → batch` + đếm lượt quét; xong `PublicTrace` thật và chuẩn hóa URL `/api/v1/public/trace/{code}`. |
| **17/11 (Thứ 3)** | Backend VER (Hoàn tất) + Frontend | `S2-5` .. `S2-8`, `S3-1` .. `S3-3` | Xong `/verify` thật, Rate Limit IP, `proof-bundle` + script `verify_proof.py`; xong 2 màn hình Frontend (`SCR-11` và `PUB-01`/`PUB-02`). |
| **18/11 (Thứ 4)** | Kiểm thử, Seed Demo & Bàn giao | `S2-9`, `S3-4`, `S3-5` | Chốt bộ Unit Test coverage >= 70%; nạp dữ liệu mẫu `005-demo-data.sql` (có sẵn `txHash` Amoy thật); hoàn tất tài liệu kịch bản Demo. |

> **Nguyên tắc kỷ luật thép:** Cuối ngày **17/11** toàn bộ hệ thống phải build xanh và chạy thông suốt luồng Demo từ đầu đến cuối. Ngày **18/11** chỉ dành cho chạy test, nạp dữ liệu mẫu, chụp ảnh màn hình vào báo cáo và tập dượt kịch bản thuyết trình. **Tuyệt đối không viết thêm tính năng mới vào ngày 18/11.**

**Quản lý nhánh Git cá nhân:**
```bash
git checkout -b feat/shp-ver master
# Thực hiện các commit nhỏ độc lập theo đúng mã S0-1..S3-5, đảm bảo mỗi commit đều compile xanh
```

---

## 6. Đặc tả chi tiết từng Commit (100% công việc của anh)

### Phase 0 — Nền tảng dùng chung & Mật mã học (12/11)

| Mã Commit | Message chuẩn | Chi tiết triển khai kỹ thuật | Tiêu chí kiểm chứng (Verify) |
|---|---|---|---|
| **S0-1** | `fix(security): remove AllowAllSecurityFilter overriding SecurityContext` | Xoá file `modules/blockchain/rest/AllowAllSecurityFilter.java`. Giữ nguyên cơ chế xác thực JWT của `AuthTokenInterceptor` và `RoleAuthorizationFilter`. | Đăng nhập bằng tài khoản thật, gọi API thấy `SecurityContext.getUserPrincipal().getName()` trả đúng username thay vì `"admin"`. |
| **S0-2** | `docs(adr): add ADR-001 for RFC8785 canonicalization and Keccak256 Merkle tree` | Tạo file `docs/ADR-001-hash-merkle.md` ghi rõ quy tắc mục 4.2, biên bản xác nhận file `.docx` cũ hỏng 43,4% không thể khôi phục, và quy định dùng chung package `common.crypto`. | Tài liệu rõ ràng, gửi nhóm xác nhận. |
| **S0-3** | `feat(crypto): implement CanonicalJson, HashUtil, SaltUtil and MerkleService` | Tạo trong `jakarta.ecofarm_backend.common.crypto`:<br>- `CanonicalJson.canonicalize(Object/JsonNode)`: sort keys đệ quy, xuất chuỗi compact.<br>- `SaltUtil.generateHex()`: sinh 32 bytes `SecureRandom` → 64 ký tự hex.<br>- `HashUtil`: `keccak256Hex(byte[])`, `leafHash(String eventHashHex, String saltHex)`, `nodeHash(byte[] left, byte[] right)`.<br>- `MerkleService`: `buildRoot(List<String> leavesHex)`, `buildProof(List<String> leavesHex, int index)` (trả về danh sách `ProofStep{position: LEFT/RIGHT, hash}`), `verifyProof(String leafHex, List<ProofStep> proof, String rootHex)`. | Unit Test JUnit 5: Cùng JSON khác thứ tự key → cùng hash; test cây 1, 2, 3, 7, 100 lá → `verifyProof` trả `true`; sửa 1 ký tự trong lá → trả `false`. |
| **S0-4** | `feat(db): add Liquibase changesets for shipments, qr_codes fallback and roles` | Thêm `db/changes/003-shipments.sql` (bảng `shipments`), `004-qr-codes-readonly.sql` (bảng `qr_codes` có cột `scan_count`), `004-seed-shipping-roles.sql` (`PACKER`, `PARTNER_QC`, `REGULATOR`) và bổ sung 3 hằng số vào `RoleEnum.java`. | Chạy `liquibase update` thành công; `mvn clean test-compile` xanh. |

### Phase 1 — Phân hệ SHP: Xuất kho & Giao hàng (13/11 – 14/11)

| Mã Commit | Message chuẩn | Chi tiết triển khai kỹ thuật | Tiêu chí kiểm chứng (Verify) |
|---|---|---|---|
| **S1-1** | `feat(shipment): add Shipment entity, ShipmentRepository and BatchRepository` | - Tạo `Shipment.java` (ánh xạ bảng `shipments`, có `@Version` chống ghi đè đồng thời).<br>- Tạo `ShipmentRepository.java` (`persist`, `findById`, `findByBatchId`, `existsActiveShipmentForBatch`).<br>- Tạo `BatchRepository.java` (`findById`, `findByBatchCode`, `updateStatus`) và `BatchStatusRules.java` kiểm tra chuyển trạng thái hợp lệ (`ACTIVE -> SHIPPED`, `SHIPPED -> ACTIVE`). | Compile xanh, tiêm CDI (`@Inject`) hoạt động tốt. |
| **S1-2** | `feat(shipment): add Shipment DTOs with Jakarta Bean Validation` | - `ShipmentCreateRequest`: `destination` (`@NotBlank`, `@Size(max=255)`), `vehiclePlate` (`@NotBlank`, `@Pattern` biển số xe VN `^[0-9]{2}[A-Z]{1,2}[- ]?[0-9]{4,5}$`), `shippedAt` (`@Positive`), `quantity` (`@Positive`), `note` (`@Size(max=1000)`).<br>- `ShipmentReceiveRequest`: `accepted` (`@NotNull`), `receivedAt`, `conditionNote` (`@Size(max=2000)`), `photoUrls` (`@Size(max=5)`).<br>- `ShipmentResponse`: đầy đủ thông tin trả về theo OpenAPI. | Gửi payload thiếu `destination` hoặc sai biển số xe → `ValidationExceptionMapper` tự động trả `422 Unprocessable Entity` (RFC 7807). |
| **S1-3** | `feat(shipment): implement ShipmentService.createShipment with state validation` | Trong 1 transaction (`@Transactional`):<br>1. Tìm `Batch` theo `batchId` (không có → `404`).<br>2. Kiểm tra trạng thái: nếu là `RECALLED` → ném lỗi `409` ("Lô hàng đã bị thu hồi, cấm xuất kho"); nếu khác `ACTIVE` → ném lỗi `409` ("Chỉ lô ở trạng thái ACTIVE mới được xuất kho").<br>3. Kiểm tra `existsActiveShipmentForBatch` (nếu đã có phiếu đang `SHIPPED` → trả `409` chống tạo trùng).<br>4. Lưu `Shipment` trạng thái `SHIPPED`, cập nhật `Batch.status = "SHIPPED"`. | Test các nhánh: Lô `GROWING` → `409`; Lô `RECALLED` → `409`; Lô `ACTIVE` → `201 Created` và trạng thái lô đổi sang `SHIPPED`. |
| **S1-4** | `feat(shipment): implement ShipmentService.receiveShipment for partner QC` | Trong `@Transactional`:<br>1. Tìm `Shipment` theo `id` (không có → `404`); nếu trạng thái khác `SHIPPED` → trả `409` ("Phiếu xuất kho đã được xử lý trước đó").<br>2. Nếu `accepted == true`: đổi `Shipment.status = "RECEIVED"`, gán `receivedAt`, `receivedBy`, `conditionNote`, `photoUrls`.<br>3. Nếu `accepted == false`: bắt buộc `conditionNote` không được rỗng (rỗng → `422`); đổi `Shipment.status = "REJECTED"`, đồng thời trả trạng thái lô về `Batch.status = "ACTIVE"` để kho có thể xuất lại chuyến khác. | Test 3 nhánh: Nhận hàng thành công (`RECEIVED`), Từ chối hàng thiếu lý do (`422`), Từ chối hàng đủ lý do (`REJECTED` + lô quay về `ACTIVE`). |
| **S1-5** | `feat(audit): record immutable audit logs for all SHP write operations` | Tạo helper `AuditHelper.log(SecurityContext, HttpServletRequest, action, targetTable, targetId, detailJson)` gọi `AuditLogRepository.persist(...)` cho các sự kiện: `SHIPMENT_CREATED`, `SHIPMENT_RECEIVED`, `SHIPMENT_REJECTED`. Lấy đúng IP từ header `X-Forwarded-For` / `X-Real-IP`. | Truy vấn bảng `audit_logs` sau khi gọi API thấy ghi nhận chính xác `actor_id`, `actor_name`, `ip_address` và chi tiết thay đổi. |
| **S1-6** | `feat(api): expose REST endpoints for creating and receiving shipments` | Tạo `ShipmentResource.java`:<br>- `POST /api/v1/batches/{batchId}/shipments` (`@RequireRoles({"PACKER", "ADMIN"})`) → trả `201 Created`.<br>- `POST /api/v1/shipments/{shipmentId}/receive` (`@RequireRoles({"PARTNER_QC", "ADMIN"})`) → trả `200 OK`. | Swagger UI (`/openapi`) hiển thị đúng đường dẫn và schema; test bằng curl/Postman thành công. |
| **S1-7** | `feat(api): add shipment history endpoints and fix RBAC on BlockchainResource` | - Bổ sung `GET /api/v1/batches/{batchId}/shipments` và `GET /api/v1/shipments?status=&page=&size=`.<br>- Sửa lỗi bảo mật `S-G8` trong `BlockchainResource`: Đổi `@RequireRoles` của `retryAnchor`, `recallBatch`, `createTestOutbox` từ `{"QUALITY_CONTROL", "FARMER"}` thành `{"ADMIN"}`. | User có role `FARMER` gọi `/recall` bị chặn `403 Forbidden`; role `PACKER` xem được danh sách phiếu xuất kho. |

### Phase 2 — Phân hệ VER: Tra cứu công khai & Xác thực Blockchain (16/11 – 17/11)

| Mã Commit | Message chuẩn | Chi tiết triển khai kỹ thuật | Tiêu chí kiểm chứng (Verify) |
|---|---|---|---|
| **S2-1** | `feat(polygon): implement dual-read Merkle root from Polygon with caching` | - Trong `PolygonClient`: Thêm hàm `getAnchoredRoot(String batchIdHashHex, String txHash)` thực hiện cơ chế đọc kép `ADR-002` (gọi `eth_call` selector `0x3d7d6592` của `getMerkleRoot(bytes32)` trước; nếu chưa có contract thì gọi `eth_getTransactionByHash` giải mã `tx.input`).<br>- Bổ sung cache in-memory TTL 10 phút (`FR-VER-007`).<br>- Đưa `explorerBaseUrl` và `chainId` ra file cấu hình `BlockchainConfig` (`FR-LDG-006`).<br>- Sửa lỗi `V-G14`: Chuẩn hóa `Anchor.anchoredAt` về **epoch milliseconds** ở mọi nơi. | Gọi hàm với `txHash` thật trên Amoy lấy ra đúng `merkleRoot`; ngắt mạng thử nghiệm thấy trả về kết quả từ cache hoặc ném ngoại lệ kiểm soát được (không crash). |
| **S2-2** | `feat(qr): implement QR code lookup with batchCode fallback and scan counter` | - Tạo `QrCodeRepository`: Tìm `QrCode` theo `code`. Nếu tìm thấy, tăng `scan_count = scan_count + 1` và kiểm tra ngưỡng cảnh báo sao chép tem (ví dụ `scan_count > 50` thì gắn cờ `suspiciousScan = true` và ghi `AuditLog` cảnh báo theo `FR-PCK-006`).<br>- **Fallback thông minh (I3):** Nếu không tìm thấy trong bảng `qr_codes`, thử tìm trong `BatchRepository.findByBatchCode(code)` để đảm bảo luôn tra cứu được ngay cả khi lane PCK chưa sinh tem. | Tra cứu bằng mã QR hoặc mã lô `DL-MANG01-281026` đều trả về đúng đối tượng `Batch`; quét nhiều lần thấy `scan_count` tăng. |
| **S2-3** | `feat(ver): implement real PublicTraceService without PII or yield leakage` | Xây dựng `PublicTraceService.getPublicTrace(String code)` trả về đầy đủ cấu trúc `PublicTrace` theo OpenAPI:<br>- Thông tin lô (`batchCode`, `status`, ngày gieo/thu hoạch), nhà màng, giống dưa (đọc an toàn, có fallback).<br>- Thẻ chỉ số (`metrics`: độ `brix`, `avgWeightKg` từ bảng QC hoặc sự kiện `DO_BRIX`).<br>- `timeline`: Lọc các sự kiện đã duyệt (`APPROVED` / `CONFIRMED`), sắp xếp theo thời gian xảy ra, chuyển mã sự kiện sang tên tiếng Việt thân thiện, **loại bỏ hoàn toàn tên công nhân, số điện thoại và tổng sản lượng thu hoạch (`NFR-PRV-001`)**.<br>- `shipment`: Điểm đến và thời gian xuất kho.<br>- `blockchain`: Trạng thái neo, `chainName`, `merkleRoot`, `txHash`, `blockNumber`, `anchoredAt`, `explorerUrl`.<br>- `recall`: Cờ cảnh báo thu hồi và lý do (nếu `status == RECALLED`). | Kiểm tra JSON trả về: Đầy đủ 7 khối dữ liệu, không chứa bất kỳ trường PII hay tổng sản lượng nào; lô chưa `ACTIVE` vẫn trả `200 OK` kèm cờ `anchored: false` và thông báo trạng thái. |
| **S2-4** | `feat(api): align public trace endpoints to /api/v1/public/trace/{code}` | - Cập nhật `@Path("/public/trace")` cho `VerificationResource` để khớp chuẩn `/api/v1/public/trace/{code}` và `/api/v1/public/trace/{code}/verify`.<br>- Cập nhật `AuthTokenInterceptor.PUBLIC_PATHS` cho phép truy cập không cần token vào `/api/v1/public/`.<br>- Giữ 1 endpoint chuyển hướng tạm (`307 Temporary Redirect`) ở đường dẫn cũ `/api/v1/block-chain/public/trace/{code}` để tương thích ngược. | Gọi `curl -i http://localhost:8080/api/v1/public/trace/{code}` không cần header `Authorization` trả về `200 OK`. |
| **S2-5** | `feat(ver): implement real Merkle verification against Polygon blockchain` | Xây dựng `VerificationService.verifyTrace(String code)`:<br>1. Tìm đúng `Batch` theo `code` (sửa triệt để lỗi `V-G2` lấy nhầm snapshot cuối bảng).<br>2. Nếu lô chưa `ACTIVE`/`SHIPPED`/`RECALLED` hoặc chưa có bản ghi `Anchor` → trả `status = "UNAVAILABLE"` kèm lý do rõ ràng.<br>3. Đọc danh sách sự kiện của lô (từ `cultivation_events` hoặc fallback `outbox_events`), lấy `salt` của lô, tính lại danh sách `leaves` và dựng lại `computedRoot = MerkleService.buildRoot(leaves)`.<br>4. Gọi `PolygonClient.getAnchoredRoot(batchIdHash, anchor.txHash)` để lấy `onChainRoot`. Nếu RPC lỗi → trả `UNAVAILABLE` ("Không thể kết nối mạng Polygon tức thời, vui lòng thử lại sau" - `FR-VER-007`).<br>5. So sánh `computedRoot` và `onChainRoot`: khớp → trả `MATCH`, lệch → trả `MISMATCH`. Đính kèm đầy đủ `txHash`, `blockNumber`, `anchoredAt`, `explorerUrl` (`FR-VER-004`). | Test đủ 3 kịch bản: `MATCH` (dữ liệu nguyên vẹn), `MISMATCH` (khi thử sửa 1 chữ trong DB), và `UNAVAILABLE` (khi lô đang `GROWING` hoặc tắt mạng RPC). |
| **S2-6** | `feat(security): add IP-based rate limiting filter for public endpoints` | Tạo `PublicRateLimitFilter.java` (`@Provider`, `@Priority(Priorities.AUTHENTICATION + 10)`):<br>- Chỉ áp dụng cho các request có path bắt đầu bằng `/api/v1/public/`.<br>- Sử dụng thuật toán Token Bucket trong bộ nhớ (`ConcurrentHashMap<String, Bucket>`) theo địa chỉ IP client (lấy qua `X-Forwarded-For` / `RemoteAddr`), giới hạn **30 requests / phút / IP** (burst 10).<br>- Khi vượt ngưỡng: trả mã `429 Too Many Requests`, header `Retry-After: 60` và body chuẩn RFC 7807 (`FR-VER-005`, `NFR-SEC-005`). | Chạy script bắn 31 request liên tiếp trong 5 giây → 30 request đầu trả `200`, request thứ 31 trả `429` kèm header `Retry-After`. |
| **S2-7** | `feat(proof): implement real cryptographic proof bundle for QC and regulators` | Hoàn thiện `GET /api/v1/batches/{batchId}/proof-bundle` (`@RequireRoles({"PARTNER_QC", "REGULATOR", "ADMIN"})`):<br>- Xoá toàn bộ giá trị giả (`0xfaceb00c`, `0xdeadbeefcafe1234`).<br>- Trả về: `batchCode`, `batchIdHash`, `hashAlgorithm: "keccak256"`, `canonicalization: "RFC8785"`, `salt` (hex 64 ký tự thật của lô), `merkleRoot` thật.<br>- Danh sách `events`: Các sự kiện đã khử PII (chỉ giữ `actorRef` ẩn danh như `"usr_7f3a"`).<br>- Danh sách `leaves`: Gồm `seq`, `eventHash`, `leafHash`, và mảng đường dẫn chứng minh `proof` (`MerkleService.buildProof`).<br>- Thông tin `anchor`: `chainId`, `contractAddress`, `txHash`, `blockNumber`, `anchoredAt`. | Tải file JSON từ API về, kiểm tra từng trường đều là dữ liệu mật mã học thật, khớp với trạng thái lô. |
| **S2-8** | `feat(script): add standalone Python verification script for proof bundles` | Tạo file `scripts/verify_proof.py` (kèm hướng dẫn chạy 1 dòng lệnh, dùng thư viện `eth-hash` / `pycryptodome` hoặc `web3`):<br>- Đọc file `proof-bundle.json` được xuất từ `S2-7`.<br>- Tự động chuẩn hóa lại từng `event` theo RFC 8785, băm `eventHash`, ghép với `salt` để băm `leafHash`.<br>- Kiểm tra từng `Merkle proof` và dựng lại `merkleRoot`.<br>- Nếu có truyền `--rpc-url`, script gọi thẳng lên Polygon Amoy đối chiếu với giao dịch `txHash` và in kết quả **`[PASS] MATCH`** hoặc **`[FAIL] MISMATCH`**. | Chạy lệnh `python scripts/verify_proof.py bundle.json` trên terminal → In ra kết quả kiểm chứng từng lá và báo `MATCH`. |
| **S2-9** | `test(shp-ver): add comprehensive unit tests for crypto, SHP and VER services` | Viết bộ JUnit 5 test bao phủ: `CanonicalJsonTest`, `MerkleServiceTest`, `BatchStatusRulesTest`, `ShipmentServiceTest`, `PublicTraceServiceTest`, `VerificationServiceTest`, `PublicRateLimitFilterTest`. | `mvn test` xanh 100%, độ bao phủ mã nguồn (code coverage) tầng service của SHP & VER đạt `>= 70%` (`NFR-MNT-001`). |

### Phase 3 — Giao diện Frontend, Dữ liệu Demo & Bàn giao (17/11 – 18/11)

| Mã Commit | Message chuẩn | Chi tiết triển khai kỹ thuật | Tiêu chí kiểm chứng (Verify) |
|---|---|---|---|
| **S3-1** | `feat(fe): setup minimal router, API client and role-based layout (if I9 missing)` | *(Chỉ thực hiện nếu nhóm chưa giao `FE-base`)*: Cài `vue-router`, tạo `src/api/client.ts` (bọc `fetch` với `credentials: 'include'`, xử lý lỗi RFC 7807), dọn dẹp các component mẫu `HelloWorld.vue` của Vite. | `npm run build` thành công không lỗi TypeScript/Vite. |
| **S3-2** | `feat(fe): implement SCR-11 Shipment Outbound and Partner Receive screen` | Tạo màn hình `src/views/ShipmentManageView.vue` (route `/shipments`):<br>- **Tab Xuất kho (Role `PACKER`):** Chọn lô đang `ACTIVE`, nhập Điểm đến, Biển số xe (validate định dạng biển VN), Số lượng, Ghi chú → Gọi `POST /api/v1/batches/{id}/shipments`.<br>- **Tab Nhận hàng (Role `PARTNER_QC`):** Danh sách các chuyến đang `SHIPPED`, nhập Ghi chú tình trạng, URL ảnh biên bản → Nút **"Xác nhận nhận hàng"** (`accepted=true`) và nút **"Từ chối nhận hàng"** (`accepted=false`).<br>- Hiển thị rõ thông báo lỗi khi server trả `409` hoặc `422`. | Thao tác mượt trên trình duyệt, giao diện tiếng Việt rõ ràng, chữ `>= 16px`. |
| **S3-3** | `feat(fe): implement PUB-01 Public Traceability page and PUB-02 Verify Modal` | Tạo màn hình `src/views/PublicTraceView.vue` (route `/t/:code`, không yêu cầu đăng nhập):<br>- **PUB-01:** Banner nông trại + mã lô, Hộp trạng thái xác thực nổi bật (Xanh lá nếu đã neo, Vàng nếu đang canh tác/chưa `ACTIVE`, Đỏ rực kèm lý do nếu `RECALLED`), cảnh báo nếu phát hiện mã QR bị quét bất thường nhiều lần, Thẻ chỉ số (Độ ngọt Brix `°Bx`, Cân nặng trung bình `kg`, Giống dưa, Nhà màng, Chứng nhận VietGAP), và **Timeline dọc** từ lúc xuống giống đến khi xuất kho.<br>- **PUB-02:** Khi bấm nút **"Kiểm chứng trên Blockchain"**, mở hộp thoại gọi `POST /api/v1/public/trace/{code}/verify`, hiển thị hiệu ứng kiểm tra từng bước, kết quả `MATCH`/`MISMATCH`/`UNAVAILABLE`, mã `merkleRoot`, `txHash`, số `block`, thời điểm neo và nút mở trực tiếp **PolygonScan**.<br>- Tối ưu CSS thuần nhẹ nhàng để tổng dung lượng trang `< 500 KB`, hiển thị đẹp trên màn hình hẹp `360px` (`NFR-PERF-001`, `NFR-USA-003`). | Mở trên trình duyệt điện thoại (giả lập màn hình 360px + mạng 4G): Tải dưới 1,5 giây, độ tương phản cao dễ đọc, bấm kiểm chứng mở đúng link PolygonScan. |
| **S3-4** | `chore(db): add comprehensive demo seed data for 18/11 presentation` | Tạo `db/changes/005-demo-data.sql` nạp sẵn dữ liệu hoàn chỉnh để demo độc lập:<br>- 1 Nhà màng (`MANG01`), 2 Giống dưa (Dưa lưới Hoàng Kim, Mật độ Brix chuẩn `14.5°Bx`), 1 Chứng nhận VietGAP.<br>- **Lô 1 (`DL-MANG01-121126` - Trạng thái `ACTIVE`):** Đầy đủ 6 sự kiện canh tác đã duyệt, có `salt` 64 hex, `merkle_snapshots` khớp thực tế, bản ghi `anchors` chứa `txHash` thật trên mạng Polygon Amoy, và mã QR `QR-DEMO-ACTIVE-001` → Dùng để demo **Xuất kho (`UC-15`)**, **Nhận hàng (`UC-16`)**, **Tra cứu (`UC-17`)** và **Kiểm chứng `MATCH` (`UC-18`)**.<br>- **Lô 2 (`DL-MANG01-101126` - Trạng thái `GROWING`):** Dùng để demo cảnh báo lô chưa kích hoạt và lỗi `409` khi cố tình xuất kho sớm.<br>- **Lô 3 (`DL-MANG01-051126` - Trạng thái `RECALLED`):** Dùng để demo màn hình cảnh báo đỏ khi lô bị thu hồi. | Chạy `liquibase update` trên DB sạch → Có ngay 3 kịch bản lô hoàn chỉnh để trình diễn mà không cần nhập tay từ đầu. |
| **S3-5** | `docs: add 5-minute demo script and progress report for 18/11 deadline` | Tạo `docs/DEMO-18-11.md` (hướng dẫn khởi chạy nhanh, kịch bản thuyết trình từng phút cho SHP & VER) và `docs/BAO-CAO-TIEN-DO-SHP-VER.md` (bảng đối chiếu 100% FR/NFR đã hoàn thành kèm ảnh chụp màn hình). | Tài liệu rõ ràng, bất kỳ thành viên nào trong nhóm đọc cũng có thể chạy lại bản demo trong 10 phút. |

---

## 7. Ma trận nghiệm thu (Acceptance Matrix)

| Mã yêu cầu | Nội dung yêu cầu (rút gọn) | Thuộc Commit | Phương pháp kiểm chứng nghiệm thu |
|---|---|---|---|
| `FR-SHP-001` | Ghi xuất kho (điểm đến, biển số, thời gian, số lượng); chuyển lô sang `SHIPPED`; chặn nếu lô chưa `ACTIVE`. | `S1-1` .. `S1-6`, `S3-2` | Unit Test + Demo trực tiếp trên màn hình `SCR-11`. |
| `FR-SHP-002` | Đối tác xác nhận nhận hàng (`RECEIVED`) hoặc từ chối (`REJECTED` kèm lýể trả lô về `ACTIVE`). | `S1-2`, `S1-4` .. `S1-7`, `S3-2` | Unit Test + Demo trực tiếp trên màn hình `SCR-11`. |
| `FR-VER-001` | `GET /api/v1/public/trace/{code}` trả hành trình rút gọn, không lộ PII hay sản lượng; đếm lượt quét QR (`FR-PCK-006`). | `S2-2`, `S2-3`, `S2-4`, `S3-3` | Unit Test + Kiểm tra JSON response trên DevTools. |
| `FR-VER-002` | Hiển thị timeline dọc, thẻ chỉ số Brix/cân nặng, trạng thái xác thực; cảnh báo rõ lô chưa `ACTIVE` hoặc `RECALLED`. | `S2-3`, `S3-3`, `S3-4` | Demo trực tiếp trên màn hình `PUB-01` với 3 mã lô mẫu. |
| `FR-VER-003` | `POST /api/v1/public/trace/{code}/verify` dựng lại Merkle Root và đối chiếu Polygon (`MATCH`/`MISMATCH`/`UNAVAILABLE`). | `S0-3`, `S2-1`, `S2-5` | Unit Test + Demo bấm nút kiểm chứng trên hộp thoại `PUB-02`. |
| `FR-VER-004` | Hiển thị `txHash`, số `block`, thời điểm neo chuẩn xác và link mở `PolygonScan`. | `S2-1`, `S2-5`, `S3-3` | Demo bấm link chuyển sang trang Amoy PolygonScan thật. |
| `FR-VER-005` | Không yêu cầu cài ví/đăng nhập; giới hạn tần suất theo IP (`429 Too Many Requests`). | `S2-4`, `S2-6` | Test bắn 31 requests liên tiếp bằng script/curl. |
| `FR-VER-006` | Xuất gói bằng chứng (`proof-bundle`) đã khử PII kèm script tự kiểm độc lập. | `S0-3`, `S2-7`, `S2-8` | Tải JSON từ API và chạy `python scripts/verify_proof.py` báo `MATCH`. |
| `FR-VER-007` | Khi Polygon RPC lỗi vẫn trả hành trình bình thường, báo chưa kiểm chứng được và dùng cache anchor. | `S2-1`, `S2-5` | Test đổi URL RPC sang cổng chết, trang tra cứu vẫn lên bình thường. |
| `FR-AUD-001` | Ghi Audit log bất biến với đúng thông tin người dùng thật và IP cho mọi thao tác ghi của SHP/VER. | `S0-1`, `S1-5` | Truy vấn bảng `audit_logs` sau khi demo xuất/nhận hàng. |
| `FR-AUTH-002` | Kiểm tra quyền theo vai trò (RBAC) chặt chẽ ở mọi endpoint phía server. | `S0-4`, `S1-6`, `S1-7`, `S2-7` | Test gọi API bằng token sai role trả về `403 Forbidden`. |

---

## 8. Quản trị rủi ro & Thứ tự ưu tiên cắt giảm Scope (nếu thiếu thời gian trước 17/11)

| # | Rủi ro thực tế | Mức độ | Giải pháp kỹ thuật đã thiết kế trong kế hoạch |
|---|---|---|---|
| **R1** | Mục 3.4 trong file `.docx` cũ bị hỏng không lấy lại được. | Đã xảy ra | Khoá ngay **`ADR-001`** ngày 12/11 theo đúng chuẩn `keccak256` + `RFC8785` của file `NLU-ECOFARM-API.yaml`; anh viết sẵn package `common.crypto` để cả hệ thống dùng chung 1 hàm băm duy nhất. |
| **R2** | Contract chưa kịp deploy lên Amoy hoặc giao dịch neo chỉ là self-transfer (`V-G3`). | Cao | Áp dụng cơ chế đọc kép **`ADR-002`** trong `S2-1`: Tự động đọc từ contract nếu có, hoặc giải mã trường `tx.input` của giao dịch self-transfer, hoặc trả về `UNAVAILABLE` hợp lệ theo `FR-VER-007`. |
| **R3** | Các bảng của thành viên khác (`qr_codes`, `cultivation_events`, `qc_records`) chưa xong trước 16/11. | Cao | Đã thiết kế sẵn cơ chế đọc fallback từ bảng `outbox_events` (đã có trong repo) và nạp sẵn dữ liệu mẫu tự chủ trong `005-demo-data.sql` (`S3-4`). |
| **R4** | Quá tải thời gian do chỉ có 1 người làm cả Backend lẫn Frontend trong 6 ngày. | Cao | **Thứ tự được phép rút gọn nếu sát giờ (Cut-line):**<br>1. Giữ giao diện `SCR-11` ở mức form chuẩn tối giản, không làm hiệu ứng phức tạp.<br>2. Phần đính kèm ảnh nhận hàng (`FR-SHP-002` - mức *Could*): Cho phép nhập URL ảnh hoặc tải ảnh base64/presigned URL đơn giản.<br>3. **Tuyệt đối KHÔNG được cắt:** `S0-1`, `S0-3`, `S1-3`, `S1-4`, `S2-3`, `S2-5`, `S3-3`. |
| **R5** | Xung đột code (Merge Conflict) khi nhiều người cùng sửa `persistence.xml` hoặc `BlockchainResource`. | Trung bình | Anh **không** sửa `persistence.xml` và **tách riêng** các class mới (`ShipmentResource`, `PublicTraceService`, `VerificationService`). Chỉ sửa đúng 3 dòng `@RequireRoles` trong `BlockchainResource` ở commit `S1-7`. |

---

## 9. Tiêu chuẩn hoàn thành (Definition of Done - DoD)

Một yêu cầu chức năng (FR) của anh chỉ được đánh dấu là **HOÀN THÀNH (DONE)** khi thỏa mãn đủ 6 tiêu chí:
1. **Kiến trúc sạch:** Đúng luồng `Resource → Service → Repository`, có `@Transactional` cho mọi thao tác ghi DB.
2. **Validation & Error Handling:** Kiểm tra chặt đầu vào và máy trạng thái ở phía Server; trả đúng mã lỗi HTTP (`400/401/403/404/409/422/429`) theo chuẩn RFC 7807.
3. **Audit Log:** Ghi nhận đầy đủ vào bảng `audit_logs` với đúng `actor_id` và `actor_name` thật (không phải `"admin"` giả của filter cũ).
4. **Kiểm thử:** Pass toàn bộ JUnit 5 tests (`mvn test` xanh) và test thực tế trên giao diện UI.
5. **Khớp OpenAPI:** Đường dẫn và cấu trúc JSON trả về khớp với file `NLU-ECOFARM-API.yaml`.
6. **Bảo mật & Dữ liệu thật:** Không rò rỉ PII hay sản lượng ra API public; xoá sạch 100% các chuỗi hardcode giả (`BATCH-EXAMPLE`, `0xfaceb00c`, `0xdeadbeefcafe1234`).

---

## 10. Checklist bàn giao cuối cùng (Ngày 18/11/2026)

**Hồ sơ tài liệu:**
- [ ] `docs/ADR-001-hash-merkle.md` đã hoàn thiện kèm biên bản ghi nhận sự cố hỏng file `.docx` cũ và giải pháp chuẩn hóa.
- [ ] `docs/DEMO-18-11.md`: Kịch bản chạy demo từng bước trong 5 phút.
- [ ] `docs/BAO-CAO-TIEN-DO-SHP-VER.md`: Bảng kết quả nghiệm thu các FR/NFR của SHP & VER kèm ảnh chụp màn hình thực tế.

**Kỹ thuật & Mã nguồn:**
- [ ] `mvn clean package` và `mvn test` xanh 100% · `npm run build` trong `ecofarm_frontend` xanh 100%.
- [ ] `liquibase update` chạy mượt từ cơ sở dữ liệu trống (tự động tạo bảng `shipments`, `qr_codes`, nạp roles và dữ liệu demo `005-demo-data.sql`).
- [ ] Chạy `python scripts/verify_proof.py` với gói bằng chứng xuất từ API báo `MATCH`.
- [ ] Đã chụp đủ 5 ảnh màn hình đưa vào báo cáo: (1) Xuất kho `SCR-11`, (2) Đối tác nhận hàng `SCR-11`, (3) Trang tra cứu `PUB-01` (lô `ACTIVE`), (4) Trang tra cứu `PUB-01` (lô `RECALLED`), (5) Hộp thoại xác thực `PUB-02` báo `MATCH` kèm link PolygonScan.

---

## 11. Phụ lục A — Liquibase Changeset bảng `shipments` (`003-shipments.sql`)

```sql
--liquibase formatted sql

--changeset ecofarm:003-shipments
--comment: FR-SHP-001/002 - Bang luu tru thong tin xuat kho va doi tac nhan hang
CREATE TABLE IF NOT EXISTS shipments (
    id              BIGSERIAL PRIMARY KEY,
    batch_id        BIGINT       NOT NULL,
    destination     VARCHAR(255) NOT NULL,
    vehicle_plate   VARCHAR(20)  NOT NULL,
    shipped_at      BIGINT       NOT NULL,
    received_at     BIGINT,
    received_by     BIGINT,
    quantity        INTEGER,
    status          VARCHAR(20)  NOT NULL DEFAULT 'SHIPPED',
    condition_note  TEXT,
    photo_urls      TEXT,
    created_by      BIGINT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_shipments_batch  ON shipments (batch_id);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments (status);
--rollback DROP TABLE IF EXISTS shipments;
```

## 12. Phụ lục B — Liquibase Changeset bảng `qr_codes` dự phòng (`004-qr-codes-readonly.sql`)

```sql
--liquibase formatted sql

--changeset ecofarm:004-qr-codes-readonly
--comment: FR-PCK-003/FR-PCK-006/FR-VER-001 - Mapping ma tem QR -> lo hang va dem luot quet chong sao chep
CREATE TABLE IF NOT EXISTS qr_codes (
    id              BIGSERIAL PRIMARY KEY,
    batch_id        BIGINT       NOT NULL,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    serial          INTEGER      NOT NULL DEFAULT 1,
    scan_count      INTEGER      NOT NULL DEFAULT 0,
    last_scanned_at BIGINT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_qr_codes_batch ON qr_codes (batch_id);
--rollback DROP TABLE IF EXISTS qr_codes;
```

## 13. Phụ lục C — Cấu trúc chuẩn của Script tự kiểm chứng (`scripts/verify_proof.py`)

```python
#!/usr/bin/env python3
"""
Script độc lập kiểm chứng gói bằng chứng (Proof Bundle) - Dự án NLU_ECOFARM (FR-VER-006)
Cách dùng: python scripts/verify_proof.py path/to/proof-bundle.json
"""
import json
import sys
from eth_hash.auto import keccak

def canonicalize_json(obj) -> bytes:
    """Chuẩn hóa JSON theo RFC 8785 (sắp xếp key từ điển, loại bỏ khoảng trắng)"""
    return json.dumps(obj, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode("utf-8")

def keccak256_hex(data: bytes) -> str:
    return "0x" + keccak(data).hex()

def hex_to_bytes32(hex_str: str) -> bytes:
    clean = hex_str[2:] if hex_str.startswith("0x") else hex_str
    return bytes.fromhex(clean.zfill(64))

def verify_bundle(filepath: str) -> int:
    with open(filepath, "r", encoding="utf-8") as f:
        bundle = json.load(f)

    salt_bytes = hex_to_bytes32(bundle["salt"])
    expected_root = bundle["merkleRoot"].lower()
    leaves_data = bundle.get("leaves", [])

    for item in leaves_data:
        ev_hash_bytes = hex_to_bytes32(item["eventHash"])
        computed_leaf = keccak256_hex(ev_hash_bytes + salt_bytes).lower()
        if computed_leaf != item["leaf"].lower():
            print(f"[FAIL] MISMATCH tại lá seq={item.get('seq')}: tính được {computed_leaf} != {item['leaf']}")
            return 1

        # Kiểm tra đường dẫn Merkle Proof của từng lá
        curr = hex_to_bytes32(computed_leaf)
        for step in item.get("proof", []):
            sibling = hex_to_bytes32(step["hash"])
            if step["position"].upper() == "LEFT":
                curr = keccak(sibling + curr)
            else:
                curr = keccak(curr + sibling)

        if ("0x" + curr.hex()).lower() != expected_root:
            print(f"[FAIL] MISMATCH Merkle Proof tại lá seq={item.get('seq')}")
            return 1

    print(f"[PASS] MATCH: Toàn bộ {len(leaves_data)} sự kiện và Merkle Proof khớp tuyệt đối với Root {expected_root}")
    return 0

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Sử dụng: python verify_proof.py <proof-bundle.json>")
        sys.exit(2)
    sys.exit(verify_bundle(sys.argv[1]))
```