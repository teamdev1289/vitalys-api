# Phân tích nghiệp vụ & Yêu cầu phát triển — Clone "NuGenesis Lab Management Software" (Waters)

> Nguồn tham khảo: trang sản phẩm chính thức của Waters (NuGenesis Software, NuGenesis LMS, NuGenesis SDMS, NuGenesis Sample Management). Đây là phần mềm quản lý phòng thí nghiệm (LIMS/ELN/SDMS) cho ngành dược, hóa chất, thực phẩm, môi trường.
> Tài liệu này tổng hợp lại toàn bộ **nghiệp vụ (business logic)** và diễn giải thành **yêu cầu phần mềm (requirements)** để bạn dùng làm baseline thiết kế hệ thống clone bằng **Angular (frontend)** + **Java Spring Boot (backend)**.

---

## 1. Tổng quan hệ thống gốc

NuGenesis Software là một nền tảng gồm 3 khối lớn hợp nhất:

| Khối | Vai trò |
|---|---|
| **SDMS** (Scientific Data Management System) | Kho lưu trữ dữ liệu khoa học tập trung, tự động thu thập dữ liệu từ thiết bị/phần mềm khác, đánh chỉ mục (index), tìm kiếm, lưu trữ dài hạn, tuân thủ quy định (compliance-ready repository). |
| **LMS/ELN** (Lab Management System / Electronic Lab Notebook) | Sổ tay thí nghiệm điện tử linh hoạt, quản lý workflow phòng lab (R&D và QC), quản lý thiết bị/hóa chất/cột sắc ký, biểu mẫu thí nghiệm. |
| **Sample Management** | Quản lý toàn bộ vòng đời mẫu: nộp mẫu, giao việc, phân tích, kết quả, OOS, báo cáo, kết nối với hệ thống ngoài (SampleShare, Connectors). |

Module phụ trợ: **NuGenesis Stability** (quản lý thử nghiệm độ ổn định thuốc), **NuGenesis Connectors** (tích hợp ERP/SAP/LIMS), **NuGenesis Dashboards** (báo cáo vận hành), **InSpector Viewer** (xem dữ liệu phân tích đa định dạng không cần phần mềm gốc).

**Đối tượng dùng**: phòng thí nghiệm dược phẩm, hóa chất, thực phẩm & đồ uống, môi trường — từ R&D, QC đến sản xuất (manufacturing). Phải tuân thủ **21 CFR Part 11, cGxP, GLP, GMP, EU GMP, Sarbanes-Oxley, CROMERR**.

---

## 2. Danh sách module nghiệp vụ (Epics)

1. Quản lý người dùng, vai trò, chữ ký điện tử, audit trail (Compliance/Security)
2. SDMS – Kho dữ liệu khoa học tập trung
3. LMS/ELN – Sổ tay thí nghiệm điện tử & workflow
4. Quản lý mẫu (Sample Management)
5. Quản lý kết quả & OOS (Out-of-Specification)
6. Stability Management (quản lý thử nghiệm độ ổn định)
7. Quản lý thiết bị (Instrument Management)
8. Quản lý hóa chất/dung môi (Reagent Management)
9. Quản lý cột sắc ký (Column Management)
10. Connectors – tích hợp hệ thống ngoài (LIMS/ERP/SAP)
11. Dashboards & Reporting
12. Tìm kiếm khoa học (Scientific Search) & InSpector Viewer
13. Data Retention & Legal Hold

Bên dưới là chi tiết nghiệp vụ + yêu cầu chức năng cho từng module.

---

## 3. Module 1 — Quản lý người dùng, vai trò & Compliance (nền tảng bắt buộc)

### Nghiệp vụ
- Hệ thống phải "compliance-ready" theo **21 CFR Part 11** (FDA Electronic Records/Electronic Signature Rule): mọi bản ghi điện tử và chữ ký điện tử phải có giá trị pháp lý tương đương chữ ký tay.
- Time-stamping cho mọi hành động thay đổi dữ liệu.
- Chữ ký điện tử có thể cấu hình linh hoạt theo workflow (configurable e-signature workflow) — ví dụ: 1 cấp ký (Analyst), 2 cấp ký (Analyst → Reviewer → Approver).
- Version control đầy đủ, audit trail toàn bộ (ai, làm gì, khi nào, giá trị cũ/mới).
- Phân quyền chi tiết theo vai trò, phòng ban, dự án.

### Yêu cầu chức năng
- **FR-1.1**: Đăng nhập, SSO (tùy chọn LDAP/AD sau này), đổi mật khẩu định kỳ, khóa tài khoản sau N lần sai.
- **FR-1.2**: Role-Based Access Control (RBAC): Admin, Lab Manager, Analyst, Reviewer/QA Approver, Auditor (read-only), External User (SampleShare).
- **FR-1.3**: Electronic Signature: mỗi bản ghi quan trọng (kết quả, báo cáo, method, SOP) phải hỗ trợ ký điện tử với meaning (Reviewed by/Approved by/Authored by), yêu cầu nhập lại mật khẩu khi ký.
- **FR-1.4**: Audit Trail: log bất biến (append-only) cho mọi CRUD trên entity quan trọng — lưu user, timestamp, action, old value, new value, reason for change (bắt buộc nhập lý do khi sửa dữ liệu đã duyệt).
- **FR-1.5**: Version Control: mọi bản ghi (mẫu, kết quả, method, document) có lịch sử phiên bản, có thể xem lại phiên bản cũ, không cho sửa/xóa cứng (soft-delete only).
- **FR-1.6**: Session timeout tự động, cảnh báo trước khi hết phiên.

### Gợi ý entity (Spring Boot)
```
User, Role, Permission, UserRole, Department, Project,
ESignature(entityType, entityId, signerId, meaning, signedAt, passwordConfirmedHash),
AuditLog(entityType, entityId, action, oldValue(JSON), newValue(JSON), userId, timestamp, reason)
```

---

## 4. Module 2 — SDMS (Scientific Data Management System)

### Nghiệp vụ
- Tự động "capture" dữ liệu sinh ra từ thiết bị phân tích (HPLC, GC, MS, NMR...), từ scientist, và từ nguồn ngoài, đưa vào kho lưu trữ tập trung ngay sau khi dữ liệu được tạo/thay đổi.
- Quản lý nhiều loại dữ liệu: print data (nội dung report từ thiết bị), raw instrument data (spectra, chromatogram), document/report từ ứng dụng khoa học & business, dữ liệu từ Windows/UNIX, dữ liệu chuẩn hóa (vd JCAMP-DX).
- Tự động quét (scan) server/project/data type để tìm nội dung mới → tự trích xuất metadata → xây dựng catalog/database index để tìm kiếm nhanh.
- Tìm kiếm full-text trong report, đồ thị, bảng biểu; xem nhiều report từ nhiều nguồn cùng lúc; xem dữ liệu mà không cần phần mềm gốc (vendor-neutral viewer).
- Liên kết linh động (dynamic link/hyperlink) giữa report – instrument data – interpreted data để truy vết nguồn gốc (traceability).
- Data retention, retirement, và legal hold (giữ dữ liệu không cho xóa/sửa khi có yêu cầu pháp lý/audit).
- Gửi dữ liệu đã chọn (text/table/graphic) sang LMS hoặc phần mềm khác (Office, LIMS, ERP).

### Yêu cầu chức năng
- **FR-2.1**: Data Ingestion Service — import file (PDF, raw binary, text report, image, JCAMP-DX...) qua watch-folder / API upload / kết nối trực tiếp thiết bị (giả lập bằng file drop trong bản clone).
- **FR-2.2**: Metadata Extraction — tự động parse metadata (loại thiết bị, ngày giờ, project, method, operator) khi ingest; cho phép cấu hình rule trích xuất theo loại file.
- **FR-2.3**: Catalog/Indexing — lưu index có thể search full-text (dùng Elasticsearch/PostgreSQL full-text search).
- **FR-2.4**: Advanced Search — filter theo project, data type, khoảng thời gian, instrument, từ khóa nội dung.
- **FR-2.5**: Universal Viewer (giống InSpector) — hiển thị nhiều loại dữ liệu phân tích (chromatogram, spectra) qua web mà không cần app gốc; hỗ trợ tối thiểu: PDF, hình ảnh, dữ liệu dạng bảng, chart XY (mô phỏng chromatogram từ dữ liệu số).
- **FR-2.6**: Hyperlink liên kết record — mỗi record có thể tham chiếu (link) đến record khác (nguồn gốc dữ liệu), hiển thị dạng "provenance chain".
- **FR-2.7**: Data Retention Policy — cấu hình thời gian lưu trữ theo loại dữ liệu/dự án; tự động cảnh báo khi đến hạn hủy; cờ **Legal Hold** để khóa không cho xóa dù đã hết hạn.
- **FR-2.8**: Export/Send-to — gửi dữ liệu đã chọn sang module LMS (tạo experiment mới liên kết dữ liệu), hoặc export CSV/PDF.
- **FR-2.9**: Tích hợp nguồn ngoài qua Connector (xem Module 10).

### Gợi ý entity
```
DataFile(id, sourceType, instrumentId, projectId, capturedAt, storagePath,
         fileFormat, checksum, retentionUntil, legalHold:boolean, status)
DataFileMetadata(dataFileId, key, value)     // metadata linh động dạng EAV
DataFileLink(sourceFileId, targetFileId, linkType)  // traceability
SearchIndexEntry (đồng bộ vào Elasticsearch)
```

---

## 5. Module 3 — LMS / ELN (Electronic Lab Notebook & Workflow)

### Nghiệp vụ
- ELN linh hoạt hỗ trợ cả môi trường "free-flow" (R&D) lẫn "process-driven" (QC theo quy trình cố định).
- Ghi nhận (document) quan sát, kết quả, thủ tục kiểm soát (control procedures).
- Template thí nghiệm generic: Procedure Purpose → Method → Observations → Findings/Conclusion.
- Execution Method: nhận mẫu qua barcode/mã vạch, thực thi theo quy trình đã định nghĩa sẵn (predefined method), ghi log từng bước.
- Liên kết dữ liệu SDMS vào trong notebook để review.
- Dashboard thống kê document/template usage.

### Yêu cầu chức năng
- **FR-3.1**: Notebook/Experiment CRUD — tạo experiment từ template hoặc từ đầu; mỗi experiment có Purpose, Method, Materials, Observations, Attachments, Conclusion.
- **FR-3.2**: Template Builder — cho phép Admin tạo template có các section tái sử dụng (reusable section: Reagent, Instrument, Sample, Generic Analysis...).
- **FR-3.3**: Execution Method (structured workflow) — quy trình gồm nhiều bước (step), mỗi bước có input bắt buộc, có thể gán barcode để quét mẫu vào bước; hệ thống enforce đúng thứ tự bước, không cho bỏ qua bước bắt buộc.
- **FR-3.4**: Trạng thái experiment: Draft → In Progress → Submitted for Review → Reviewed → Approved/Rejected → Locked (immutable sau khi approve, chỉ sửa qua "change with reason").
- **FR-3.5**: Đính kèm dữ liệu từ SDMS vào experiment (liên kết record, không copy file).
- **FR-3.6**: Ghi nhận & xử lý sự kiện bất thường (Adverse Event) — cho phép gắn cờ, mô tả, workflow xử lý CAPA cơ bản (root cause, corrective action).
- **FR-3.7**: Dashboard "Document Statistics" — số lượng experiment theo trạng thái, template được dùng nhiều nhất, thời gian trung bình hoàn thành.

### Gợi ý entity
```
ExperimentTemplate(id, name, sections:[SectionDefinition])
Experiment(id, templateId, code, status, ownerId, projectId, createdAt, approvedAt)
ExperimentSection(experimentId, sectionType, dataJson)  // linh động theo template
ExecutionMethod(id, name, steps:[MethodStep])
MethodStep(id, methodId, order, requiredInput, barcodeRequired:boolean)
ExperimentStepLog(experimentId, stepId, performedBy, performedAt, sampleBarcode, result)
AdverseEvent(experimentId, description, severity, rootCause, correctiveAction, status)
```

---

## 6. Module 4 — Sample Management

### Nghiệp vụ
- Nộp mẫu (sample submission) từ bất kỳ đâu có internet.
- Ghi nhận đặc tả mẫu (sample spec) và đặc tả sản phẩm (product spec) — hỗ trợ **tiêu chí chấp nhận theo từng quốc gia** (country-specific acceptance criteria).
- Giao việc (assign) mẫu cho cá nhân/nhóm.
- Xác định phép thử (test) cần thực hiện khi "log in" mẫu.
- Quản lý toàn bộ vòng đời: nhận mẫu → phân tích → kết quả → báo cáo → lưu trữ/hủy.
- Thiết lập **chuỗi giám sát (chain of custody)** để đáp ứng yêu cầu quy định.
- Trao đổi dữ liệu mẫu hai chiều với đơn vị ngoài (lab thuê ngoài, khách hàng, đối tác) — kể cả khi họ không dùng hệ thống (qua "SampleShare" web portal).
- Quản lý cả từ góc độ lab dịch vụ phân tích doanh nghiệp / lab thử nghiệm hợp đồng (contract testing lab).

### Yêu cầu chức năng
- **FR-4.1**: Sample Submission Form — nộp mẫu online, sinh mã mẫu tự động (barcode/QR), chọn loại mẫu, số lượng, ngày lấy mẫu, người gửi, dự án.
- **FR-4.2**: Sample Login/Accessioning — khi mẫu về lab, xác định các phép thử áp dụng (test panel) dựa trên loại mẫu/spec sản phẩm.
- **FR-4.3**: Product/Sample Specification Management — CRUD spec, mỗi spec có acceptance criteria khác nhau theo **quốc gia/thị trường** (vd: giới hạn tạp chất khác nhau giữa US/EU/VN).
- **FR-4.4**: Assignment — giao mẫu/test cho analyst hoặc nhóm; hàng đợi công việc (workload queue) hiển thị theo người/nhóm/phòng.
- **FR-4.5**: Chain of Custody — log đầy đủ: ai giữ mẫu, thời điểm chuyển giao, vị trí lưu trữ (kho/tủ lạnh), tình trạng mẫu.
- **FR-4.6**: Result Entry — nhập kết quả phép thử, so sánh tự động với acceptance criteria.
- **FR-4.7**: OOS Detection (Out-of-Specification) — tự động gắn cờ kết quả vượt giới hạn spec, kích hoạt workflow điều tra OOS (ghi nhận nguyên nhân, retest, kết luận).
- **FR-4.8**: Report Generation — sinh **intermediate report** (để review nội bộ) và **final PDF report** (có chữ ký điện tử, chỉ tạo khi đã Approved).
- **FR-4.9**: SampleShare Portal (external) — cổng web riêng cho đối tác ngoài hệ thống: nộp mẫu, nhận PDF report, không cần cài đặt phần mềm nội bộ; phân quyền giới hạn chỉ thấy mẫu/report của chính họ.
- **FR-4.10**: Sample Data Transfer — export/import dữ liệu mẫu theo định dạng chuẩn (CSV/XML/JSON) để trao đổi với lab ngoài không dùng hệ thống.

### Gợi ý entity
```
Sample(id, code, barcodeValue, status, submittedBy, submittedAt, sampleType, projectId, productSpecId)
ProductSpecification(id, productCode, countryCode, effectiveDate)
AcceptanceCriteria(productSpecId, testParameter, minValue, maxValue, unit)
TestAssignment(sampleId, testDefinitionId, assignedToUserId/GroupId, status, dueDate)
TestResult(id, testAssignmentId, parameter, value, unit, isOOS:boolean, enteredBy, enteredAt)
OOSInvestigation(testResultId, reportedAt, rootCause, retestSampleId, conclusion, status)
ChainOfCustodyLog(sampleId, fromUser, toUser, location, timestamp, note)
Report(sampleId, type[INTERMEDIATE|FINAL], fileUrl, generatedAt, signedBy)
ExternalPartner(id, name, accessScope)  // cho SampleShare
```

---

## 7. Module 5 — Stability Management (NuGenesis Stability)

### Nghiệp vụ
- Giải pháp quản lý **thử nghiệm độ ổn định (stability testing)** cho sản phẩm dược — tuân theo GAMP & ICH guidelines.
- Định nghĩa **study matrix**: time points (mốc thời gian lấy mẫu, vd tháng 0/3/6/12/24), storage conditions (điều kiện bảo quản: nhiệt độ/độ ẩm), test regimes (bộ phép thử áp dụng theo mốc).
- Quản lý & kiểm soát **kho lưu trữ mẫu ổn định** (stability inventory) — mẫu được cất ở buồng ổn định (stability chamber) theo điều kiện quy định.
- **Sample Pull Overview** — xem tổng quan lịch "rút mẫu" (pull) sắp tới trên tất cả các study đang chạy, để lab chuẩn bị trước.
- Review theo từng time point, tạo báo cáo & phân tích thống kê (trend/statistical analysis qua các mốc thời gian).
- Mục tiêu: chuẩn hóa quy trình, giảm rủi ro sai sót con người — **không cần có LIMS đầy đủ** vẫn triển khai được (đứng độc lập trong LMS).

### Yêu cầu chức năng
- **FR-5.1**: Stability Study Definition — tạo study gồm: sản phẩm, số lô, điều kiện bảo quản (nhiều điều kiện song song, vd 25°C/60%RH và 40°C/75%RH), danh sách time point, test regime cho từng time point.
- **FR-5.2**: Stability Inventory — mỗi study sinh ra danh sách "pull events" (mẫu cần rút tại mỗi time point); quản lý vị trí lưu trữ mẫu trong buồng ổn định (chamber/shelf/position).
- **FR-5.3**: Pull Schedule/Overview — dashboard lịch rút mẫu toàn bộ study đang active, cảnh báo pull sắp đến hạn/quá hạn.
- **FR-5.4**: Time Point Review — khi đến hạn, tạo task test cho các phép thử của time point đó, liên kết với Sample Management để nhập kết quả.
- **FR-5.5**: Statistical/Trend Report — biểu đồ kết quả theo thời gian (time point) cho từng thông số, so sánh với spec, hiển thị xu hướng (trend line, ví dụ dùng hồi quy tuyến tính để dự đoán hạn dùng — shelf-life estimation, có thể để giai đoạn 2).
- **FR-5.6**: Bộ báo cáo chuẩn (suite of stability reports): Summary Report theo study, theo sản phẩm, theo điều kiện bảo quản.

### Gợi ý entity
```
StabilityStudy(id, productId, batchNumber, startDate, status)
StorageCondition(studyId, temperature, humidity, chamberId)
TimePoint(studyId, label, dueDate, testRegimeId)
TestRegime(id, name, testParameters:[...])
StabilityPullEvent(studyId, timePointId, storageConditionId, scheduledDate, status[PENDING|PULLED|TESTED])
StabilityInventoryPosition(sampleId, chamberId, shelf, position)
StabilityResult(pullEventId, parameter, value, unit, isOOS)
```

---

## 8. Module 6, 7, 8 — Quản lý Thiết bị / Hóa chất / Cột sắc ký (LMS Solution Components)

Đây là 3 "out-of-the-box template" trong bản gốc, dùng chung một mô hình vòng đời (lifecycle):

### Nghiệp vụ chung
- **Instrument Management**: đăng ký (register), bảo trì, hiệu chuẩn (calibrate), xác minh (verify), ngừng sử dụng (decommission) thiết bị; ghi nhận tình trạng sử dụng & trạng thái dịch vụ (service status).
- **Reagent Management**: đăng ký và loại bỏ (dispose) hóa chất/dung môi; ghi nhận việc sử dụng qua "reusable section template" (mẫu ghi chép dùng lại được trong ELN).
- **Column Management**: tương tự Reagent nhưng cho vòng đời cột sắc ký (số lần bơm mẫu, áp suất tối đa, ngày hết hạn sử dụng).
- Dashboard **System Usage**: theo dõi lượng hóa chất/dung môi còn lại (consumable remaining) + thông tin hạn sử dụng (expiry).

### Yêu cầu chức năng
- **FR-6.1**: CRUD Instrument — mã thiết bị, loại, nhà sản xuất, ngày mua, trạng thái (In Service/Maintenance/Decommissioned).
- **FR-6.2**: Lịch Calibration/Verification — đặt lịch định kỳ, ghi log mỗi lần hiệu chuẩn (kết quả pass/fail, người thực hiện, chứng chỉ đính kèm), cảnh báo khi sắp đến hạn.
- **FR-6.3**: Instrument Usage Log — liên kết với Experiment/Sample test để biết thiết bị nào dùng cho thí nghiệm nào (truy vết).
- **FR-6.4**: CRUD Reagent/Solvent — mã lô (lot number), nhà cung cấp, ngày nhận, hạn sử dụng, số lượng tồn.
- **FR-6.5**: Reagent Usage Tracking — mỗi lần dùng trong experiment trừ vào tồn kho, cảnh báo sắp hết/sắp hết hạn.
- **FR-6.6**: CRUD Column — serial number, loại cột, số lần inject tích lũy, áp suất tối đa từng ghi nhận, ngày đưa vào dùng.
- **FR-6.7**: Dashboard "System Usage" — bảng tổng hợp tồn kho reagent/dung môi + cảnh báo hết hạn, theo phòng/lab.

### Gợi ý entity
```
Instrument(id, code, type, manufacturer, purchaseDate, status)
CalibrationRecord(instrumentId, performedAt, performedBy, result, certificateUrl, nextDueDate)
Reagent(id, name, lotNumber, supplier, receivedDate, expiryDate, quantityOnHand, unit)
ReagentUsageLog(reagentId, experimentId, usedBy, usedAt, quantityUsed)
Column(id, serialNumber, type, installDate, totalInjections, status)
```

---

## 9. Module 9 — Connectors (tích hợp ERP/SAP/LIMS)

### Nghiệp vụ
- Kết nối 2 chiều (bidirectional) giữa thiết bị lab / hệ thống lab và hệ thống business (SAP, ERP, LIMS khác).
- Trao đổi: test requests, SOP, product specifications, test results.
- Mục tiêu: giảm giấy tờ, giảm nhập liệu tay, tăng khả năng truy vết, tăng tốc release sản phẩm đạt chuẩn (release in-spec products nhanh hơn).

### Yêu cầu chức năng
- **FR-7.1**: Connector Configuration — cấu hình endpoint (REST/SOAP/File-based), mapping field giữa 2 hệ thống, không cần code (config-driven, có thể để version đầu là JSON mapping file).
- **FR-7.2**: Outbound Sync — khi Test Result được Approve, tự động đẩy sang hệ thống ERP/SAP (giả lập bằng webhook/REST call).
- **FR-7.3**: Inbound Sync — nhận Test Request/SOP/Spec từ hệ thống ngoài, tạo Sample/Spec tương ứng.
- **FR-7.4**: Log & Retry — mọi lần đồng bộ đều log lại; nếu lỗi, cho retry thủ công hoặc tự động theo lịch.

### Gợi ý entity
```
ConnectorConfig(id, name, direction[INBOUND|OUTBOUND], endpointUrl, fieldMapping(JSON), active)
SyncLog(connectorConfigId, payload, status[SUCCESS|FAILED], errorMessage, triggeredAt)
```

---

## 10. Module 10 — Dashboards & Reporting

### Nghiệp vụ (từ bản gốc, feature "NuGenesis Dashboards" từ v9.3 trở lên)
- **System Compliance**: quản lý nguồn dữ liệu linh hoạt (SDMS) + khả năng truy vết toàn hệ thống (LMS).
- **Process Statistics**: hiệu suất hệ thống — số lượng file di chuyển (import/archive) theo khoảng thời gian.
- **System Usage**: tồn kho consumable + hạn sử dụng.
- **Document Statistics**: thống kê sử dụng document & template.

### Yêu cầu chức năng
- **FR-8.1**: Dashboard tổng quan (read-only, non-editable như bản gốc mô tả) cho 4 loại trên, hiển thị bằng chart (line/bar/pie).
- **FR-8.2**: Bộ lọc theo khoảng thời gian, phòng ban, dự án.
- **FR-8.3**: Export báo cáo dashboard ra PDF/Excel.

---

## 11. Module 11 — Tìm kiếm khoa học & Universal Viewer

### Nghiệp vụ
- Tìm kiếm xuyên suốt toàn bộ dữ liệu (SDMS + LMS + Sample) theo từ khóa, loại dữ liệu, metadata.
- Xem dữ liệu phân tích nhiều định dạng (chromatogram, spectra, cấu trúc hóa học...) trực tiếp trên trình duyệt mà không cần phần mềm nguồn.

### Yêu cầu chức năng
- **FR-9.1**: Global Search bar — full-text search toàn hệ thống, kết quả phân nhóm theo loại entity (Sample, Experiment, DataFile, Report...).
- **FR-9.2**: Faceted filter — theo project, instrument, ngày, trạng thái, loại dữ liệu.
- **FR-9.3**: Web Viewer — hiển thị PDF, ảnh, dữ liệu dạng bảng; với dữ liệu dạng chuỗi số (x,y) mô phỏng chromatogram/spectra, vẽ bằng chart library trên Angular (vd ngx-charts / ECharts).

---

## 12. Module 12 — Data Retention & Legal Hold

### Nghiệp vụ
- Chính sách lưu trữ dài hạn, retirement (nghỉ hưu dữ liệu — chuyển sang lưu trữ lạnh), và Legal Hold (tạm khóa không cho xóa khi có tranh chấp/kiểm toán).

### Yêu cầu chức năng
- **FR-10.1**: Cấu hình Retention Policy theo loại dữ liệu/dự án (số năm lưu trữ).
- **FR-10.2**: Job định kỳ (Spring Scheduler) quét dữ liệu hết hạn retention và **không** có Legal Hold → đánh dấu "Eligible for Deletion", yêu cầu phê duyệt thủ công trước khi xóa thật (không tự động xóa).
- **FR-10.3**: Legal Hold flag có thể bật/tắt bởi Admin/QA, kèm lý do + audit log.

---

## 13. Yêu cầu phi chức năng (Non-Functional Requirements)

- **NFR-1 Compliance**: Audit trail bất biến, e-signature, version control là **bắt buộc xuyên suốt toàn hệ thống**, không phải tính năng phụ.
- **NFR-2 Bảo mật**: JWT/OAuth2 cho auth, mã hóa dữ liệu nhạy cảm, phân quyền chi tiết đến từng project/department.
- **NFR-3 Khả năng mở rộng**: kiến trúc phải scale từ 1 lab đến nhiều lab/nhiều site (multi-tenant hoặc multi-site trong 1 tenant).
- **NFR-4 Tích hợp**: API-first (REST, OpenAPI) để dễ viết Connector cho ERP/LIMS khác.
- **NFR-5 Hiệu năng tìm kiếm**: dùng Elasticsearch hoặc Postgres full-text cho SDMS search vì dữ liệu tăng theo cấp số nhân.
- **NFR-6 Khả năng theo dõi (traceability)**: mọi kết quả cuối cùng phải truy ngược được về dữ liệu thô gốc (raw data) và người/thiết bị tạo ra nó.
- **NFR-7 Đa ngôn ngữ/đa quốc gia**: hỗ trợ acceptance criteria khác nhau theo quốc gia (i18n cho spec, không nhất thiết cho UI ở bản đầu).

---

## 14. Đề xuất kiến trúc kỹ thuật (Angular + Spring Boot)

### Backend (Spring Boot)
- **Kiến trúc**: modular monolith theo package-by-feature (dễ tách microservice sau): `auth`, `sdms`, `eln`, `sample`, `stability`, `inventory` (instrument/reagent/column), `connector`, `dashboard`, `audit`.
- **Persistence**: PostgreSQL (transactional data) + Elasticsearch (search index cho SDMS/Global Search) + object storage (S3/MinIO) cho file thô.
- **Security**: Spring Security + JWT, method-level `@PreAuthorize` theo Role/Permission.
- **Audit**: dùng Hibernate Envers hoặc AOP `@Auditable` interceptor để tự log mọi thay đổi vào bảng `audit_log`.
- **E-signature**: middleware bắt buộc xác thực lại mật khẩu trước khi set `status = APPROVED`.
- **Workflow engine**: bắt đầu bằng state machine đơn giản (enum + Spring State Machine) cho Sample/Experiment/OOS; có thể nâng cấp Camunda/Flowable sau.
- **Scheduling**: Spring `@Scheduled` cho retention job, stability pull reminder, calibration due alert.
- **Async/Event**: Spring Events hoặc Kafka (giai đoạn sau) cho Connector sync & notification.

### Frontend (Angular)
- **Kiến trúc**: Angular standalone components + lazy-loaded feature modules tương ứng từng Epic (sample, eln, stability, inventory, sdms-search, dashboard, admin).
- **State management**: NgRx (Signal Store hoặc Store cổ điển) cho state phức tạp (workflow, wizard nộp mẫu nhiều bước).
- **UI Kit**: Angular Material hoặc PrimeNG cho bảng dữ liệu lớn (virtual scroll cho danh sách mẫu/kết quả).
- **Chart**: ngx-charts/ECharts cho dashboard & mô phỏng chromatogram/spectra.
- **Form Builder**: dynamic form renderer cho ELN Template (JSON schema → form), vì template là do người dùng tự định nghĩa (giống form builder).
- **Barcode**: tích hợp thư viện quét/sinh QR/barcode (ngx-barcode, zxing) cho Sample login & Execution Method.
- **File Viewer**: pdf.js cho xem PDF report trực tiếp trong trình duyệt.

---

## 15. Đề xuất lộ trình phát triển (Roadmap gợi ý)

| Giai đoạn | Nội dung |
|---|---|
| **MVP (Phase 1)** | Auth/RBAC + Audit trail cơ bản, Sample Management (submit → login → assign → result → report PDF), OOS detection cơ bản |
| **Phase 2** | ELN/LMS (template + experiment workflow), Instrument/Reagent/Column management |
| **Phase 3** | SDMS (ingest, metadata, search, viewer), Global Search |
| **Phase 4** | Stability Management, Dashboards |
| **Phase 5** | Connectors (ERP/SAP), SampleShare external portal, Data Retention & Legal Hold, e-signature nâng cao (2-step approval) |

---

## 16. Lưu ý quan trọng

- Đây là bản **phân tích lại từ trang marketing công khai** của Waters (không có tài liệu đặc tả kỹ thuật/mã nguồn nội bộ), nên các con số, tên trường, luồng chi tiết ở trên là **suy luận hợp lý theo nghiệp vụ ngành LIMS/ELN** để bạn dùng làm baseline thiết kế — không phải bản sao 1:1 mã nguồn hay UI của NuGenesis.
- "NuGenesis", "Waters", "Empower", "InSpector" là thương hiệu/sản phẩm của Waters Corporation — khi clone, nên đặt tên sản phẩm khác và không sao chép logo/UI/copy nguyên văn để tránh vi phạm thương hiệu/bản quyền.
- Khuyến nghị bắt đầu từ **Module 4 (Sample Management)** vì đây là lõi nghiệp vụ dễ demo giá trị nhất và có ROI nhanh nhất cho một LIMS thu nhỏ.
