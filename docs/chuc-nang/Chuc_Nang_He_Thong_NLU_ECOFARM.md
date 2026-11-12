# SRS – Hệ thống Blockchain Melon (Hybrid Private/Public)

### 1.1 Danh sách use case

### 1.2 Yêu cầu chức năng

Cột KC là cách kiểm chứng: T = Test, D = Demo, I = Inspection. Mọi yêu cầu ở dạng "Hệ thống phải…".

AUTH – Xác thực, phân quyền

MST – Danh mục

BAT – Lô

LOG – Nhật ký canh tác

QC – Chất lượng và thu hoạch

PCK – Đóng gói, kích hoạt, QR

SHP – Xuất kho, giao hàng

LDG – Blockchain

VER – Tra cứu và xác thực

RPT, AUD – Báo cáo, kiểm toán, thu hồi

## 1.3 Yêu cầu phi chức năng

Các con số dưới đây là mục tiêu cho môi trường demo và thử nghiệm 1 nhà màng (khoảng 5.000 quả/tuần), được đo bằng công cụ nêu ở cột cuối; nhóm nên chốt lại với giảng viên trước khi đóng băng SRS. Nhóm chất lượng theo ISO/IEC 25010.

### 1.4  Mẫu payload sự kiện (trước khi chuẩn hóa và băm)

{
  "schemaVersion": 1,
  "eventType": "THU_PHAN",
  "batchCode": "DL-MANG01-281026",
  "occurredAt": "2026-09-10T08:30:00Z",
  "actorRef": "usr_7f3a",
  "data": { "ghiChu": "Hoàn thành thụ phấn, giữ 1 quả tối ưu trên cây" },
  "attachments": [ { "sha256": "<hash tệp ảnh>" } ]
}

actorRef là mã ẩn danh nội bộ, không phải tên thật, để gói bằng chứng chia sẻ ra ngoài không lộ thông tin cá nhân.

## 2. Giao diện, API và tích hợp

Một monorepo gồm backend Jakarta EE, hợp đồng thông minh Solidity, ứng dụng Vue.js và hạ tầng; client đi qua REST /api/v1, riêng trang tra cứu công khai dùng /public.

### 2.1 Công nghệ và cấu trúc repo

Phiên bản thư viện được khóa trong package-lock.json tại tuần 5; các lựa chọn trên là gợi ý phổ biến, có thể đổi nếu nhóm quen thư viện khác. Giao diện "Mobile" trong các bảng bên dưới là Vue.js responsive dạng PWA, cài lên màn hình chính điện thoại.

NLU_ECOFARM/
  backend/      Jakarta EE 10 (Java 21), Flyway, web3j
  contracts/    Solidity: MelonLedger.sol, MelonAnchor.sol, test Hardhat/Foundry
  mobile/       Vue.js (PWA) cho công nhân, kỹ sư, đóng gói
  web-admin/    Vue.js cho Admin, báo cáo
  public-page/  Trang tra cứu Vue.js nhẹ (Vite, build tĩnh)
  infra/        docker-compose, nginx, Besu genesis, Grafana
  docs/         SRS, ADR, runbook, OpenAPI

### 2.2 Danh sách màn hình

Bố cục PUB-01 theo tài liệu ý tưởng: banner và mã lô, hộp trạng thái xác thực, thẻ Brix, cân nặng, giống, timeline dọc từ trồng đến lên kệ, nút kiểm chứng. Không yêu cầu cài ví.

### 2.3 REST API

Quy ước: JSON, lỗi theo RFC 7807 (application/problem+json), phân trang page/size, định danh lặp bằng clientEventId, spec OpenAPI 3 tự sinh bằng MicroProfile OpenAPI.

### 2.4 Tích hợp bên ngoài

- Polygon: RPC từ nhà cung cấp (Alchemy, Infura hoặc node công khai làm dự phòng); link PolygonScan sinh từ txHash. URL RPC và khóa API để trong secret.

- Besu: JSON-RPC nội bộ giữa container, không mở ra Internet.

- cloudflare: kên S3, link ảnh ký tạm (presigned URL) cho client.

- Thông báo: Web Push (FCM cho web) hoặc email SMTP qua Jakarta Mail (FR-AUD-004).

- Máy in tem: giai đoạn đầu in PDF qua máy in thông thường; kết nối máy in tem trực tiếp là Could.

| Mã | Tên | Tác nhân chính | Quan hệ | Ưu tiên |

| --- | --- | --- | --- | --- |

| UC-01 | Đăng nhập, đăng xuất | Mọi người dùng nội bộ | Được include bởi mọi UC nội bộ | Must |

| UC-02 | Quản lý người dùng, phân quyền | Admin |  | Must |

| UC-03 | Quản lý nhà màng | Admin, Kỹ sư |  | Must |

| UC-04 | Quản lý giống, chứng nhận | Admin, Kỹ sư |  | Must |

| UC-05 | Tạo lô | Kỹ sư |  | Must |

| UC-06 | Phân công công nhân cho lô | Kỹ sư |  | Should |

| UC-07 | Ghi nhật ký canh tác | Công nhân, Kỹ sư | include UC-08 | Must |

| UC-08 | Đính kèm ảnh, chứng từ | Công nhân, Kỹ sư | Được include bởi UC-07, UC-11 | Must |

| UC-09 | Duyệt nhật ký | Kỹ sư |  | Must |

| UC-10 | Đính chính sự kiện (amendment) | Kỹ sư | extend UC-07 | Should |

| UC-11 | Ghi kiểm định chất lượng (Brix, cân nặng) | Kỹ sư | include UC-08 | Must |

| UC-12 | Ghi nhận thu hoạch | Kỹ sư, Công nhân |  | Must |

| UC-13 | Kích hoạt lô (khóa dữ liệu) | Nhân viên đóng gói | include UC-14, UC-23 | Must |

| UC-14 | Sinh và in tem QR | Nhân viên đóng gói |  | Must |

| UC-15 | Xuất kho | Nhân viên kho |  | Should |

| UC-16 | Xác nhận nhận hàng | QC đối tác |  | Could |

| UC-17 | Tra cứu hành trình sản phẩm | Người tiêu dùng |  | Must |

| UC-18 | Xác thực dữ liệu trên blockchain | Người tiêu dùng, QC, Thanh tra |  | Must |

| UC-19 | Thu hồi lô | Admin |  | Should |

| UC-20 | Xem dashboard, báo cáo | Admin, Kỹ sư, Thanh tra |  | Should |

| UC-21 | Xem nhật ký kiểm toán | Admin, Thanh tra |  | Should |

| UC-22 | Đồng bộ dữ liệu offline | Công nhân | extend UC-07 | Should |

| UC-23 | Neo Merkle root, đối soát chuỗi | Hệ thống (Outbox), Admin | Được include bởi UC-13 | Must |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-AUTH-001 | Đăng nhập bằng tài khoản và mật khẩu, cấp JWT access token ngắn hạn và refresh token | UC-01 | Must | T |

| FR-AUTH-002 | Kiểm tra quyền theo vai trò (RBAC) ở mọi endpoint phía server, không dựa vào ẩn/hiện nút trên app | UC-01, UC-02 | Must | T |

| FR-AUTH-003 | Khóa tạm tài khoản sau số lần đăng nhập sai liên tiếp (cấu hình được) | UC-01 | Should | T |

| FR-AUTH-004 | Lưu mật khẩu dạng bcrypt hoặc Argon2; bắt đổi mật khẩu lần đầu | UC-01 | Must | I, T |

| FR-AUTH-005 | Admin tạo, khóa, mở khóa người dùng và gán vai trò | UC-02 | Must | D |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-MST-001 | Quản lý nhà màng: mã, tên, tọa độ GPS, diện tích, trạng thái | UC-03 | Must | D |

| FR-MST-002 | Quản lý giống dưa: mã, tên, số ngày từ thụ phấn đến thu hoạch dự kiến, ngưỡng Brix tối thiểu, số ngày cách ly tối thiểu | UC-04 | Must | D |

| FR-MST-003 | Quản lý chứng nhận VietGAP/GlobalGAP: số, hiệu lực, tệp PDF; cảnh báo trước khi hết hạn | UC-04 | Should | D |

| FR-MST-004 | Không cho đổi mã nhà màng, mã giống khi đã có lô tham chiếu | UC-03, UC-04 | Must | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-BAT-001 | Tạo lô với mã tự sinh dạng DL-<mã nhà màng>-<ddMMyy>, thêm hậu tố số nếu trùng ngày | UC-05 | Must | T |

| FR-BAT-002 | Mỗi lô gắn đúng 1 nhà màng, 1 giống, ngày xuống giống dự kiến | UC-05 | Must | T |

| FR-BAT-003 | Sinh salt ngẫu nhiên 32 byte khi tạo lô và lưu dạng mã hóa | UC-05 | Must | I |

| FR-BAT-004 | Chỉ cho chuyển trạng thái theo Hình 3.5; chuyển sai trả 409 | UC-05 đến UC-19 | Must | T |

| FR-BAT-005 | Phân công công nhân cho lô; công nhân chỉ thấy lô được giao | UC-06 | Should | T |

| FR-BAT-006 | Tự tính ngày thu hoạch dự kiến từ ngày thụ phấn và số ngày theo giống | UC-07 | Should | T |

| FR-BAT-007 | Từ trạng thái ANCHORING chặn mọi thêm, sửa sự kiện canh tác | UC-13 | Must | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-LOG-001 | Ghi sự kiện gồm loại, thời điểm xảy ra (do thiết bị), thời điểm ghi (do server), người ghi, dữ liệu theo JSON Schema có phiên bản của loại đó | UC-07 | Must | T |

| FR-LOG-002 | Hỗ trợ tối thiểu các loại: XUONG_GIONG, THU_PHAN, BON_PHAN_TUOI, NGUNG_PHAN_CACH_LY, DO_BRIX, THU_HOACH, GHI_CHU | UC-07 | Must | D |

| FR-LOG-003 | Kiểm tra thứ tự và điều kiện nghiệp vụ (thụ phấn sau xuống giống, ngừng phân trước thu hoạch đủ số ngày cách ly của giống); sai trả 422 kèm lý do | UC-07 | Must | T |

| FR-LOG-004 | Xử lý idempotent theo clientEventId: gửi lại không tạo sự kiện trùng | UC-07, UC-22 | Must | T |

| FR-LOG-005 | Sự kiện bất biến: không có API sửa hay xóa, chỉ có thêm | UC-07 | Must | T, I |

| FR-LOG-006 | Sau khi lưu, tính eventHash theo quy tắc mục 3.4 và ghi outbox trong cùng một transaction | UC-07 | Must | T |

| FR-LOG-007 | Kỹ sư duyệt hoặc từ chối sự kiện; duyệt sinh sự kiện EVENT_APPROVED tham chiếu eventHash và cũng ghi chuỗi | UC-09 | Must | T |

| FR-LOG-008 | Đính chính tạo sự kiện mới có amends_event_id; sự kiện cũ đánh dấu SUPERSEDED nhưng vẫn hiển thị trong lịch sử | UC-10 | Should | T |

| FR-LOG-009 | Đính kèm ảnh (giới hạn số ảnh và dung lượng mỗi ảnh, app nén trước), lưu MinIO; SHA-256 của tệp nằm trong payload để được hash cùng sự kiện | UC-08 | Must | T |

| FR-LOG-010 | App ghi offline và đồng bộ theo thứ tự; chênh lệch giữa giờ thiết bị và giờ server vượt ngưỡng thì gắn cờ cần rà soát | UC-22 | Should | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-QC-001 | Ghi Brix (°Bx, 1 chữ số thập phân), cân nặng trung bình (kg), kích thước, ghi chú, ảnh | UC-11 | Must | T |

| FR-QC-002 | Tự tính đạt/không đạt theo ngưỡng của giống; Kỹ sư xác nhận kết quả cuối | UC-11 | Must | T |

| FR-QC-003 | Cảnh báo và chặn QC đạt khi thời gian cách ly trước thu hoạch chưa đủ | UC-11 | Must | T |

| FR-QC-004 | Ghi nhận thu hoạch: thời gian, số lượng quả ước tính, khu vực; chuyển lô sang HARVESTED | UC-12 | Must | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-PCK-001 | Chỉ kích hoạt khi lô QC_PASSED, có thu hoạch, mọi sự kiện đã CONFIRMED và đã duyệt hoặc SUPERSEDED; thiếu thì trả 409 kèm danh sách | UC-13 | Must | T |

| FR-PCK-002 | Khi kích hoạt: dựng Merkle theo mục 3.4, lưu snapshot, chuyển ANCHORING, tạo outbox finalize và anchor trong cùng một transaction | UC-13 | Must | T |

| FR-PCK-003 | Sinh mã QR cho từng đơn vị đóng gói, dạng URL /t/<code> với code ngẫu nhiên khó đoán; chỉ lô ACTIVE mới được in | UC-14 | Must | T |

| FR-PCK-004 | Xuất tệp in tem (PDF); in lại đánh dấu reprint và giữ nguyên mã. ZPL/CSV cho máy in tem là Could | UC-14 | Should | D |

| FR-PCK-005 | Số tem sinh ra không vượt số đơn vị khai báo; sinh thêm phải có lý do và ghi audit | UC-14 | Should | T |

| FR-PCK-006 | Đếm lần quét mỗi mã, cảnh báo khi bất thường (chống sao chép tem) | UC-17 | Could | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-SHP-001 | Ghi xuất kho: điểm đến, biển số xe, thời gian, số lượng; chuyển lô sang SHIPPED | UC-15 | Should | T |

| FR-SHP-002 | Đối tác xác nhận nhận hàng: thời gian, tình trạng, ảnh | UC-16 | Could | D |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-LDG-001 | Mọi thao tác ghi chuỗi đi qua outbox, giao tối thiểu một lần và idempotent theo (batchIdHash, seq) | UC-23 | Must | T |

| FR-LDG-002 | Retry theo backoff tăng dần; quá số lần cấu hình thì ANCHOR_FAILED và cảnh báo Admin | UC-23 | Must | T |

| FR-LDG-003 | Chờ đủ N confirmations (cấu hình) trên Public Chain trước khi lô ACTIVE | UC-13 | Must | T |

| FR-LDG-004 | Đặt trần gas price, cảnh báo khi số dư ví Public thấp | UC-23 | Must | T |

| FR-LDG-005 | Job đối soát hằng ngày: root trong DB, Private Chain, Public Chain; lệch thì cảnh báo và ghi audit | UC-23 | Should | T |

| FR-LDG-006 | Đổi mạng Public (Amoy sang mainnet) bằng cấu hình; mỗi anchor lưu chain_id và contract_address | UC-23 | Must | I |

| FR-LDG-007 | Khóa ký lấy từ keystore hoặc secret manager, không ghi log, không để trong mã nguồn | UC-23 | Must | I |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-VER-001 | GET /public/trace/{code} trả hành trình rút gọn: giống, nhà màng, mốc ngày chính, Brix, cân nặng, chứng nhận, trạng thái; không có thông tin cá nhân hay sản lượng | UC-17 | Must | T |

| FR-VER-002 | Hiển thị timeline dọc, thẻ chỉ số, trạng thái xác thực; báo rõ khi lô chưa ACTIVE hoặc đã thu hồi | UC-17 | Must | D |

| FR-VER-003 | POST /public/trace/{code}/verify dựng lại root và so với Polygon; trả MATCH, MISMATCH hoặc UNAVAILABLE | UC-18 | Must | T |

| FR-VER-004 | Hiển thị txHash, block, thời điểm neo và link PolygonScan | UC-18 | Must | D |

| FR-VER-005 | Không yêu cầu ví hay đăng nhập; giới hạn tần suất theo IP | UC-17, UC-18 | Must | T |

| FR-VER-006 | Gói bằng chứng cho QC và Thanh tra (cần đăng nhập): sự kiện đã khử thông tin cá nhân, salt, các leaf, Merkle proof, anchor tx, kèm script tự kiểm | UC-18 | Should | D |

| FR-VER-007 | Khi Polygon RPC lỗi: vẫn trả hành trình, báo chưa kiểm chứng được; cache anchor đã đọc | UC-18 | Should | T |

| Mã | Yêu cầu | UC | Ưu tiên | KC |

| --- | --- | --- | --- | --- |

| FR-RPT-001 | Dashboard: số lô theo trạng thái, tỷ lệ QC đạt, lô đang chờ neo, lô lỗi | UC-20 | Should | D |

| FR-RPT-002 | Xuất báo cáo lô ra PDF hoặc CSV | UC-20 | Could | D |

| FR-AUD-001 | Ghi audit mọi thao tác ghi dữ liệu và đăng nhập: ai, khi nào, đối tượng, IP | UC-21 | Must | T |

| FR-AUD-002 | Audit log chỉ thêm, Admin không xóa được | UC-21 | Must | T |

| FR-AUD-003 | Thu hồi lô: Admin nhập lý do, lô sang RECALLED, gọi revoke trên Polygon, trang tra cứu hiện cảnh báo | UC-19 | Should | T |

| FR-AUD-004 | Thông báo (push hoặc email) khi lô ACTIVE, ANCHOR_FAILED, ví gas thấp | UC-23 | Should | D |

| Mã | Yêu cầu | Cách đo |

| --- | --- | --- |

| NFR-PERF-001 | Trang tra cứu hiển thị nội dung chính dưới 1,5 giây (p95) trên 4G; tổng dung lượng trang dưới 500 KB, mỗi ảnh dưới 150 KB | Lighthouse, WebPageTest giả lập 4G |

| NFR-PERF-002 | API ghi sự kiện trả lời dưới 500 ms (p95), không chờ chuỗi | k6 |

| NFR-PERF-003 | Từ lúc lưu đến CONFIRMED trên Private Chain dưới 30 giây (p95) | Metric outbox lag |

| NFR-PERF-004 | Từ lúc kích hoạt đến ACTIVE thường dưới 10 phút (phụ thuộc mạng Polygon và số confirmations N) | Đo thử trên Amoy |

| NFR-PERF-005 | API công khai chịu 50 yêu cầu/giây, p95 dưới 1 giây; 50 người dùng nội bộ đồng thời | k6 |

| NFR-PERF-006 | Kích hoạt lô 5.000 đơn vị (dựng Merkle, sinh QR) dưới 60 giây | Test hiệu năng |

| NFR-SEC-001 | Mọi kết nối dùng TLS 1.2 trở lên, bật HSTS | SSL Labs |

| NFR-SEC-002 | Chống các lỗi trong OWASP Top 10; quét OWASP ZAP không còn lỗ hổng mức High | ZAP, review mã |

| NFR-SEC-003 | Khóa ký không nằm trong Git hay ảnh Docker; ví Private và Public tách riêng; ví Public chỉ giữ số dư tối thiểu | Quét bí mật (gitleaks), review |

| NFR-SEC-004 | Phân quyền ở mức đối tượng: công nhân chỉ truy cập lô được giao | Test phân quyền |

| NFR-SEC-005 | Giới hạn tần suất cho API công khai và đăng nhập | Test tải |

| NFR-PRV-001 | Không đưa PII hay sản lượng lên Public Chain; hợp đồng Public chỉ nhận bytes32 | Test contract, review |

| NFR-REL-001 | Không mất sự kiện đã trả 201 dùng khi chuỗi lỗi hoặc worker bị tắt giữa chừng | Test phá hoại (kill worker, tắt node) |

| NFR-REL-002 | Sao lưu PostgreSQL hằng ngày, khôi phục thử ít nhất 1 lần trước khi nộp (RPO tối đa 24 giờ, RTO tối đa 4 giờ) | Biên bản khôi phục |

| NFR-REL-003 | Ứng dụng không mất hàng đợi offline (IndexedDB) khi bị đóng đột ngột | Test thủ công trên thiết bị |

| NFR-AVL-001 | Trang tra cứu sẵn sàng 99% trong tháng chạy thử | Uptime monitor |

| NFR-USA-001 | Ghi một sự kiện chuẩn trong tối đa 5 lần chạm và 60 giây, ưu tiên chọn và quét QR hơn gõ chữ | Thử với 3 công nhân thật |

| NFR-USA-002 | Giao diện tiếng Việt; chữ từ 16px trên giao diện; tương phản tối thiểu 4,5:1 để đọc ngoài nắng | Kiểm tra contrast |

| NFR-USA-003 | Trang tra cứu chạy tốt trên màn hình 360 px, Chrome và Safari hai phiên bản gần nhất | Test trên thiết bị |

| NFR-CMP-001 | Ứng dụng Vue.js (PWA) chạy trên Chrome Android 8.0 trở lên và Safari iOS; web admin chạy trên Chrome và Edge mới | Test thiết bị |

| NFR-CMP-002 | Triển khai bằng một lệnh Docker Compose trên Ubuntu 22.04 | Chạy trên VPS sạch |

| NFR-MNT-001 | Test bao phủ từ 70% dòng cho tầng domain và application; contract được test mọi hàm | JaCoCo, Vitest, Hardhat/Foundry |

| NFR-MNT-002 | CI chạy build, test, lint mỗi pull request; migration bằng Flyway; OpenAPI tự sinh | GitHub Actions |

| NFR-MNT-003 | Chuỗi nằm sau port; có bản giả lập trong bộ nhớ để test không cần mạng | Review kiến trúc |

| NFR-MNT-004 | Có README, runbook triển khai, danh sách ADR | Inspection |

| NFR-OBS-001 | Log có correlationId; metric Prometheus cho outbox lag, anchor lỗi, số dư ví; có cảnh báo | Grafana |

| NFR-OBS-002 | Có endpoint health cho API, DB, MinIO, hai chuỗi | /health/ready |

| Mục | Lựa chọn | Mục đích |

| --- | --- | --- |

| Quản lý trạng thái, điều hướng | Vue 3 (Composition API), Pinia, Vue Router | Tách UI và logic, deep link |

| Gọi API | Axios + interceptor JWT, refresh tự động | Xác thực, retry |

| Lưu offline | IndexedDB (Dexie) + Service Worker (Workbox, PWA) | Hàng đợi sự kiện, cache danh sách lô |

| Quét QR, chụp ảnh | html5-qrcode, thẻ input chụp ảnh (capture), browser-image-compression | Chọn lô nhanh, nén ảnh trước khi gửi |

| Lưu token | Access token giữ trong bộ nhớ, refresh token cookie HttpOnly | Bảo mật phiên |

| Sinh mã API client | openapi-typescript-codegen từ spec của backend | Tránh lệch hợp đồng |

| Backend | Jakarta EE 10: JAX-RS, CDI, JPA, Bean Validation, JTA | REST API, giao dịch, truy cập PostgreSQL |

| Xác thực, phân quyền | Jakarta Security + MicroProfile JWT, @RolesAllowed | JWT, RBAC kiểm tra ở server |

| Tác vụ nền | Jakarta Concurrency (ManagedScheduledExecutorService) hoặc EJB Timer | Outbox worker, job đối soát hằng ngày |

| Hợp đồng thông minh | Solidity ^0.8.24, OpenZeppelin, Hardhat/Foundry; web3j sinh wrapper từ ABI | MelonLedger (Besu), MelonAnchor (Polygon) |

| Mã | Màn hình | Nền tảng, vai trò | UC |

| --- | --- | --- | --- |

| SCR-01 | Đăng nhập | Mobile, Web, mọi vai trò nội bộ | UC-01 |

| SCR-02 | Danh sách lô của tôi | Web | UC-07 |

| SCR-03 | Chi tiết lô và timeline | Web | UC-07, UC-09 |

| SCR-04 | Ghi sự kiện (biểu mẫu theo loại) | Web, Công nhân, Kỹ sư | UC-07, UC-08 |

| SCR-05 | Quét QR cổng nhà màng | Web | UC-07 |

| SCR-06 | Hàng đợi đồng bộ | Web | UC-22 |

| SCR-07 | Duyệt nhật ký, đính chính | Web, Kỹ sư | UC-09, UC-10 |

| SCR-08 | Nhập QC và thu hoạch | Web, Kỹ sư | UC-11, UC-12 |

| SCR-09 | Kích hoạt lô và tiến trình neo | Web, Đóng gói | UC-13 |

| SCR-10 | In tem QR | Web, Đóng gói | UC-14 |

| SCR-11 | Xuất kho, nhận hàng | Web | UC-15, UC-16 |

| SCR-20 | Dashboard | Web, Admin, Thanh tra | UC-20 |

| SCR-21 | Người dùng và vai trò | Web, Admin | UC-02 |

| SCR-22 | Danh mục nhà màng, giống, chứng nhận | Web | UC-03, UC-04 |

| SCR-23 | Nhật ký kiểm toán | Web | UC-21 |

| SCR-24 | Lô lỗi neo và đối soát | Web, Admin | UC-23, UC-19 |

| PUB-01 | Trang hành trình sản phẩm (banner, thẻ chỉ số, timeline) | Trình duyệt, người mua | UC-17 |

| PUB-02 | Hộp thoại xác thực Blockchain (hash, txHash, link PolygonScan) | Trình duyệt, người mua | UC-18 |

| Phương thức và đường dẫn | Mô tả | Vai trò | FR |

| --- | --- | --- | --- |

| POST /api/v1/auth/login, POST /auth/refresh, GET /me | Đăng nhập, làm mới token, hồ sơ | Mọi vai trò | AUTH-001 |

| GET, POST /greenhouses, /varieties, /certificates | Danh mục | Admin, Kỹ sư | MST-001..003 |

| POST /batches, GET /batches, GET /batches/{id} | Tạo, liệt kê, chi tiết lô | Kỹ sư (xem: theo quyền) | BAT-001..004 |

| POST /batches/{id}/assignments | Phân công | Kỹ sư | BAT-005 |

| POST, GET /batches/{id}/events | Ghi, liệt kê sự kiện | Công nhân, Kỹ sư | LOG-001..006 |

| POST /events/{id}/approve, /reject, /amend | Duyệt, từ chối, đính chính | Kỹ sư | LOG-007, 008 |

| POST /batches/{id}/qc, POST /batches/{id}/harvest | QC, thu hoạch | Kỹ sư | QC-001..004 |

| POST /batches/{id}/activate, GET /batches/{id}/anchor-status | Kích hoạt, theo dõi neo | Đóng gói | PCK-001, 002, LDG-003 |

| GET /batches/{id}/qr | Tệp in tem (PDF) | Đóng gói | PCK-003, 004 |

| POST /batches/{id}/shipments, POST /shipments/{id}/receive | Xuất kho, nhận hàng | Kho, QC đối tác | SHP-001, 002 |

| POST /batches/{id}/recall | Thu hồi | Admin | AUD-003 |

| GET /batches/{id}/proof-bundle | Gói bằng chứng | QC đối tác, Thanh tra | VER-006 |

| GET /reports/dashboard, GET /audit-logs | Báo cáo, kiểm toán | Admin, Thanh tra | RPT-001, AUD-001 |

| POST /admin/anchors/{id}/retry, GET /admin/reconciliation | Thử lại neo, đối soát | Admin | LDG-002, 005 |

| GET /public/trace/{code} | Hành trình công khai | Không cần đăng nhập | VER-001 |

| POST /public/trace/{code}/verify | Xác thực blockchain | Không cần đăng nhập | VER-003 |