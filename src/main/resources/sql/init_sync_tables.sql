-- 目标表DDL（MySQL 8 / utf8mb4）
-- 按需求创建四张表
CREATE TABLE IF NOT EXISTS cus_raw_customer_service (
  source_id int NOT NULL,
  voucher_no varchar(255) DEFAULT NULL,
  region_id int DEFAULT NULL,
  service_source int NOT NULL,
  customer_name varchar(255) DEFAULT NULL,
  phone varchar(255) DEFAULT NULL,
  finish_time_required datetime DEFAULT NULL,
  accepted_date datetime DEFAULT NULL,
  details varchar(1500) DEFAULT NULL,
  service_type int NOT NULL,
  comment int DEFAULT NULL,
  comment_content varchar(500) DEFAULT NULL,
  complaint_status int DEFAULT NULL,
  confirm_time datetime DEFAULT NULL,
  sync_batch_no varchar(64) DEFAULT NULL,
  sync_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (source_id), KEY idx_ics_accepted_date (accepted_date), KEY idx_ics_region_id (region_id), KEY idx_ics_voucher_no (voucher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cus_raw_customer_service_history (
  source_id int NOT NULL,
  created_by_id varchar(450) DEFAULT NULL,
  create_date datetime NOT NULL,
  content varchar(255) NOT NULL,
  customer_service_id int NOT NULL,
  operation_status int NOT NULL DEFAULT 0,
  complaint_handle_id int DEFAULT NULL,
  dispatch_id int DEFAULT NULL,
  word_content longtext,
  sync_batch_no varchar(64) DEFAULT NULL,
  sync_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (source_id), KEY idx_icsh_customer_service_id (customer_service_id), KEY idx_icsh_create_date (create_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS cus_raw_organization_item (
  source_id int NOT NULL,
  name varchar(255) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  sync_batch_no varchar(64) DEFAULT NULL,
  sync_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (source_id),
  KEY idx_iro_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cus_sync_task_checkpoint (
  task_code varchar(100) NOT NULL,
  last_max_id int DEFAULT 0,
  last_sync_time datetime DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cus_sync_task_log (
  id bigint NOT NULL AUTO_INCREMENT,
  task_code varchar(100) NOT NULL,
  sync_batch_no varchar(64) NOT NULL,
  sync_type varchar(20) NOT NULL,
  start_time datetime NOT NULL,
  end_time datetime DEFAULT NULL,
  status varchar(20) NOT NULL,
  read_count int DEFAULT 0,
  write_count int DEFAULT 0,
  error_message text,
  PRIMARY KEY (id), KEY idx_task_code_time (task_code,start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SQL Server 2008 分页备用：ROW_NUMBER()
-- SELECT * FROM (
--   SELECT ROW_NUMBER() OVER(ORDER BY Id ASC) AS rn, *
--   FROM dbo.CustomerService
--   WHERE AcceptedDate >= DATEADD(day,-?,GETDATE())
-- ) t WHERE t.rn > ? AND t.rn <= ?;
