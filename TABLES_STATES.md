# Bảng: purchase_orders (Đơn đặt hàng nhà cung cấp)
Cấu trúc: Status,Ý nghĩa,Trigger / Luồng cập nhật
DRAFT,Bản nháp,Mặc định khi mới tạo PO.
ORDERED,Đã gửi NCC,"Người dùng bấm ""Xác nhận gửi PO""."
PARTIAL,Nhận thiếu hàng,Khi có một inbound_receipts chuyển sang COMPLETED nhưng tổng qc_passed_qty < ordered_qty.
RECEIVED,Nhận đủ hàng,Khi qc_passed_qty đã bằng hoặc lớn hơn ordered_qty.
CANCELLED,Hủy PO,Người dùng hủy trước khi có bất kỳ phiếu nhận hàng nào.

# Bảng: inbound_receipts (Phiếu nhận hàng)
Status,Ý nghĩa,Trigger / Luồng cập nhật
PENDING,Hàng tới bãi/kho,Mới tạo phiếu lúc xe tải tới.
INSPECTING,Đang kiểm KCS đầu vào,Khi bắt đầu nhập số lượng vào inbound_receipt_details.
COMPLETED,Chốt phiếu,Quản lý kho bấm xác nhận. Hệ thống tự động kích hoạt ghi log inventory_transactions và cập nhật material_stocks.

# Bảng: material_stocks & fabric_rolls (Tồn kho vật tư & Cuộn vải)
Status,Ý nghĩa,Trigger / Luồng cập nhật
PENDING_QC,Chờ kiểm tra,Mặc định khi sinh ra từ một Phiếu nhận hàng đang INSPECTING.
AVAILABLE,Sẵn sàng sử dụng,Khi Phiếu nhận hàng chuyển sang COMPLETED.
IN_USE,Đang đưa vào cắt,(Chỉ dành cho vải) Bắt đầu xuất theo material_issue_tickets.
SPLITTED,Đã chia nhỏ,"(Chỉ dành cho vải) Bị cắt thành các cuộn nhỏ, chiều dài cuộn gốc về 0."
OUT_OF_STOCK / USED_UP,Đã dùng hết,Khi trigger của inventory_transactions ghi nhận quantity hoặc current_length = 0.

# Bảng: material_issue_tickets (Phiếu yêu cầu xuất kho)
Status,Ý nghĩa,Trigger / Luồng cập nhật
REQUESTED,Chờ duyệt,Tổ trưởng tạo phiếu xin vật tư từ bảng BOM.
APPROVED,Quản đốc đã duyệt,Quản đốc bấm duyệt. Kho bắt đầu soạn hàng.
ISSUED,Đã xuất kho,Thủ kho bấm xuất. Hệ thống tự động trigger sinh inventory_transactions trừ tồn kho.

# Bảng: bom_headers (Định mức nguyên liệu)
Status,Ý nghĩa,Trigger / Luồng cập nhật
DRAFT,Đang thiết kế rập,"Mới tạo, đang thêm/bớt vật tư."
ACTIVE,Đang áp dụng,Quản lý Kỹ thuật chốt BOM. Bắt đầu dùng để tính định mức xuất kho.
INACTIVE,Dừng áp dụng,Đổi mẫu hoặc version BOM này đã cũ.

# Bảng: production_orders (Lệnh sản xuất)
Status,Ý nghĩa,Trigger / Luồng cập nhật
PLANNED,Kế hoạch,"Tạo từ đơn hàng, đang chờ xin vật tư."
IN_PROGRESS,Đang sản xuất,Kích hoạt khi có phiếu material_issue_tickets đầu tiên được ISSUED (Đã có hàng vào xưởng).
COMPLETED,Hoàn thành Lệnh,Kích hoạt khi toàn bộ bundles của Lệnh này đã ra khỏi chuyền và pass KCS.
CANCELLED,Hủy,Xóa lệnh (chỉ được hủy khi status đang là PLANNED).

# Bảng: bundles (Quản lý Bó hàng)
Status,Ý nghĩa,Trigger / Luồng cập nhật
CREATED,Vừa phân bó xong,"Công nhân cắt xong, sinh mã vạch chờ lên chuyền."
IN_SEWING,Đang trên chuyền,Khi record đầu tiên trong production_logs được tạo (bắt đầu trạm may 1).
FINISHED_SEWING,Hoàn thành chuyền,Bó hàng vượt qua sequence cuối cùng trong bảng routing. Chuyển qua Service 4.
READY_FOR_PACKING,Pass QC cuối chuyền,Khi final_inspections trả kết quả PASSED. Sẵn sàng đóng thùng.
REJECTED,Hủy bỏ cả bó,Khi final_inspections trả kết quả SCRAP (hỏng nặng toàn bộ bó).

# Bảng: production_logs (Nhật ký trạm may)
Status,Ý nghĩa,Trigger / Luồng cập nhật
PENDING,Bó hàng tới trạm,Trạm trước vừa may xong đẩy sang.
SEWING,Đang may,"Công nhân scan mã bó bắt đầu tính giờ (start_time). Nếu QC bắt REWORK, status cũng quay lại trạng thái này."
COMPLETED,Chốt số lượng trạm,Quét hoàn thành. Update good_qty và kết thúc giờ (end_time).

# Bảng: final_inspections (KCS Cuối Chuyền)
Status,Ý nghĩa,Trigger / Luồng cập nhật
PASSED,Đạt chuẩn Grade A,Đóng gói xuất khẩu. Kích hoạt update bundles.status = READY_FOR_PACKING.
B_GRADE,Hàng hạ cấp,"Khách chê, bán nội địa. Tự động sinh giao dịch nhập kho loại B."
SCRAP,Vứt bỏ hoàn toàn,Lỗi nặng. Kích hoạt update bundles.status = REJECTED. Ghi nhận lỗ.

# Bảng: shipments (Lô hàng xuất khẩu)
Status,Ý nghĩa,Trigger / Luồng cập nhật
SHIPPING,Đang vận chuyển,"Đã gom đủ các thùng (packing_lists), xe rời bãi."
DELIVERED,Đã giao,Khách hàng/Cảng ký nhận. Kích hoạt đóng lệnh production_orders = COMPLETED tương ứng.

