# Base entity

Table _Base_Entity {
  created_at timestamp [default: `now()`]
  updated_at timestamp
  created_by uuid
  updated_by uuid
  is_deleted boolean [default: false]
  version int [default: 0]
}

Table audit_logs { // Mỗi service có 1 bảng này riêng trong DB của nó
  id bigserial [pk]
  entity_name varchar 
  entity_id varchar
  action varchar 
  old_payload jsonb 
  new_payload jsonb 
  changed_by uuid
  changed_at timestamp [default: `now()`]
}

# SERVICE 1: INVENTORY & PROCUREMENT (DB: db_inventory)

Table materials {
  id uuid [pk]
  code varchar [unique]
  name varchar
  base_unit varchar 
  note text
}

Table suppliers {
  id uuid [pk]
  name varchar
  phone_number text [unique]
  email text [unique]
  location text
}

Table purchase_orders {
  id uuid [pk]
  supplier_id uuid [ref: > suppliers.id]
  po_number varchar [unique]
  status varchar 
}

Table po_details {
  id uuid [pk]
  po_id uuid [ref: > purchase_orders.id]
  material_id uuid [ref: > materials.id]
  material_code_snapshot varchar 
  ordered_qty decimal
  received_qty decimal
  unit_price decimal
}

Table inbound_receipts {
  id uuid [pk]
  po_id uuid [ref: > purchase_orders.id]
  received_date timestamp
  received_by uuid
  status varchar 
}

Table inbound_receipt_details {
  id uuid [pk]
  receipt_id uuid [ref: > inbound_receipts.id]
  po_detail_id uuid [ref: > po_details.id]
  expected_qty decimal
  actual_received_qty decimal
  qc_passed_qty decimal
  qc_rejected_qty decimal
}

Table material_stocks {
  id uuid [pk]
  material_id uuid [ref: > materials.id]
  lot_number varchar 
  quantity decimal 
  bin_location varchar
  expiry_date date 
  inbound_receipt_id uuid [ref: > inbound_receipts.id]
  status varchar 
}

Table fabric_rolls {
  id uuid [pk]
  roll_code varchar [unique]
  material_id uuid [ref: > materials.id]
  parent_roll_id uuid [ref: > fabric_rolls.id]
  shade_lot varchar 
  original_length decimal
  current_length decimal
  bin_location varchar
  inbound_receipt_id uuid [ref: > inbound_receipts.id]
  status varchar 
}

Table inventory_transactions { 
  id bigserial [pk]
  material_stock_id uuid [ref: > material_stocks.id]
  fabric_roll_id uuid [ref: > fabric_rolls.id]
  transaction_type varchar 
  qty_change decimal 
  reference_id uuid // BỎ KHÓA NGOẠI: Có thể là ID của Lệnh Cắt, ID Ticket...
  created_at timestamp
}

// Chuyển Material Tickets về Service Kho vì nó tương tác trực tiếp với vật tư
Table material_issue_tickets { 
  id uuid [pk]
  ticket_code varchar [unique] 
  production_order_id uuid // BỎ KHÓA NGOẠI: Giao tiếp qua API với Production Service
  status varchar 
  requested_by uuid 
  issued_by uuid 
  issued_at timestamp
  note text
}

Table material_issue_details { 
  id uuid [pk]
  ticket_id uuid [ref: > material_issue_tickets.id]
  material_stock_id uuid [ref: > material_stocks.id]
  requested_qty decimal 
  actual_issued_qty decimal 
}

# SERVICE 2: TECHNICAL & BOM (DB: db_engineering)

Table bom_headers {
  id uuid [pk]
  style_code varchar [unique]
  style_name varchar
  version varchar [default: '1.0']
  status varchar 
}

Table bom_details {
  id uuid [pk]
  bom_id uuid [ref: > bom_headers.id]
  material_id uuid // BỎ KHÓA NGOẠI: Call API sang Inventory Service để lấy Tên Vật tư
  material_code_snapshot varchar
  consumption decimal 
  wastage_percent decimal 
}

Table routing {
  id uuid [pk]
  style_code varchar [ref: > bom_headers.style_code]
  operation_name varchar 
  sequence int 
  sam decimal 
}

# SERVICE 3: PRODUCTION (DB: db_production)

Table production_orders {
  id uuid [pk]
  parent_order_id uuid [ref: > production_orders.id]
  order_number varchar [unique]
  style_code varchar // BỎ KHÓA NGOẠI: Call API sang Engineering Service để validate
  total_qty int
  deadline date
  status varchar 
}

Table bundles {
  id uuid [pk]
  production_order_id uuid [ref: > production_orders.id]
  fabric_roll_id uuid // BỎ KHÓA NGOẠI: Thuộc về Inventory Service
  bundle_code varchar [unique] 
  initial_qty int 
  current_good_qty int 
  current_routing_sequence int
  status varchar 
}

Table production_logs {
  id uuid [pk]
  bundle_id uuid [ref: > bundles.id]
  operation_id uuid // BỎ KHÓA NGOẠI: Thuộc về Engineering Service (Routing table)
  worker_id uuid
  status varchar 
  input_qty int 
  good_qty int 
  defect_qty int 
  start_time timestamp
  end_time timestamp
}

Table defect_logs {
  id uuid [pk]
  production_log_id uuid [ref: > production_logs.id] 
  defect_reason_code varchar 
  defect_qty int 
  disposition varchar // REWORK, SCRAP
  auditor_id uuid 
}

# SERVICE 4: QUALITY & SHIPPING (DB: db_quality_shipping)

Table final_inspections {
  id uuid [pk]
  bundle_id uuid // BỎ KHÓA NGOẠI: Thuộc về Production Service
  inspection_type varchar 
  total_qty_submitted int 
  passed_qty int 
  rejected_qty int 
  status varchar 
  inspector_id uuid
  inspected_at timestamp
  note text
}

Table final_inspection_defects {
  id uuid [pk]
  final_inspection_id uuid [ref: > final_inspections.id]
  defect_category varchar 
  defect_qty int
}

Table shipments {
  id uuid [pk]
  customer_name varchar
  ship_date timestamp
  customer_phone text
  status varchar 
}

Table packing_lists {
  id uuid [pk]
  shipment_id uuid [ref: > shipments.id]
  carton_code varchar
  total_qty int
}

Table carton_details {
  id uuid [pk]
  packing_list_id uuid [ref: > packing_lists.id]
  bundle_id uuid // BỎ KHÓA NGOẠI: Thuộc về Production Service
  qty_in_carton int
}

