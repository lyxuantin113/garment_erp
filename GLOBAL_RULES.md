# BUSINESS RULES & ACTIVITY POLICY (MICROSERVICES ARCHITECTURE)

0. GLOBAL_RULES (Quy tắc toàn cục)
   ├── 0.1. IMMUTABLE_INVENTORY
   │   └── Rule: Cấm UPDATE trực tiếp cột [quantity] trong bảng [material_stocks].
   │   └── Action: Mọi thay đổi phải thông qua việc INSERT vào [inventory_transactions].
   ├── 0.2. OPTIMISTIC_LOCKING
   │   └── Rule: Cập nhật trạng thái phải check cột [version].
   │   └── Action: Throw ConcurrentModificationException nếu version sai lệch.
   └── 0.3. EVENT_DRIVEN_COMMUNICATION
       └── Rule: Không dùng Foreign Key xuyên Service.
       └── Action: Publish Event (Kafka/RabbitMQ) để trigger logic ở Service khác.

-----------------------------------------------------------------------

1. INVENTORY_AND_PROCUREMENT_SERVICE (Kho & Mua Hàng)
   ├── 1.1. ACTION: INBOUND_AND_QC (Nhận & Kiểm hàng đầu vào)
   │   ├── Trigger: Khởi tạo [inbound_receipts].
   │   ├── Logic:
   │   │   ├── Calculate: actual_received_qty = qc_passed_qty + qc_rejected_qty
   │   │   ├── Condition 1: IF qc_passed_qty >= expected_qty
   │   │   │   └── Update: [purchase_orders.status] -> RECEIVED
   │   │   └── Condition 2: ELSE IF (qc_passed_qty > 0 AND qc_passed_qty < expected_qty)
   │   │       └── Update: [purchase_orders.status] -> PARTIAL
   │   └── Finalize: 
   │       └── When [inbound_receipts.status] == COMPLETED
   │           └── Insert: [inventory_transactions] (Type: INBOUND, Qty: qc_passed_qty)
   │
   └── 1.2. ACTION: MATERIAL_ISSUE (Xuất kho cho Sản xuất)
       ├── Pre-condition: [production_orders.status] == PLANNED AND [material_issue_tickets.status] == APPROVED
       ├── Logic:
       │   ├── Input: Thủ kho nhập actual_issued_qty
       │   ├── Validate: IF actual_issued_qty > current_stock_quantity
       │   │   └── Throw: InsufficientStockException
       │   └── Success:
       │       ├── Update: [material_issue_tickets.status] -> ISSUED
       │       └── Insert: [inventory_transactions] (Type: ISSUE_TO_PRODUCTION, Qty: -actual_issued_qty)
       └── Output_Event: Publish [MATERIAL_ISSUED_EVENT]

-----------------------------------------------------------------------

2. PRODUCTION_SERVICE (Xưởng Sản Xuất)
   ├── 2.1. ACTION: CUTTING_AND_BUNDLING (Cắt & Phân bó)
   │   ├── Trigger: Listen to [MATERIAL_ISSUED_EVENT] AND [fabric_rolls.status] == AVAILABLE
   │   ├── Logic:
   │   │   ├── Condition 1: IF (Cắt hết cuộn)
   │   │   │   └── Update: [fabric_rolls.status] -> USED_UP
   │   │   ├── Condition 2: IF (Cắt một nửa)
   │   │   │   ├── Update: Cuộn gốc [fabric_rolls.status] -> SPLITTED
   │   │   │   └── Insert: Cuộn mới vào [fabric_rolls] (Gắn parent_roll_id)
   │   │   ├── Execution: Gen mã vạch, Insert vào [bundles]
   │   │   │   ├── Set: [bundles.status] = CREATED
   │   │   │   └── Set: [bundles.current_good_qty] = initial_qty
   │   │   └── Update: [production_orders.status] -> IN_PROGRESS
   │
   └── 2.2. ACTION: INLINE_QC_AND_ROUTING (Nhật ký Chuyền May - LOOP)
       ├── Pre-condition: Bó hàng nằm đúng routing sequence.
       ├── Logic (Per Station):
       │   ├── Step 1 (Start): Insert [production_logs] (Status: SEWING), Ghi start_time.
       │   ├── Step 2 (Defect Check): IF defect_qty > 0
       │   │   ├── Insert: [defect_logs]
       │   │   ├── Sub-condition 1: IF [disposition] == REWORK
       │   │   │   └── Keep bó hàng tại trạm, công nhân sửa lỗi (Không trừ good_qty).
       │   │   └── Sub-condition 2: IF [disposition] == SCRAP
       │   │       └── Calculate: [bundles.current_good_qty] -= defect_qty
       │   ├── Step 3 (End Station): 
       │   │   ├── Update: [production_logs.status] -> COMPLETED, Ghi end_time.
       │   │   └── Calculate: [bundles.current_routing_sequence] += 1
       │   └── Step 4 (Exit Loop): IF current_routing_sequence > max_routing_steps
       │       ├── Update: [bundles.status] -> FINISHED_SEWING
       │       └── Output_Event: Publish [BUNDLE_FINISHED_EVENT]

-----------------------------------------------------------------------

3. QUALITY_AND_SHIPPING_SERVICE (KCS Cuối Chuyền & Đóng gói)
   ├── 3.1. ACTION: FINAL_INSPECTION (Chốt KCS Thành Phẩm)
   │   ├── Pre-condition: [bundles.status] == FINISHED_SEWING
   │   ├── Logic:
   │   │   ├── Condition 1: IF [final_inspections.status] == PASSED
   │   │   │   └── Update: [bundles.status] -> READY_FOR_PACKING
   │   │   ├── Condition 2: IF [final_inspections.status] == B_GRADE
   │   │   │   ├── Update: [bundles.status] -> FINISHED (Không cho phép đóng gói đơn này)
   │   │   │   └── Output_Event: Publish [DOWNGRADE_SKU_EVENT] -> (Inventory Service tạo mã nội địa)
   │   │   └── Condition 3: IF [final_inspections.status] == SCRAP
   │   │       └── Update: [bundles.status] -> REJECTED (Hủy bó hàng)
   │
   ├── 3.2. ACTION: MAKE_UP_ORDER_AGENT (Thuật toán tự động Lệnh bù)
   │   ├── Trigger: Khi toàn bộ bó hàng của một PO đã có kết quả final_inspections.
   │   ├── Logic:
   │   │   ├── Calculate: total_passed = SUM(passed_qty) của PO đó.
   │   │   ├── Validate: required_min_qty = total_qty * (100% - Tolerance_Percent)
   │   │   └── Condition: IF total_passed < required_min_qty
   │   │       ├── Calculate: shortfall_qty = required_min_qty - total_passed
   │   │       └── Execution: Insert Lệnh bù vào [production_orders] 
   │   │           ├── Set: [parent_order_id] = original_po_id
   │   │           ├── Set: [type] = MAKE_UP
   │   │           ├── Set: [status] = PLANNED
   │   │           └── Set: [total_qty] = shortfall_qty
   │   │               -> (Quay lại vòng lặp ACTION 1.2 Xuất kho)
   │
   └── 3.3. ACTION: PACKING_AND_CLOSURE (Đóng gói & Chốt Đơn)
       ├── Pre-condition: Chỉ quét mã các bundle có [bundles.status] == READY_FOR_PACKING
       ├── Logic:
       │   ├── Step 1 (Pack): Insert [carton_details] & [packing_lists].
       │   ├── Step 2 (Ship): Cập nhật [shipments.status] -> SHIPPING (khi xe chạy).
       │   ├── Step 3 (Deliver): Cập nhật [shipments.status] -> DELIVERED (khách nhận).
       │   └── Step 4 (Close PO): 
       │       ├── Output_Event: Publish [ORDER_FULFILLED_EVENT]
       │       └── Listen (Production Service): Bắt event -> Update [production_orders.status] -> COMPLETED (Freeze PO).

       