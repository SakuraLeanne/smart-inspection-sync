SET SESSION group_concat_max_len = 10240;

/*
兼容说明：当前部分环境的 cus_raw_customer_service 为精简版字段集，
不存在 organization_item_id/place/address/customer_id/appointment_time/dispatch_time 等列。
本脚本按“精简字段可运行”实现：缺失字段先置 NULL，后续可通过补齐 raw 表结构升级。
*/
INSERT INTO cus_std_work_order (
  source_system, source_order_id, order_no, project_id, project_name, location_id, location_name, location_text,
  customer_id, report_person, phone, service_source, service_source_name, service_type, service_type_name,
  repair_status, repair_status_name, dispatch_type_id, dispatch_type_name, dispatch_type_parent_id,
  dispatch_type_parent_name, dispatch_priority_id, dispatch_priority_name, report_time, accepted_time,
  appointment_time, required_finish_time, dispatch_time, start_process_time, finish_time, confirm_time,
  report_content, content_keyword, content_normalized, comment_level, comment_level_name, comment_content,
  comment_date, comment_by_customer, has_history, history_count, last_history_time, last_history_content,
  last_word_content, is_repair_order, is_closed, is_finished, is_valid, raw_sync_time
)
SELECT
  'property', cs.source_id, cs.voucher_no, cs.region_id, NULL, NULL, NULL, NULL,
  NULL, cs.customer_name, cs.phone, cs.service_source,
  CASE WHEN cs.service_source = 1 THEN '自检自查' ELSE CONCAT('来源-', IFNULL(cs.service_source, 'NULL')) END,
  cs.service_type, CASE WHEN cs.service_type = 0 THEN '报修' ELSE CONCAT('服务类型-', IFNULL(cs.service_type, 'NULL')) END,
  NULL, NULL,
  NULL, NULL, NULL, NULL, NULL, NULL,
  cs.accepted_date, cs.accepted_date, NULL, cs.finish_time_required, NULL,
  NULL, NULL, cs.confirm_time,
  cs.details,
  CASE
    WHEN cs.details LIKE '%漏水%' THEN '漏水'
    WHEN cs.details LIKE '%堵%' THEN '堵塞'
    WHEN cs.details LIKE '%跳闸%' THEN '跳闸'
    WHEN cs.details LIKE '%照明%' OR cs.details LIKE '%灯%' THEN '照明'
    WHEN cs.details LIKE '%玻璃%' THEN '玻璃'
    WHEN cs.details LIKE '%消防%' THEN '消防'
    WHEN cs.details LIKE '%门禁%' THEN '门禁'
    WHEN cs.details LIKE '%电梯%' THEN '电梯'
    WHEN cs.details LIKE '%空调%' THEN '空调'
    WHEN cs.details LIKE '%清理%' OR cs.details LIKE '%卫生%' THEN '清理'
    WHEN cs.details LIKE '%违停%' OR cs.details LIKE '%乱停%' THEN '违停'
    WHEN cs.details LIKE '%电动车%' OR cs.details LIKE '%电瓶车%' THEN '电动车'
    WHEN cs.details LIKE '%小车%' OR cs.details LIKE '%机动车%' THEN '车辆'
    WHEN cs.details LIKE '%花盆%' THEN '花盆'
    WHEN cs.details LIKE '%树枝%' OR cs.details LIKE '%枯树%' THEN '树枝'
    ELSE '其他' END,
  TRIM(cs.details),
  cs.comment,
  CASE cs.comment WHEN 0 THEN '好评' WHEN 1 THEN '中评' WHEN 2 THEN '差评' ELSE NULL END,
  cs.comment_content, NULL, 0,
  CASE WHEN hs.history_count > 0 THEN 1 ELSE 0 END,
  IFNULL(hs.history_count, 0), hs.last_history_time, hs.last_history_content, hs.last_word_content,
  CASE WHEN cs.service_type = 0 THEN 1 ELSE 0 END,
  0,
  CASE WHEN hs.history_count > 0 THEN 1 ELSE 0 END,
  1, cs.sync_time
FROM cus_raw_customer_service cs
LEFT JOIN (
  SELECT source_order_id,
         COUNT(*) AS history_count,
         MAX(operation_time) AS last_history_time,
         SUBSTRING_INDEX(GROUP_CONCAT(operation_content ORDER BY operation_time DESC SEPARATOR '||'), '||', 1) AS last_history_content,
         SUBSTRING_INDEX(GROUP_CONCAT(word_content ORDER BY operation_time DESC SEPARATOR '||'), '||', 1) AS last_word_content
  FROM cus_std_work_order_history
  GROUP BY source_order_id
) hs ON hs.source_order_id = cs.source_id
ON DUPLICATE KEY UPDATE
  order_no=VALUES(order_no), project_id=VALUES(project_id), project_name=VALUES(project_name), location_id=VALUES(location_id),
  location_name=VALUES(location_name), location_text=VALUES(location_text), customer_id=VALUES(customer_id), report_person=VALUES(report_person),
  phone=VALUES(phone), service_source=VALUES(service_source), service_source_name=VALUES(service_source_name), service_type=VALUES(service_type),
  service_type_name=VALUES(service_type_name), repair_status=VALUES(repair_status), repair_status_name=VALUES(repair_status_name),
  dispatch_type_id=VALUES(dispatch_type_id), dispatch_type_name=VALUES(dispatch_type_name), dispatch_type_parent_id=VALUES(dispatch_type_parent_id),
  dispatch_type_parent_name=VALUES(dispatch_type_parent_name), dispatch_priority_id=VALUES(dispatch_priority_id), dispatch_priority_name=VALUES(dispatch_priority_name),
  report_time=VALUES(report_time), accepted_time=VALUES(accepted_time), appointment_time=VALUES(appointment_time),
  required_finish_time=VALUES(required_finish_time), dispatch_time=VALUES(dispatch_time), start_process_time=VALUES(start_process_time),
  finish_time=VALUES(finish_time), confirm_time=VALUES(confirm_time), report_content=VALUES(report_content), content_keyword=VALUES(content_keyword),
  content_normalized=VALUES(content_normalized), comment_level=VALUES(comment_level), comment_level_name=VALUES(comment_level_name),
  comment_content=VALUES(comment_content), comment_date=VALUES(comment_date), comment_by_customer=VALUES(comment_by_customer),
  has_history=VALUES(has_history), history_count=VALUES(history_count), last_history_time=VALUES(last_history_time),
  last_history_content=VALUES(last_history_content), last_word_content=VALUES(last_word_content), is_repair_order=VALUES(is_repair_order),
  is_closed=VALUES(is_closed), is_finished=VALUES(is_finished), is_valid=VALUES(is_valid), raw_sync_time=VALUES(raw_sync_time), std_update_time=CURRENT_TIMESTAMP;
