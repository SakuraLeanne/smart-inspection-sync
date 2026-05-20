-- ============================================================
-- 文件: 06_validate_cus_std_and_model.sql
-- 目标: 标准层与模型层结果快速验收脚本（人工巡检）
-- ============================================================

-- 1) 原始表与标准表规模对比
SELECT COUNT(*) FROM cus_raw_customer_service;
SELECT COUNT(*) FROM cus_std_work_order;
SELECT COUNT(*) FROM cus_raw_customer_service_history;
SELECT COUNT(*) FROM cus_std_work_order_history;

-- 2) 指定工单抽样核对
SELECT * FROM cus_std_work_order WHERE order_no = 'BX202605120042';
SELECT * FROM cus_std_work_order_history WHERE order_no = 'BX202605120042' ORDER BY operation_time;

-- 3) 关键字段空值检查
SELECT COUNT(*) FROM cus_std_work_order WHERE report_time IS NULL;

SELECT COUNT(*) FROM cus_std_work_order
WHERE location_id IS NULL AND location_name IS NULL AND location_text IS NULL;

-- 4) 关键词分布检查
SELECT content_keyword, COUNT(*) FROM cus_std_work_order GROUP BY content_keyword ORDER BY COUNT(*) DESC;

-- 5) 模型结果分级分布检查
SELECT warning_level, COUNT(*) FROM cus_model_repeat_repair_result GROUP BY warning_level;
SELECT * FROM cus_model_repeat_repair_result ORDER BY repeat_count DESC LIMIT 20;
