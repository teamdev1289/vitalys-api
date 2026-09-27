# Thiết kế Module & Entity cho hệ thống LIMS (Spring Boot)

> Tài liệu này phân rã ERD hiện có (~45 bảng) thành các **module nghiệp vụ**, kèm entity, quan hệ chính và gợi ý package Java. Mục tiêu: mỗi module là một bounded-context độc lập, dễ tách thành package riêng (hoặc module Maven/Gradle riêng nếu sau này đi theo modular monolith / microservices).

---

## 0. Nguyên tắc phân chia

- Nhóm theo **vòng đời nghiệp vụ** (business lifecycle) chứ không nhóm theo việc bảng có FK trỏ tới nhau hay không — vì trong LIMS gần như bảng nào cũng liên kết chéo qua `SAMPLE`, `USER`, `METHOD`.
- Entity dùng chung nhiều module (`USER`, `DEPARTMENT`, `METHOD`, `SAMPLE`...) đặt ở module "chủ sở hữu nghiệp vụ" của nó, các module khác chỉ **tham chiếu bằng ID** (dùng `@ManyToOne` + FK, tránh phụ thuộc ngược package).
- Base package đề xuất: `com.company.lims`
- Mỗi module con nên có cấu trúc chuẩn:
  ```
  com.company.lims.<module>/
    ├── entity/
    ├── repository/
    ├── dto/
    ├── service/
    ├── controller/
    └── mapper/
  ```
- Ngoài ra cần 1 package **`common`** (hoặc `shared`) chứa: `BaseEntity` (id, created_at, updated_at, deleted_at), enum dùng chung (`Status`, `Priority`...), exception handler, security utils.

---

## Sơ đồ tổng quan module

| # | Module | Vai trò chính |
|---|--------|----------------|
| 1 | `auth` | Người dùng, vai trò, quyền, phòng ban, token |
| 2 | `audit` | Audit trail, notification |
| 3 | `product` | Sản phẩm, công thức (formulation), R&D, hồ sơ đăng ký |
| 4 | `partner` | Khách hàng, dự án |
| 5 | `sample` | Yêu cầu xét nghiệm, mẫu, trạng thái mẫu |
| 6 | `method` | Phương pháp thử, tiêu chuẩn (spec), form động |
| 7 | `testing` | Thực hiện test, kết quả, lịch sử sửa kết quả |
| 8 | `approval` | Phê duyệt, báo cáo (report) |
| 9 | `equipment` | Thiết bị, hiệu chuẩn, bảo trì |
| 10 | `inventory` | Kho hóa chất/vật tư, lô, pha chế |

---

## 1. Module `auth` — Người dùng & Phân quyền

**Package:** `com.company.lims.auth`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `Department` | id, parentId (self-FK, phòng ban cha), name, type, code, status | Cây phòng ban (tree) |
| `User` | id, departmentId, email, passwordHash, fullName, isActive, mustChangePassword, lastLoginAt, createdAt, updatedAt, deletedAt | Soft-delete (`deletedAt`) |
| `Role` | id, name, description, createdAt | |
| `Permission` | id, name, description | |
| `UserRole` | userId, roleId, assignedBy, assignedAt | Bảng nối N-N |
| `RolePermission` | roleId, permissionId | Bảng nối N-N |
| `RefreshToken` | id, userId, tokenHash, expiresAt, revokedAt | Dùng cho JWT refresh flow |

### Quan hệ
- `User (N) → Department (1)`
- `User (N) — (N) Role` qua `UserRole`
- `Role (N) — (N) Permission` qua `RolePermission`
- `RefreshToken (N) → User (1)`

### Gợi ý API
- `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`
- `CRUD /users`, `/roles`, `/permissions`, `/departments`
- Dùng Spring Security + JWT, `@PreAuthorize` theo permission name.

---

## 2. Module `audit` — Nhật ký & Thông báo

**Package:** `com.company.lims.audit`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `AuditTrail` | id, userId, entityType, entityId, action, actionTime | Ghi mọi thao tác CUD quan trọng (generic, polymorphic bằng entityType/entityId) |
| `Notification` | id, userId, type, title, message, isRead, createdAt | |

### Cách triển khai
- Nên implement bằng **Spring AOP / Event Listener** (`@EventListener` trên domain event) để không phải gọi `auditService.log()` rải rác khắp code.
- `AuditTrail.entityType` là string (`"SAMPLE"`, `"TEST_REQUEST"`...) — không FK cứng vì polymorphic.

---

## 3. Module `product` — Sản phẩm, Công thức, R&D

**Package:** `com.company.lims.product`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `Product` | id, registrationNumber, productName, dosageForm, packagingSpec, shelfLifeMonths, registrant, manufacturer, countryOfOrigin, qualityStandard, productCategory, classification, status | |
| `Ingredient` | id, name, innName, casNumber, type | Nguyên liệu/hoạt chất dùng chung |
| `ProductActiveIngredient` | id, productId, ingredientId, declaredStrength, unit | Hàm lượng hoạt chất công bố của sản phẩm |
| `RdProject` | id, departmentId, name, objective, startDate, status | Dự án R&D |
| `FormulationTrial` | id, rdProjectId, productId, trialCode, version, status | Thử nghiệm công thức trong quá trình R&D |
| `FormulationTrialComponent` | id, trialId, ingredientId, role, quantityPerUnit, unit | Thành phần của 1 trial |
| `Formulation` | id, productId, sourceTrialId, formulationCode, version, status | Công thức chính thức (sau khi trial được duyệt) |
| `FormulationComponent` | id, formulationId, ingredientId, role, quantityPerUnit, unit | Thành phần công thức chính thức |
| `RegistrationDossier` | id, productId, formulationTrialId, dossierType, submittedDate, status, authorityRefNumber, decisionDate | Hồ sơ đăng ký lưu hành |
| `Batch` | id, formulationId, specSetId, batchNumber, manufacturingDate, expiryDate, quantityProduced, status | Lô sản xuất, gắn với 1 formulation + 1 spec set |

### Quan hệ
- `Product (1) → (N) FormulationTrial → (N) Formulation`
- `Formulation (1) → (N) FormulationComponent → (1) Ingredient`
- `Product (1) → (N) RegistrationDossier`
- `Batch → Formulation`, `Batch → SpecificationSet` (thuộc module `method`)

> Gợi ý: nếu team lớn, có thể tách tiếp `product.rd` (RdProject, FormulationTrial*) khỏi `product.master` (Product, Ingredient, Formulation, Batch) — nhưng ở quy mô vừa, gộp chung 1 module `product` là hợp lý.

---

## 4. Module `partner` — Khách hàng & Dự án

**Package:** `com.company.lims.partner`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `Customer` | id, name, contactEmail, contactPhone | |
| `Project` | id, customerId, name, description | Dự án phân tích cho khách hàng (khác với `RdProject` nội bộ) |

### Quan hệ
- `Customer (1) → (N) Project`
- `Project` được `TestRequest` (module `sample`) tham chiếu tới.

---

## 5. Module `sample` — Yêu cầu xét nghiệm & Mẫu

**Package:** `com.company.lims.sample`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `TestRequest` | id, sourceType, sampleType, batchId, processStage, formulationTrialId, customerId, projectId, requestedBy, requestDate, dueDate, priority, testScope, status | Nguồn có thể là nội bộ (batch/trial) hoặc khách hàng (customer/project) |
| `Sample` | id, requestId, departmentId, sampleCode, status, receivedAt | |
| `SampleStatusHistory` | id, sampleId, fromStatus, toStatus, changedBy, changedAt, reason | Lịch sử đổi trạng thái mẫu (state machine log) |
| `SampleAttachment` | id, sampleId, filename, fileUrl, mimeType, uploadedBy | File đính kèm (COA khách gửi, ảnh mẫu...) |

### Quan hệ
- `TestRequest (1) → (N) Sample`
- `Sample (1) → (N) SampleStatusHistory`, `(1) → (N) SampleAttachment`
- `Sample (1) → (N) Test` (module `testing`)

### Gợi ý thiết kế
- `Sample.status` nên implement như **state machine** (Spring Statemachine hoặc enum + validation service) vì có bảng lịch sử riêng — tránh update status trực tiếp mà không ghi log.

---

## 6. Module `method` — Phương pháp thử & Tiêu chuẩn

**Package:** `com.company.lims.method`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `Method` | id, departmentId, name, version, sourceStandard, validationStatus, bodyTemplate | Quy trình thử nghiệm (SOP) |
| `MethodStepItem` | id, methodId, stepKey, label, role, expectedItemId (FK → InventoryItem), expectedQuantity, unit, orderIndex | Các bước chuẩn trong 1 method, có thể yêu cầu vật tư |
| `MethodValidationProtocol` | id, methodId, protocolCode, validationType, triggerReason, status, approvedBy, approvedDate | Hồ sơ thẩm định phương pháp |
| `ValidationParameterResult` | id, protocolId, parameterName, acceptanceCriteria, actualResult, passFail | Kết quả từng chỉ tiêu thẩm định |
| `SpecificationSet` | id, formulationId, version, effectiveDate, status, changeReason | Bộ tiêu chuẩn chất lượng áp cho 1 formulation |
| `SpecificationItem` | id, specSetId, methodId, analyte, minLimit, maxLimit, unit | Từng chỉ tiêu trong bộ tiêu chuẩn, gắn với 1 method dùng để test |
| `FormTemplate` | id, methodId, version, status, schemaName | Form nhập liệu động cho method |
| `FormField` | id, templateId, fieldKey, label, fieldType, dataBinding, isRequired, orderIndex | Định nghĩa field động (dynamic form builder) |

### Quan hệ
- `Method (1) → (N) MethodStepItem`
- `Method (1) → (N) MethodValidationProtocol → (N) ValidationParameterResult`
- `Method (1) → (N) FormTemplate → (N) FormField`
- `SpecificationSet (1) → (N) SpecificationItem → (1) Method`
- `Batch (module product) → SpecificationSet`

> Đây là module phức tạp nhất về mặt "metadata-driven". `FormTemplate/FormField` + `FormSubmission` (module `testing`) tạo thành cơ chế **dynamic form** — cân nhắc dùng JSON schema (`schemaName`/`dataBinding`) và validate ở service layer thay vì hard-code entity cho từng loại test.

---

## 7. Module `testing` — Thực hiện Test & Kết quả

**Package:** `com.company.lims.testing`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `AnalyticalRun` | id, instrumentId, runDate | Một lượt chạy máy, có thể phục vụ nhiều test |
| `Test` | id, sampleId, methodId, runId, formTemplateId, status | Một phép thử cụ thể trên 1 mẫu |
| `TestStepExecution` | id, testId, methodStepItemId, inventoryLotId, instrumentId, actualQuantity, unit, performedBy, performedAt | Ghi nhận thực tế từng bước (đối chiếu với `MethodStepItem`), tiêu thụ vật tư qua `inventoryLotId` |
| `FormSubmission` | id, testId, submittedBy, submittedAt, data (JSON) | Dữ liệu nhập theo `FormTemplate` |
| `Result` | id, testId, analyte, value, unit, passFail | Kết quả cuối, đối chiếu `SpecificationItem` |
| `TestResultRevision` | id, resultId, oldValue, newValue, revisedBy, revisedAt, reason | Lịch sử sửa kết quả — bắt buộc cho tính toàn vẹn dữ liệu LIMS (GxP/ALCOA+) |

### Quan hệ
- `Sample (1) → (N) Test → (N) TestStepExecution`
- `Test (1) → (N) Result → (N) TestResultRevision`
- `Test (1) → (1) FormSubmission`
- `AnalyticalRun (1) → (N) Test`

### Gợi ý thiết kế
- Không cho phép `UPDATE` trực tiếp trên `Result.value` sau khi đã duyệt — mọi thay đổi phải đi qua service tạo `TestResultRevision` (audit trail cấp field, khác với `AuditTrail` chung).
- `TestStepExecution.inventoryLotId` liên kết sang module `inventory` để trừ tồn kho theo từng bước thực hiện thực tế.

---

## 8. Module `approval` — Phê duyệt & Báo cáo

**Package:** `com.company.lims.approval`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `ApprovalStep` | id, entityType, entityId, stepOrder, requiredRole, status, actionedBy, actionedAt, comment, eSignatureHash | Generic approval workflow (polymorphic, áp dụng cho Sample, Test, Batch, Dossier...) |
| `Report` | id, sampleId, version, status, filePath, generatedBy, createdAt | Báo cáo kết quả xuất ra (PDF/COA) |

### Quan hệ
- `ApprovalStep` không FK cứng tới entity cụ thể (dùng `entityType` + `entityId`) → generic workflow engine, tái sử dụng cho nhiều loại đối tượng cần duyệt.
- `Report (N) → Sample (1)`

### Gợi ý thiết kế
- Đây là ứng viên tốt để thiết kế thành **Workflow Engine nội bộ**: `ApprovalStep` là instance của step, có thể định nghĩa thêm bảng `APPROVAL_WORKFLOW_DEFINITION` (chưa có trong ERD) nếu cần cấu hình số bước động theo loại entity.
- `eSignatureHash`: cần tuân thủ 21 CFR Part 11 nếu hướng tới thị trường dược — nên tách riêng 1 `SignatureService`.

---

## 9. Module `equipment` — Thiết bị

**Package:** `com.company.lims.equipment`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `Instrument` | id, departmentId, name, model, assetCode | |
| `CalibrationRecord` | id, instrumentId, performedAt, nextDue, performedBy, certificateUrl, notes | |
| `MaintenanceRecord` | id, instrumentId, performedAt, nextDue, performedBy, description | |

### Quan hệ
- `Instrument (1) → (N) CalibrationRecord`, `(1) → (N) MaintenanceRecord`
- `Instrument` được tham chiếu bởi `AnalyticalRun` và `TestStepExecution` (module `testing`)

### Gợi ý
- Nên có scheduled job (Spring `@Scheduled`) quét `nextDue` để bắn `Notification` nhắc hiệu chuẩn/bảo trì sắp tới hạn — tích hợp với module `audit`.

---

## 10. Module `inventory` — Kho hóa chất & Vật tư

**Package:** `com.company.lims.inventory`

### Entities
| Entity | Field chính | Ghi chú |
|---|---|---|
| `InventoryItem` | id, departmentId, name, category, unit, reorderLevel | Danh mục vật tư/hóa chất |
| `InventoryLot` | id, inventoryItemId, lotNumber, expiryDate, quantityRemaining | Từng lô nhập của 1 item |
| `InventoryUsageLog` | id, inventoryLotId, sourceType, sourceId, quantityUsed, balanceAfter, usedBy, usedAt | Log xuất/nhập kho (generic source qua sourceType/sourceId) |
| `SupplyRequest` | id, requestedBy, sampleId, purpose, requestDate, status | Yêu cầu cấp vật tư |
| `PreparationRecord` | id, resultLotId (FK → InventoryLot lô tạo ra), methodId, preparedBy, preparedAt, expiryAt, status | Pha chế dung dịch/thuốc thử từ các lot khác |
| `PreparationInput` | id, preparationId, sourceLotId, quantityUsed, unit | Nguyên liệu đầu vào của 1 lần pha chế |

### Quan hệ
- `InventoryItem (1) → (N) InventoryLot`
- `InventoryLot (1) → (N) InventoryUsageLog`
- `PreparationRecord (1) → (N) PreparationInput → (1) InventoryLot` (lô nguồn), và `PreparationRecord → InventoryLot` (lô kết quả)
- `SupplyRequest → Sample` (module `sample`)
- `TestStepExecution` (module `testing`) ghi nhận `inventoryLotId` khi tiêu thụ → nên **trigger** tạo `InventoryUsageLog` tương ứng (qua domain event, không gọi trực tiếp giữa 2 module).

---

## Ma trận phụ thuộc giữa các module

```
auth        ← được tất cả module khác tham chiếu (userId, departmentId)
audit       ← lắng nghe event từ tất cả module (không phụ thuộc ngược)
partner     → sample
product     → sample, method (qua SpecificationSet/Batch)
sample      → testing, approval, inventory (SupplyRequest)
method      → testing (Test.methodId, FormTemplate)
testing     → inventory (tiêu thụ lot), approval (duyệt kết quả)
equipment   ← testing (AnalyticalRun, TestStepExecution), method-adjacent
approval    ← generic, tham chiếu polymorphic tới sample/testing/product
inventory   ← testing, sample (độc lập tương đối)
```

**Nguyên tắc chống phụ thuộc vòng (circular dependency):**
- Chỉ dùng FK ID (Long) khi tham chiếu sang module khác, **không** dùng `@ManyToOne` load trực tiếp entity của module khác nếu muốn giữ module thực sự độc lập (đặc biệt hữu ích nếu sau này tách microservice).
- Giao tiếp giữa module nên qua **Spring Application Event** (`ApplicationEventPublisher`) cho các side-effect (VD: `TestStepExecution` tạo xong → publish `InventoryConsumedEvent` → `inventory` module lắng nghe và tạo `InventoryUsageLog`), thay vì gọi service module khác trực tiếp.

---

## Gợi ý thứ tự triển khai (implementation roadmap)

1. **`auth` + `common`** — nền tảng bắt buộc trước (security, base entity).
2. **`equipment`** — độc lập, ít phụ thuộc, dễ làm trước để quen pattern.
3. **`product`** — master data (Product, Ingredient, Formulation, Batch).
4. **`partner`** — Customer, Project (đơn giản).
5. **`method`** — SOP, Specification, Form template (phức tạp, cần chốt kỹ trước khi làm `testing`).
6. **`sample`** — TestRequest, Sample + state machine.
7. **`inventory`** — độc lập, có thể làm song song với (5)/(6).
8. **`testing`** — phụ thuộc `sample`, `method`, `equipment`, `inventory`.
9. **`approval`** — generic workflow, làm sau khi có ít nhất `sample`/`testing` để có đối tượng cần duyệt.
10. **`audit`** — triển khai xuyên suốt bằng AOP/Event, hoàn thiện cuối để bắt được toàn bộ audit points.

---

## Enum dùng chung nên đặt ở `common.enums`

- `EntityType` (dùng cho `AuditTrail`, `ApprovalStep`, `InventoryUsageLog`, `SampleAttachment` polymorphic reference)
- `SampleStatus`, `TestStatus`, `ApprovalStatus`, `RequestPriority`, `ValidationStatus`, `RoleType`...

