-- ============================================================
-- 文件: 05_calculate_repeat_repair_model.sql
-- 目标: 基于标准工单表计算重复维修/过度维修预警模型
-- 前置条件:
--   1) cus_std_work_order_history 已先构建
--   2) cus_std_work_order 已先构建
--   3) MySQL 8+
-- ============================================================

-- 放宽 GROUP_CONCAT 长度，避免关联工单列表被截断
SET SESSION group_concat_max_len = 10240;

-- 生成模型批次号（yyyyMMddHHmmss）
SET @model_batch_no = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s');

-- 结果入库：同一批次只插入一次
INSERT INTO cus_model_repeat_repair_result (
  model_batch_no, project_id, project_name, location_key, location_name,
  dispatch_type_name, dispatch_type_parent_name, content_keyword,
  stat_start_time, stat_end_time, repeat_count, warning_level,
  warning_title, warning_content, related_order_ids, related_order_nos
)
SELECT
  @model_batch_no,
  t.project_id,
  MAX(t.project_name) AS project_name,
  t.location_key,
  MAX(t.location_name) AS location_name,
  t.dispatch_type_name,
  t.dispatch_type_parent_name,
  t.content_keyword,
  DATE_SUB(NOW(), INTERVAL 90 DAY),
  NOW(),
  COUNT(*) AS repeat_count,
  -- 预警分级：10+一级，7-9二级，3-6三级
  CASE WHEN COUNT(*) >= 10 THEN '一级预警' WHEN COUNT(*) >= 7 THEN '二级预警' ELSE '三级预警' END,
  CONCAT('重复维修预警-', IFNULL(t.content_keyword, '未知')),
  CONCAT('近90天内重复次数=', COUNT(*), '，位置=', t.location_key),
  GROUP_CONCAT(CAST(t.source_order_id AS CHAR) ORDER BY t.report_time DESC SEPARATOR ','),
  GROUP_CONCAT(IFNULL(t.order_no, '') ORDER BY t.report_time DESC SEPARATOR ',')
FROM (
  SELECT w.*, COALESCE(NULLIF(TRIM(w.location_name), ''),
                       CASE WHEN w.location_id IS NOT NULL THEN CONCAT('LOC-', w.location_id) END,
                       NULLIF(TRIM(w.location_text), ''), '未知位置') AS location_key
  FROM cus_std_work_order w
  WHERE w.is_repair_order = 1
    AND w.is_valid = 1
    AND w.is_finished = 1
    AND w.report_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)
    AND w.content_keyword IS NOT NULL
    AND w.content_keyword <> ''
    AND w.content_keyword <> '其他'
) t
-- 分组口径：项目 + 位置 + 工单大小类 + 关键词
GROUP BY t.project_id, t.project_name, t.location_key, t.dispatch_type_parent_name, t.dispatch_type_name, t.content_keyword
-- 入模阈值：重复次数至少3次
HAVING COUNT(*) >= 3;
