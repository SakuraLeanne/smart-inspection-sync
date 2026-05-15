SELECT COUNT(*) FROM cus_raw_customer_service;
SELECT COUNT(*) FROM cus_raw_customer_service_history;
SELECT MAX(source_id) FROM cus_raw_customer_service;
SELECT MAX(source_id) FROM cus_raw_customer_service_history;
SELECT * FROM cus_raw_customer_service WHERE voucher_no = 'BX202605120042';
SELECT * FROM cus_raw_customer_service_history WHERE customer_service_id = 164915 ORDER BY create_date;
