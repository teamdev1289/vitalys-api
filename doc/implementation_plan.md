# Kế hoạch Triển khai Chi tiết Dự án Vitalys (LIMS / SDMS / ELN)
> **Kiến trúc**: Spring Boot 3 (Backend) + Angular Standalone (Frontend) + PostgreSQL  
> **Tuân thủ**: 21 CFR Part 11, GxP, ALCOA+ Data Integrity  
> **Cơ chế thực thi**: AI triển khai từng Step cụ thể, User duyệt (Approve) theo từng chặng.

---

## 🧭 Cấu trúc quy trình phê duyệt (Approval Workflow)

Mỗi Phase được chia thành các **Step nguyên tử (Atomic Steps)**. Khi thực hiện:
1. AI trình bày chi tiết code thay đổi của Step đó.
2. Thực hiện build/test xác nhận không lỗi biên dịch và chạy đúng logic.
3. User kiểm tra và duyệt (**Approve**) trước khi AI chuyển sang Step tiếp theo.

---

## 📋 Danh sách các Phase

- [x] **Phase 1: Master Data & Hạ tầng danh mục gốc** (`equipment`, `partner`, `product`)
- [x] **Phase 2: Core LIMS - Phương pháp thử, Biểu mẫu động & Tiêu chuẩn** (`method`, `SpecificationSet`, `FormTemplate`)
- [x] **Phase 3: Quản lý vòng đời mẫu & Chuỗi giám sát** (`sample`, `TestRequest`, `ChainOfCustody`, Barcode)
- [x] **Phase 4: Phân công phân tích, Nhập kết quả & Kiểm soát OOS** (`testing`, `AnalyticalRun`, `ResultRevision`)
- [x] **Phase 5: Phê duyệt nhiều cấp, Chữ ký điện tử 21 CFR Part 11 & Báo cáo COA PDF** (`approval`, `ESignature`)
- [x] **Phase 6: Kho dữ liệu khoa học (SDMS) & Universal Viewer đồ thị sắc ký/phổ** (`sdms`, File Ingestion, ECharts)
- [x] **Phase 8: Báo cáo điều hành (Dashboards), Connectors tích hợp & Data Retention**

---

## 🔹 PHASE 1: Master Data & Hạ tầng danh mục gốc

### Mục tiêu Phase 1
Xây dựng nền tảng danh mục thiết bị phòng lab, khách hàng/dự án và sản phẩm/lô sản xuất. Mọi phép thử nghiệm và lấy mẫu ở các phase sau đều phải tham chiếu tới các danh mục này.

---

### Step 1.1: Module Thiết bị phòng Lab (`equipment`) - Backend
- [x] **1.1.1**: Tạo DTOs trong `com.vitalys.modules.equipment.dto`:
  - `InstrumentCreateRequest`, `InstrumentUpdateRequest`, `InstrumentResponse`
  - `CalibrationRecordRequest`, `CalibrationRecordResponse`
  - `MaintenanceRecordRequest`, `MaintenanceRecordResponse`
- [x] **1.1.2**: Implement `InstrumentService` & `InstrumentServiceImpl`:
  - CRUD thiết bị, lọc theo trạng thái (`IN_SERVICE`, `MAINTENANCE`, `OUT_OF_SERVICE`).
  - Thêm bản ghi hiệu chuẩn (`CalibrationRecord`), tự động tính `nextDue` và cập nhật trạng thái thiết bị.
  - Thêm bản ghi bảo dưỡng (`MaintenanceRecord`).
- [x] **1.1.3**: Implement `InstrumentController` (`/api/v1/instruments`):
  - Phân quyền `@PreAuthorize` với `EQUIPMENT:INSTRUMENT:*`
  - Đầy đủ Swagger OpenAPI annotations, paging, filtering.
- [x] **1.1.4**: Unit & Integration Test cho `InstrumentService`.

### Step 1.2: Module Thiết bị phòng Lab (`equipment`) - Frontend Angular
- [x] **1.2.1**: Tạo models & service trong `vitalys-ui/src/app/core`:
  - `equipment.model.ts`, `equipment.service.ts`
- [x] **1.2.2**: Tạo module giao diện `vitalys-ui/src/app/features/equipment`:
  - `instrument-list.component`: Bảng danh sách thiết bị, badge trạng thái, bộ lọc khoa/phòng ban, KPI thống kê.
  - `instrument-detail.component`: Chi tiết thông số máy + tab Lịch sử hiệu chuẩn & Bảo trì.
  - `instrument-form-dialog.component`: Dialog thêm/sửa thiết bị và ghi log hiệu chuẩn/bảo trì mới.
- [x] **1.2.3**: Đăng ký router `/equipment` trong `app.routes.ts` & menu sidebar navigation.

### Step 1.3: Module Khách hàng & Dự án (`partner`) - Backend & Frontend
- [x] **1.3.1**: Backend DTO, Service, Controller cho `Customer` và `Project`:
  - Quản lý thông tin khách hàng ủy thác kiểm nghiệm, danh sách dự án trực thuộc.
  - Endpoints: `/api/v1/customers`, `/api/v1/projects`.
- [x] **1.3.2**: Frontend Angular:
  - Feature `features/partner`: Quản lý khách hàng, liên hệ và dự án.
  - Dialog thêm/sửa khách hàng & dự án nghiên cứu phân tích.

### Step 1.4: Module Sản phẩm & Lô sản xuất (`product`) - Backend & Frontend
- [x] **1.4.1**: Backend DTO, Service, Controller cho `Product`, `Ingredient`, `Formulation`, `Batch`:
  - CRUD sản phẩm dược/hóa chất, dạng bào chế, hạn dùng, thành phần hoạt chất (`ProductActiveIngredient`).
  - Quản lý công thức chuẩn (`Formulation`) và tạo lô sản xuất (`Batch`).
  - Endpoints: `/api/v1/products`, `/api/v1/batches`.
- [x] **1.4.2**: Frontend Angular:
  - Feature `features/product`: Danh mục sản phẩm, quản lý danh sách lô (Batch list) với trạng thái hạn dùng.
- [x] **Checkpoint Nghiệm thu Phase 1**: Khởi tạo được 1 thiết bị HPLC, 1 khách hàng và 1 sản phẩm Paracetamol Lô 20260901 trên UI.

---

## 🔹 PHASE 2: Core LIMS - Phương pháp thử, Biểu mẫu động & Tiêu chuẩn

### Mục tiêu Phase 2
Định nghĩa phương pháp thử (SOP), quản lý bộ tiêu chuẩn kỹ thuật (Specification Set - Min/Max) và cơ chế Dynamic Form cho phép mỗi phương pháp thử có các trường nhập liệu kết quả riêng.

---

### Step 2.1: Quản lý Phương pháp thử & Thẩm định (`method`) - Backend
- [x] **2.1.1**: DTO & Service cho `Method`, `MethodStepItem`, `MethodValidationProtocol`.
- [x] **2.1.2**: Quản lý versioning của SOP (Draft ➔ Validated ➔ Released ➔ Deprecated).
- [x] **2.1.3**: Controller `/api/v1/methods`.

### Step 2.2: Tiêu chuẩn kỹ thuật chấp nhận (`SpecificationSet` & `SpecificationItem`) - Backend
- [x] **2.2.1**: DTO, Service, Controller cho `SpecificationSet`:
  - Mỗi chỉ tiêu (`SpecificationItem`) gắn với 1 Method, có giá trị min, max, đơn vị tính, tiêu chuẩn chấp nhận văn bản.
  - Hỗ trợ tiêu chuẩn theo từng quốc gia/thị trường (US/EU/VN) cho cùng 1 sản phẩm.
- [x] **2.2.2**: Endpoint tra cứu tiêu chuẩn áp dụng cho 1 Batch: `GET /api/v1/specifications/active?productId=X&market=Y`.

### Step 2.3: Dynamic Form Schema Engine (`FormTemplate` & `FormField`) - Fullstack
- [x] **2.3.1**: Backend lưu trữ Schema động của biểu mẫu thí nghiệm (JSON Schema hoặc field cấu hình: Text, Number, Formula, Select, Table).
- [x] **2.3.2**: Frontend Dynamic Form Renderer Component:
  - Nhận vào `FormTemplate` ➔ tự động render ra form Reactive Forms tương ứng.
  - Hỗ trợ validation tự động (Required, Min/Max range, Regex).
- [x] **2.3.3**: Giao diện Angular quản lý Method & Specification Matrix.
- [x] **Checkpoint Nghiệm thu Phase 2**: Tạo 1 SOP kiểm nghiệm có form nhập liệu gồm "Khối lượng cân (g)", "Diện tích pic", "Hàm lượng tính toán (%)" và bộ tiêu chuẩn chấp nhận [98.5% - 101.5%].

---

## 🔹 PHASE 3: Quản lý vòng đời Mẫu & Chuỗi giám sát (Sample Lifecycle)

### Mục tiêu Phase 3
Xây dựng luồng tiếp nhận mẫu (Accessioning), sinh mã vạch Barcode/QR, quản lý vị trí lưu trữ và truy vết đường đi của mẫu (Chain of Custody).

---

### Step 3.1: Quản lý Yêu cầu kiểm nghiệm (`TestRequest`) - Backend & Frontend
- [x] **3.1.1**: Backend `TestRequestService`: Nộp yêu cầu kiểm nghiệm từ nội bộ (Lô sản xuất) hoặc khách hàng dịch vụ.
- [x] **3.1.2**: Thiết lập độ ưu tiên (`NORMAL`, `URGENT`, `EMERGENCY`) và ngày hẹn trả kết quả.
- [x] **3.1.3**: Frontend nộp đơn yêu cầu phân tích với bộ lọc chỉ tiêu cần kiểm.

### Step 3.2: Tiếp nhận mẫu (`Sample Accessioning`) & Sinh Barcode - Fullstack
- [x] **3.2.1**: Backend sinh mã mẫu độc nhất (theo quy tắc format phòng lab: `SMP-YYYYMMDD-XXXX`) và hash mã vạch (`BAR-SMP-YYYYMMDD-XXXX`).
- [x] **3.2.2**: Frontend tích hợp thư viện in nhãn Barcode / QR Code trực tiếp ra máy in tem nhãn (50mm x 30mm layout).

### Step 3.3: Máy trạng thái mẫu (Sample State Machine) & Chuỗi giám sát (`ChainOfCustody`)
- [x] **3.3.1**: Enforce State Machine: `SUBMITTED` ➔ `RECEIVED` ➔ `ASSIGNED` ➔ `TESTING` ➔ `REVIEWED` ➔ `APPROVED` / `REJECTED` ➔ `DISPOSED` (với lý do bắt buộc GxP / 21 CFR Part 11).
- [x] **3.3.2**: Tự động ghi vết vào `SampleStatusHistory` và `SampleChainOfCustody` mỗi khi có hành động bàn giao/thay đổi vị trí cất giữ (kho mát, tủ âm sâu...).
- [x] **3.3.3**: Frontend: Màn hình quản lý mẫu dạng Table/KPI kèm timeline theo dõi vết bàn giao, dialog chuyển đổi trạng thái và bàn giao vị trí.
- [x] **Checkpoint Nghiệm thu Phase 3**: Nộp 1 phiếu yêu cầu ➔ Tiếp nhận 2 mẫu ➔ In mã vạch ➔ Chuyển giao từ Tiếp nhận mẫu cho Kho mẫu mát thành công.

---

## 🔹 PHASE 4: Phân công, Thực hiện Phân tích & Quản lý OOS

### Mục tiêu Phase 4
Môi trường làm việc của kiểm nghiệm viên (Analyst): nhận việc, thực thi bước phân tích, nhập số liệu theo form động, tự động bắt lỗi OOS (Out-of-Specification) và kiểm soát sửa đổi số liệu tuân thủ ALCOA+.

---

### Step 4.1: Phân công phép thử (`TestAssignment`) & Worklist
- [x] **4.1.1**: Phân công các phép thử cho cá nhân Analyst hoặc nhóm phân tích.
- [x] **4.1.2**: API `GET /api/v1/tests/my-worklist`: Trả về danh sách mẫu đang chờ kiểm nghiệm của Analyst đang đăng nhập.
- [x] **4.1.3**: Frontend Analyst Dashboard: Danh sách việc cần làm, sắp xếp theo thời hạn Due Date và mức độ ưu tiên.

### Step 4.2: Thực hiện phân tích & Nhập kết quả Form động
- [x] **4.2.1**: Bản ghi lượt chạy máy (`AnalyticalRun`) gắn với thiết bị ở Phase 1.
- [x] **4.2.2**: Ghi log thực hiện từng bước (`TestStepExecution`).
- [x] **4.2.3**: Lưu dữ liệu biểu mẫu động vào `FormSubmission` (JSON) và trích xuất kết quả cuối vào bảng `Result`.

### Step 4.3: Tự động phát hiện OOS & Quy trình điều tra sơ bộ
- [x] **4.3.1**: Backend so khớp `Result.value` với `SpecificationItem` ngay khi nhập:
  - Nếu nằm ngoài khoảng Min-Max ➔ đánh dấu `isOOS = true`, tự động kích hoạt `OOSInvestigation`.
- [x] **4.3.2**: UI hiển thị cảnh báo đỏ OOS nổi bật và bắt buộc Analyst/Supervisor nhập thông tin điều tra sơ bộ tại phòng lab (Phase 1 OOS Lab Investigation).

### Step 4.4: Toàn vẹn dữ liệu: Lịch sử sửa kết quả (`TestResultRevision`)
- [x] **4.4.1**: Cấm hoàn toàn lệnh UPDATE trực tiếp đè giá trị `Result.value`.
- [x] **4.4.2**: Mọi thay đổi bắt buộc tạo 1 bản ghi `TestResultRevision` chứa `oldValue`, `newValue`, `revisedBy`, `revisedAt` và `reason` (Lý do sửa bắt buộc ALCOA+).
- [x] **Checkpoint Nghiệm thu Phase 4**: Analyst nhập kết quả 72.5% (dưới ngưỡng 80%) ➔ Hệ thống kích hoạt cờ OOS và sinh `OosInvestigation`; khi sửa lại thành 88.5% ➔ Bắt buộc nhập lý do giải trình và lưu lịch sử `TestResultRevision` đầy đủ.

---

## 🔹 PHASE 5: Phê duyệt nhiều cấp, Chữ ký điện tử 21 CFR Part 11 & Báo cáo COA PDF

### Mục tiêu Phase 5
Thiết lập quy trình ký duyệt có giá trị pháp lý theo tiêu chuẩn FDA 21 CFR Part 11 (yêu cầu mật khẩu khi ký, ý nghĩa chữ ký rõ ràng) và kết xuất Phiếu kiểm nghiệm (COA) dạng PDF chính thức.

---

### Step 5.1: Generic Approval Workflow Engine (`approval`)
- [x] **5.1.1**: Xây dựng `ApprovalStepService` hỗ trợ quy trình 2 hoặc 3 cấp:
  - Cấp 1: Analyst hoàn thành kết quả (Author).
  - Cấp 2: Kiểm soát viên / Trưởng nhóm soát xét (Reviewer).
  - Cấp 3: Trưởng phòng QA / Giám đốc chất lượng phê duyệt (Approver).
- [x] **5.1.2**: Quản lý trạng thái: `PENDING` ➔ `APPROVED` hoặc `REJECTED` (yêu cầu ghi rõ nguyên nhân trả về).

### Step 5.2: Chữ ký điện tử tuân thủ 21 CFR Part 11 (Electronic Signature)
- [x] **5.2.1**: Backend Middleware/Service xác thực chữ ký:
  - Bắt buộc kiểm tra lại username & raw password tại thời điểm ký.
  - Lưu trữ: `signerId`, `signedAt`, `meaning` (*Authored by*, *Reviewed by*, *Approved by*), `signatureHash` (SHA-256 mã hóa token + dữ liệu bản ghi).
- [x] **5.2.2**: Frontend Modal ký điện tử chuyên dụng (E-Signature Dialog):
  - Hiển thị tóm tắt dữ liệu sẽ ký, trường nhập mật khẩu, dropdown chọn mục đích ký, cam kết pháp lý điện tử.

### Step 5.3: Kết xuất Báo cáo Phiếu phân tích (Certificate of Analysis - COA)
- [x] **5.3.1**: Tích hợp thư viện sinh PDF (iText / OpenPDF / JasperReports) trên Backend:
  - Mẫu COA tiêu chuẩn: Logo, Tiêu đề phiếu, Thông tin mẫu, Bảng kết quả đối chiếu tiêu chuẩn, Thông tin chữ ký điện tử số hóa.
- [x] **5.3.2**: Khóa bất biến (Lock/Immutable) toàn bộ kết quả của mẫu sau khi có chữ ký `APPROVED`.
- [x] **5.3.3**: Frontend tích hợp trình xem PDF trực tuyến và nút in/tải file báo cáo đã ký.
- [x] **Checkpoint Nghiệm thu Phase 5**: Thực hiện quy trình ký duyệt 2 cấp thành công ➔ Mẫu chuyển trạng thái `APPROVED` ➔ Xuất ra file PDF COA sắc nét có bảng chữ ký điện tử.

---

## 🔹 PHASE 6: Kho dữ liệu khoa học (SDMS) & Trình xem đồ thị InSpector

### Mục tiêu Phase 6
Mô phỏng thành phần lõi NuGenesis SDMS: Thu thập file dữ liệu từ thiết bị sắc ký (HPLC/GC), trích xuất metadata, lập chỉ mục và hiển thị sắc ký đồ (Chromatogram) trực tiếp trên Web.

---

### Step 6.1: Service Tiếp nhận Dữ liệu Khoa học (SDMS Ingestion)
- [x] **6.1.1**: Entity & DTO cho `DataFile`, `DataFileMetadata`, `ChromatogramPeak`.
- [x] **6.1.2**: API upload dữ liệu khoa học kèm xác thực checksum SHA-256 (chống sửa đổi file gốc 21 CFR Part 11).
- [x] **6.1.3**: Tự động bóc tách metadata: Tên máy, người chạy, ngày tạo, loại thiết bị.

### Step 6.2: Universal Data Viewer (Mô phỏng InSpector Viewer)
- [x] **6.2.1**: Backend parser: Đọc file dữ liệu số tọa độ điểm `(time, intensity)` sinh ra từ máy sắc ký/quang phổ (`ChromatogramParserService`).
- [x] **6.2.2**: Frontend: Xây dựng Component vẽ sắc ký đồ bằng **Apache ECharts** (`InspectorViewerComponent`):
  - Hỗ trợ hiển thị đỉnh sắc ký (peaks), thời gian lưu (retention time), phóng to/thu nhỏ vùng pic phân tích, bảng tích phân đỉnh peak integration table.
- [x] **6.2.3**: Traceability Link: Nút đính kèm trực tiếp file SDMS vào bài `Test` ở Phase 4 (`TestExecutionComponent`) để truy vết nguồn gốc.
- [x] **Checkpoint Nghiệm thu Phase 6**: Upload file dữ liệu sắc ký ➔ Giao diện web hiển thị biểu đồ sắc ký đồ tương tác mượt mà và link được tới phiếu kiểm nghiệm.

---

## 🔹 PHASE 7: Thử nghiệm độ ổn định (Stability) & Quản lý Kho vật tư/pha chế (Inventory)

### Mục tiêu Phase 7
Xây dựng giải pháp NuGenesis Stability (thử nghiệm lão hóa thuốc theo ICH) và kiểm soát hao hụt kho hóa chất/dung môi/cột sắc ký.

---

### Step 7.1: Quản lý Thử nghiệm độ ổn định thuốc (Stability Management)
- [x] **7.1.1**: Quản lý đề cương thử nghiệm (`StabilityStudy`): Sản phẩm, số lô, điều kiện bảo quản (25°C/60%RH, 40°C/75%RH), các mốc thời gian rút mẫu (0, 3, 6, 12, 24 tháng).
- [x] **7.1.2**: Quản lý tủ vi khí hậu (`StabilityStorageCondition`), vị trí lưu mẫu và ma trận rút mẫu (`StabilityPullEvent`).
- [x] **7.1.3**: Spring Scheduler quét lịch rút mẫu (**Sample Pull Schedule**) ➔ Tự động tạo `TestRequest` và `Sample` khi đến hạn.
- [x] **7.1.4**: Frontend: Calendar hiển thị lịch rút mẫu, dialog thực hiện rút mẫu và biểu đồ ECharts theo dõi suy giảm hàm lượng theo thời gian (Trend Line $y = ax + b, R^2$) & dự đoán tuổi thọ (Shelf-life prediction).

### Step 7.2: Quản lý Kho hóa chất, Chuẩn & Pha chế dung dịch (`inventory`)
- [x] **7.2.1**: Danh mục hóa chất, dung môi (`InventoryItem`), lô nhập kho (`InventoryLot`) kèm cảnh báo hạn sử dụng (<30 ngày, Expired).
- [x] **7.2.2**: Nhật ký pha chế dung dịch chuẩn/thuốc thử/pha động HPLC (`PreparationRecord`, `PreparationInput`) từ các lô nguyên liệu gốc.
- [x] **7.2.3**: Trừ kho tự động khi kiểm nghiệm viên sử dụng hóa chất hoặc tạo hồ sơ pha chế với ALCOA+ audit trail.
- [x] **Checkpoint Nghiệm thu Phase 7**: Tạo 1 nghiên cứu độ ổn định 24 tháng ➔ Hệ thống lên lịch pull ma trận; phân tích hồi quy suy thoái hoạt chất; quản lý danh mục hóa chất và tạo hồ sơ pha chế dung dịch trừ tồn kho tự động.

---

## 🔹 PHASE 8: Báo cáo vận hành (Dashboards), Connectors & Data Retention

### Mục tiêu Phase 8
Hoàn thiện dashboard điều hành phòng lab, cổng API kết nối ngoài (ERP/LIMS khác) và chính sách lưu trữ/bảo vệ dữ liệu pháp lý (Legal Hold).

---

### Step 8.1: Executive Lab Dashboard
- [x] **8.1.1**: Dashboard tuân thủ (Compliance): Tỷ lệ OOS, thời gian chu chuyển mẫu (TAT: 2.4 ngày), First Time Right (FTR 66.7%), Lệnh pháp lý (Legal Hold) đang hoạt động.
- [x] **8.1.2**: Dashboard vận hành: Tần suất sử dụng máy sắc ký & khả dụng thiết bị (Utilization Rate 87.5%), danh sách hóa chất sắp hết hạn (<30 ngày), biểu đồ thông lượng hàng tháng (ECharts: Mẫu, Phép thử, COA), phân tích căn nguyên gốc OOS (RCA Donut).
- [x] **8.1.3**: Xuất báo cáo thống kê định kỳ sang bản in chuẩn điều hành (Print / Summary Export).

### Step 8.2: Connectors (Tích hợp hệ thống ngoài) & Data Retention
- [x] **8.2.1**: Webhook/REST Inbound & Outbound: Nhận lệnh kiểm nghiệm từ ERP/SAP (`InboundOrder`) và tự động chuyển đổi thành LIMS `Sample` + `TestRequest`, ghi log phát sự kiện outbound (`WebhookOutbound`) về ERP (COA_SIGNED, OOS_ALERT).
- [x] **8.2.2**: Chính sách lưu trữ dữ liệu (`Data Retention Policy` - 21 CFR Part 11): Thiết lập số năm lưu trữ dữ liệu điện tử (Audit Trail: 99 năm vĩnh viễn, SDMS Raw Data: 10 năm, Testing Results: 7 năm, Stability: 15 năm, COA: 10 năm), cấu hình Auto-Archive.
- [x] **8.2.3**: Cơ chế **Legal Hold (FDA Inspection Lock)**: Khóa phong tỏa dữ liệu không cho sửa đổi, hủy bỏ hoặc xóa dữ liệu đối với các hồ sơ trong diện thanh tra FDA 483 hoặc kiểm toán pháp lý, hỗ trợ gỡ phong tỏa có biên bản giải trình và kiểm soát ALCOA+.
- [x] **Checkpoint Nghiệm thu Toàn diện (Final DoD)**: Toàn bộ vòng đời từ Khách hàng nộp mẫu ➔ Phân tích ELN ➔ Duyệt ký COA 21 CFR Part 11 ➔ Lưu trữ SDMS ➔ Thử nghiệm Độ ổn định ICH ➔ Quản lý Kho hóa chất/Pha chế ➔ Báo cáo Executive Dashboard & Connectors ERP/Legal Hold vận hành mượt mà, đồng bộ.

---

## 🚦 Quy trình bắt đầu thực hiện

Để đảm bảo tính chuẩn mực và kiểm soát chất lượng chặt chẽ:
1. **Chúng ta sẽ triển khai tuần tự từ Phase 1 (Master Data)**.
2. Trong Phase 1, bước đầu tiên là **Step 1.1: Module Thiết bị phòng Lab (`equipment`) - Backend**.
3. Sau khi hoàn thành Step 1.1, AI sẽ kiểm tra biên dịch, chạy test và xin xác nhận của bạn để chuyển sang **Step 1.2 (Frontend)**.
