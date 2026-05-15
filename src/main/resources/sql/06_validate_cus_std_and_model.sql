SELECT COUNT(*) FROM cus_raw_customer_service;
SELECT COUNT(*) FROM cus_std_work_order;
SELECT COUNT(*) FROM cus_raw_customer_service_history;
SELECT COUNT(*) FROM cus_std_work_order_history;

SELECT * FROM cus_std_work_order WHERE order_no = 'BX202605120042';
SELECT * FROM cus_std_work_order_history WHERE order_no = 'BX202605120042' ORDER BY operation_time;

SELECT COUNT(*) FROM cus_std_work_order WHERE report_time IS NULL;

SELECT COUNT(*) FROM cus_std_work_order
WHERE location_id IS NULL AND location_name IS NULL AND location_text IS NULL;

SELECT content_keyword, COUNT(*) FROM cus_std_work_order GROUP BY content_keyword ORDER BY COUNT(*) DESC;

SELECT warning_level, COUNT(*) FROM cus_model_repeat_repair_result GROUP BY warning_level;
SELECT * FROM cus_model_repeat_repair_result ORDER BY repeat_count DESC LIMIT 20;
