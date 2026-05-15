-- 目的：将当前精简版 cus_raw_customer_service 升级到标准化/模型推荐字段集
-- 说明：使用 IF NOT EXISTS 方式不可用于 MySQL 5.7 的 ADD COLUMN，因此采用“可重复执行”的信息架构检查+动态 SQL。

SET @db_name = DATABASE();

-- helper: add column when missing
SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN organization_item_id int DEFAULT NULL COMMENT ''报修地点ID''',
    'SELECT ''organization_item_id exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='organization_item_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN place varchar(255) DEFAULT NULL COMMENT ''区域/报修位置描述''',
    'SELECT ''place exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='place'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN address text COMMENT ''详细地址''',
    'SELECT ''address exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='address'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN customer_id int DEFAULT NULL COMMENT ''客户ID''',
    'SELECT ''customer_id exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='customer_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN appointment_time datetime DEFAULT NULL COMMENT ''预约时间''',
    'SELECT ''appointment_time exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='appointment_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN dispatch_time datetime DEFAULT NULL COMMENT ''派工时间''',
    'SELECT ''dispatch_time exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='dispatch_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN start_process_time datetime DEFAULT NULL COMMENT ''开始处理时间''',
    'SELECT ''start_process_time exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='start_process_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN dispatch_finish_time datetime DEFAULT NULL COMMENT ''派工完成时间''',
    'SELECT ''dispatch_finish_time exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='dispatch_finish_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN dispatch_type_id int DEFAULT NULL COMMENT ''工单类型ID''',
    'SELECT ''dispatch_type_id exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='dispatch_type_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN dispatch_priority_id int DEFAULT NULL COMMENT ''工单优先级ID''',
    'SELECT ''dispatch_priority_id exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='dispatch_priority_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN repair_status int DEFAULT NULL COMMENT ''报修状态''',
    'SELECT ''repair_status exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='repair_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN comment_date datetime DEFAULT NULL COMMENT ''评价时间''',
    'SELECT ''comment_date exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='comment_date'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD COLUMN comment_by_customer tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否客户评价''',
    'SELECT ''comment_by_customer exists''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND COLUMN_NAME='comment_by_customer'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 可选索引（若不存在）
SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD KEY idx_crcs_org_item_id (organization_item_id)',
    'SELECT ''idx_crcs_org_item_id exists''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND INDEX_NAME='idx_crcs_org_item_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD KEY idx_crcs_dispatch_type_id (dispatch_type_id)',
    'SELECT ''idx_crcs_dispatch_type_id exists''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND INDEX_NAME='idx_crcs_dispatch_type_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*)=0,
    'ALTER TABLE cus_raw_customer_service ADD KEY idx_crcs_repair_status (repair_status)',
    'SELECT ''idx_crcs_repair_status exists''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='cus_raw_customer_service' AND INDEX_NAME='idx_crcs_repair_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 验证
SELECT COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'cus_raw_customer_service'
  AND COLUMN_NAME IN (
    'organization_item_id','place','address','customer_id','appointment_time','dispatch_time',
    'start_process_time','dispatch_finish_time','dispatch_type_id','dispatch_priority_id','repair_status',
    'comment_date','comment_by_customer'
  )
ORDER BY COLUMN_NAME;
