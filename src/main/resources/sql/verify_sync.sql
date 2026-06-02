-- 全量同步结果验证脚本
-- 仅验证当前保留的 SQL Server -> MySQL 原始同步表与执行日志。

-- 1) 原始同步表行数
SELECT 'cus_raw_customer_service' AS table_name, COUNT(*) AS row_count FROM cus_raw_customer_service
UNION ALL
SELECT 'cus_raw_customer_service_history' AS table_name, COUNT(*) AS row_count FROM cus_raw_customer_service_history
UNION ALL
SELECT 'cus_raw_organization_item' AS table_name, COUNT(*) AS row_count FROM cus_raw_organization_item
UNION ALL
SELECT 'cus_raw_materials_inventory_request' AS table_name, COUNT(*) AS row_count FROM cus_raw_materials_inventory_request
UNION ALL
SELECT 'cus_raw_materials_inventory_request_detail' AS table_name, COUNT(*) AS row_count FROM cus_raw_materials_inventory_request_detail;

-- 2) 各表当前最大源主键，用于判断全量扫描是否覆盖到最新源数据
SELECT 'cus_raw_customer_service' AS table_name, MAX(source_id) AS max_source_id FROM cus_raw_customer_service
UNION ALL
SELECT 'cus_raw_customer_service_history' AS table_name, MAX(source_id) AS max_source_id FROM cus_raw_customer_service_history
UNION ALL
SELECT 'cus_raw_organization_item' AS table_name, MAX(source_id) AS max_source_id FROM cus_raw_organization_item
UNION ALL
SELECT 'cus_raw_materials_inventory_request' AS table_name, MAX(source_id) AS max_source_id FROM cus_raw_materials_inventory_request
UNION ALL
SELECT 'cus_raw_materials_inventory_request_detail' AS table_name, MAX(source_id) AS max_source_id FROM cus_raw_materials_inventory_request_detail;

-- 3) 最近同步任务日志
SELECT id, task_code, sync_type, status, read_count, write_count, start_time, end_time, error_message
FROM cus_sync_task_log
ORDER BY id DESC
LIMIT 20;
