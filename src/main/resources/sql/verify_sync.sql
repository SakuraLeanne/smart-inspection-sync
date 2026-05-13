SELECT COUNT(*) FROM ic_raw_customer_service;
SELECT COUNT(*) FROM ic_raw_customer_service_history;
SELECT MAX(source_id) FROM ic_raw_customer_service;
SELECT MAX(source_id) FROM ic_raw_customer_service_history;
SELECT * FROM ic_raw_customer_service WHERE voucher_no = 'BX202605120042';
SELECT * FROM ic_raw_customer_service_history WHERE customer_service_id = 164915 ORDER BY create_date;
