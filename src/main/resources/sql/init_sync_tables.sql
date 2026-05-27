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


DROP TABLE IF EXISTS `cus_raw_organization_item`;

CREATE TABLE `cus_raw_organization_item` (
  `source_id` int NOT NULL COMMENT 'SQL Server OrganizationItem.Id',
  `parent_id` int DEFAULT NULL COMMENT '父级ID',
  `name` varchar(255) DEFAULT NULL COMMENT '名称，对应OrganizationItem.Name',
  `code` varchar(255) DEFAULT NULL COMMENT '编码',
  `item_type` int NOT NULL COMMENT '组织项类型',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `tel` varchar(255) DEFAULT NULL COMMENT '电话',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `region_id` int DEFAULT NULL COMMENT '所属区域ID，对应OrganizationItem.RegionId',
  `building_id` int DEFAULT NULL COMMENT '楼栋ID',
  `company_id` int DEFAULT NULL COMMENT '公司ID',
  `group_id` int DEFAULT NULL COMMENT '组团ID',
  `unit_id` int DEFAULT NULL COMMENT '单元ID',
  `city_id` int DEFAULT NULL COMMENT '城市ID',
  `building_type` int DEFAULT NULL COMMENT '楼栋类型',
  `floors` int DEFAULT NULL COMMENT '楼层数',
  `floor` int DEFAULT NULL COMMENT '楼层',
  `purpose` int DEFAULT NULL COMMENT '用途',
  `house_status` int DEFAULT NULL COMMENT '房屋状态',
  `house_state` int DEFAULT NULL COMMENT '房屋状态2',
  `house_type_id` int DEFAULT NULL COMMENT '房屋类型ID',
  `discriminator` varchar(255) NOT NULL COMMENT '实体类型标识',
  `hierarchical_path` text COMMENT '层级路径',
  `contact` text COMMENT '联系人',
  `contact_phone` text COMMENT '联系电话',
  `phone` text COMMENT '电话',
  `email` text COMMENT '邮箱',
  `is_virtual` tinyint(1) DEFAULT 0 COMMENT '是否虚拟',
  `is_disabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁用',
  `is_rentable` tinyint(1) DEFAULT NULL COMMENT '是否可租',
  `is_rented` tinyint(1) DEFAULT NULL COMMENT '是否已租',
  `is_sold` tinyint(1) DEFAULT NULL COMMENT '是否已售',
  `is_lease_out` tinyint(1) DEFAULT NULL COMMENT '是否租出',
  `uuid` text COMMENT 'UUID',
  `number` text COMMENT '编号',
  `terminal_code` text COMMENT '终端编码',
  `brand_code` text COMMENT '品牌编码',
  `check_in_date` datetime DEFAULT NULL COMMENT '入住日期',
  `handover_date` datetime DEFAULT NULL COMMENT '交付日期',
  `sold_time` datetime DEFAULT NULL COMMENT '出售时间',
  `update_time` datetime DEFAULT NULL COMMENT '源系统更新时间',
  `sync_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
  PRIMARY KEY (`source_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_region_id` (`region_id`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_unit_id` (`unit_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_name` (`name`),
  KEY `idx_is_disabled` (`is_disabled`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业系统组织项目位置原始同步表';

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
