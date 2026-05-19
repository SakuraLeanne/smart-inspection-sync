-- ============================================================
-- 文件: 02_build_cus_std_work_order_history.sql
-- 目标: 从原始处理记录表构建标准处理记录表 cus_std_work_order_history
-- 说明: 使用 ON DUPLICATE KEY UPDATE 实现幂等更新
-- ============================================================

INSERT INTO cus_std_work_order_history (
  source_system, source_history_id, source_order_id, order_no, operation_time, operation_user_id,
  operation_user_name, operation_status, operation_status_name, operation_content, word_content,
  dispatch_id, complaint_handle_id, is_dispatch, is_start_process, is_finish, is_confirm,
  is_return_visit, is_close, is_valid, raw_sync_time
)
SELECT
  'property', h.source_id, h.customer_service_id, cs.voucher_no, h.create_date, h.created_by_id,
  NULL, h.operation_status,
  CASE h.operation_status
    WHEN 0 THEN '提交/创建' WHEN 1 THEN '派工' WHEN 2 THEN '开始处理'
    WHEN 4 THEN '回单/完成处理' WHEN 5 THEN '确认' WHEN 17 THEN '回访/关闭'
    ELSE CONCAT('未知状态-', IFNULL(h.operation_status, 'NULL')) END,
  LEFT(h.content, 1000), h.word_content, h.dispatch_id, h.complaint_handle_id,
  CASE WHEN h.operation_status = 1 OR h.content LIKE '%派工%' OR h.content LIKE '%指派%' THEN 1 ELSE 0 END,
  CASE WHEN h.operation_status = 2 OR h.content LIKE '%开始处理%' THEN 1 ELSE 0 END,
  CASE WHEN h.operation_status = 4 OR h.content LIKE '%完成%' OR h.content LIKE '%回单%' THEN 1 ELSE 0 END,
  CASE WHEN h.operation_status = 5 OR h.content LIKE '%确认%' THEN 1 ELSE 0 END,
  CASE WHEN h.operation_status = 17 OR h.content LIKE '%回访%' THEN 1 ELSE 0 END,
  CASE WHEN h.content LIKE '%关闭%' THEN 1 ELSE 0 END,
  1, h.sync_time
FROM cus_raw_customer_service_history h
LEFT JOIN cus_raw_customer_service cs ON cs.source_id = h.customer_service_id
ON DUPLICATE KEY UPDATE
  source_order_id=VALUES(source_order_id), order_no=VALUES(order_no), operation_time=VALUES(operation_time),
  operation_user_id=VALUES(operation_user_id), operation_status=VALUES(operation_status), operation_status_name=VALUES(operation_status_name),
  operation_content=VALUES(operation_content), word_content=VALUES(word_content), dispatch_id=VALUES(dispatch_id),
  complaint_handle_id=VALUES(complaint_handle_id), is_dispatch=VALUES(is_dispatch), is_start_process=VALUES(is_start_process),
  is_finish=VALUES(is_finish), is_confirm=VALUES(is_confirm), is_return_visit=VALUES(is_return_visit), is_close=VALUES(is_close),
  is_valid=VALUES(is_valid), raw_sync_time=VALUES(raw_sync_time), std_update_time=CURRENT_TIMESTAMP;
