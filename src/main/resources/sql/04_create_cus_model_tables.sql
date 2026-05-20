CREATE TABLE IF NOT EXISTS `cus_model_repeat_repair_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_batch_no` varchar(64) NOT NULL COMMENT '模型批次号',
  `project_id` int DEFAULT NULL COMMENT '项目ID',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `location_key` varchar(255) NOT NULL COMMENT '位置分组Key',
  `location_name` varchar(255) DEFAULT NULL COMMENT '位置名称',
  `dispatch_type_name` varchar(255) DEFAULT NULL COMMENT '工单小类',
  `dispatch_type_parent_name` varchar(255) DEFAULT NULL COMMENT '工单大类',
  `content_keyword` varchar(100) DEFAULT NULL COMMENT '问题关键词',
  `stat_start_time` datetime NOT NULL COMMENT '统计开始时间',
  `stat_end_time` datetime NOT NULL COMMENT '统计结束时间',
  `repeat_count` int NOT NULL COMMENT '重复次数',
  `warning_level` varchar(20) NOT NULL COMMENT '预警等级：一级/二级/三级',
  `warning_title` varchar(255) DEFAULT NULL COMMENT '预警标题',
  `warning_content` text COMMENT '预警内容',
  `related_order_ids` text COMMENT '关联工单ID列表',
  `related_order_nos` text COMMENT '关联工单编号列表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_model_batch_no` (`model_batch_no`),
  KEY `idx_project_location` (`project_id`, `location_key`),
  KEY `idx_warning_level` (`warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重复维修与过度维修模型结果表';


-- ============================================================
-- 财务风险模型结果表
-- 1) cus_model_large_arrear_result: 大额欠费财务风险预警结果
-- 2) cus_model_fee_reduction_result: 物业费用减免合规探查结果
-- 3) cus_warning_order: 可选统一预警工单汇总表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cus_model_large_arrear_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_batch_no` varchar(64) NOT NULL COMMENT '模型批次号',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
  `room_id` bigint DEFAULT NULL COMMENT '房间ID',
  `room_name` varchar(255) DEFAULT NULL COMMENT '房间名称',
  `arrear_principal_total` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计欠费本金',
  `earliest_due_date` date DEFAULT NULL COMMENT '最早应缴日期',
  `arrear_age_days` int DEFAULT NULL COMMENT '最早欠费账龄天数',
  `amount_level` varchar(20) DEFAULT NULL COMMENT '金额等级',
  `age_level` varchar(20) DEFAULT NULL COMMENT '账龄等级',
  `warning_level` varchar(20) NOT NULL COMMENT '最终预警等级',
  `warning_title` varchar(255) DEFAULT NULL COMMENT '预警标题',
  `warning_content` text COMMENT '预警内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch` (`model_batch_no`),
  KEY `idx_project_room` (`project_id`, `room_id`),
  KEY `idx_warning_level` (`warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大额欠费财务风险预警结果表';

CREATE TABLE IF NOT EXISTS `cus_model_fee_reduction_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_batch_no` varchar(64) NOT NULL COMMENT '模型批次号',
  `stat_year` int NOT NULL COMMENT '统计年度',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `room_id` bigint DEFAULT NULL COMMENT '房间ID',
  `room_name` varchar(255) DEFAULT NULL COMMENT '房间名称',
  `reduction_amount_total` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计减免金额',
  `project_year_rank` int NOT NULL COMMENT '项目内年度排名',
  `warning_level` varchar(20) NOT NULL COMMENT '预警等级',
  `warning_title` varchar(255) DEFAULT NULL COMMENT '预警标题',
  `warning_content` text COMMENT '预警内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch` (`model_batch_no`),
  KEY `idx_year_project` (`stat_year`, `project_id`),
  KEY `idx_warning_level` (`warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业费用减免合规性探查结果表';

CREATE TABLE IF NOT EXISTS `cus_warning_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_batch_no` varchar(64) NOT NULL COMMENT '模型批次号',
  `warning_type` varchar(64) NOT NULL COMMENT '预警类型',
  `warning_level` varchar(20) NOT NULL COMMENT '预警等级',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `biz_key` varchar(255) DEFAULT NULL COMMENT '业务主键（如客户+房间/年度+房间）',
  `warning_title` varchar(255) DEFAULT NULL COMMENT '预警标题',
  `warning_content` text COMMENT '预警内容',
  `status` varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT '工单状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch` (`model_batch_no`),
  KEY `idx_type_level` (`warning_type`, `warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一预警工单表';

-- 与新口径对齐：补充结果扩展字段（幂等DDL）
ALTER TABLE `cus_model_large_arrear_result`
  ADD COLUMN IF NOT EXISTS `bill_count` int DEFAULT '0' COMMENT '欠费账单数量',
  ADD COLUMN IF NOT EXISTS `fee_item_names` text COMMENT '涉及费用项',
  ADD COLUMN IF NOT EXISTS `related_detail_ids` longtext COMMENT '关联来源明细ID';

ALTER TABLE `cus_model_fee_reduction_result`
  ADD COLUMN IF NOT EXISTS `bill_count` int DEFAULT '0' COMMENT '减免账单数量',
  ADD COLUMN IF NOT EXISTS `fee_item_names` text COMMENT '涉及费用项',
  ADD COLUMN IF NOT EXISTS `discount_user_names` text COMMENT '涉及减免操作人',
  ADD COLUMN IF NOT EXISTS `check_statuses` text COMMENT '涉及审核状态',
  ADD COLUMN IF NOT EXISTS `related_detail_ids` longtext COMMENT '关联来源明细ID';
