-- ============================================================
-- 文件: 08_calculate_cus_charge_risk_model.sql
-- 目标: 基于 MySQL8 从 cus_raw_charge_customerchargedetail 生成标准层与模型层结果
-- 特性:
--   1) 可重复执行（标准层使用 INSERT ... ON DUPLICATE KEY UPDATE）
--   2) 模型结果按批次号 model_batch_no 入库
--   3) 覆盖两个场景：
--      - 大额欠费财务风险预警
--      - 物业费用减免合规性探查
-- ============================================================

-- 统一批次号（格式：yyyyMMddHHmmss）
SET @model_batch_no = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s');

-- ============================================================
-- Step-1: 构建标准表 cus_std_arrear_bill（欠费账单）
-- 口径说明:
--   欠费本金 = max(应收本金 - 实收本金, 0)
--   仅保留欠费本金 > 0 的明细
-- 幂等说明:
--   以 (source_system, source_detail_id) 唯一键冲突时执行更新
-- ============================================================
INSERT INTO cus_std_arrear_bill (
  source_system,source_detail_id,project_id,project_name,customer_id,customer_name,room_id,room_name,bill_no,fee_item_name,bill_date,due_date,arrear_principal,raw_sync_time
)
SELECT
  'property',r.id,r.project_id,r.project_name,r.customer_id,r.customer_name,r.room_id,r.room_name,r.bill_no,r.fee_item_name,
  DATE(r.bill_date),DATE(r.due_date),GREATEST(IFNULL(r.receivable_principal,0)-IFNULL(r.received_principal,0),0),r.sync_time
FROM cus_raw_charge_customerchargedetail r
WHERE GREATEST(IFNULL(r.receivable_principal,0)-IFNULL(r.received_principal,0),0) > 0
ON DUPLICATE KEY UPDATE
project_id=VALUES(project_id),project_name=VALUES(project_name),customer_id=VALUES(customer_id),customer_name=VALUES(customer_name),
room_id=VALUES(room_id),room_name=VALUES(room_name),bill_no=VALUES(bill_no),fee_item_name=VALUES(fee_item_name),bill_date=VALUES(bill_date),due_date=VALUES(due_date),
arrear_principal=VALUES(arrear_principal),raw_sync_time=VALUES(raw_sync_time),std_update_time=CURRENT_TIMESTAMP;

-- ============================================================
-- Step-2: 构建标准表 cus_std_fee_reduction（费用减免）
-- 口径说明:
--   统计年度 stat_year = YEAR(COALESCE(bill_date, charge_date, NOW()))
--   仅保留 reduction_amount > 0 的明细
-- 幂等说明:
--   以 (source_system, source_detail_id) 唯一键冲突时执行更新
-- ============================================================
INSERT INTO cus_std_fee_reduction (
  source_system,source_detail_id,stat_year,project_id,project_name,room_id,room_name,fee_item_name,reduction_amount,raw_sync_time
)
SELECT
  'property',r.id,YEAR(COALESCE(r.bill_date,r.charge_date,NOW())),r.project_id,r.project_name,r.room_id,r.room_name,r.fee_item_name,IFNULL(r.reduction_amount,0),r.sync_time
FROM cus_raw_charge_customerchargedetail r
WHERE IFNULL(r.reduction_amount,0) > 0
ON DUPLICATE KEY UPDATE
stat_year=VALUES(stat_year),project_id=VALUES(project_id),project_name=VALUES(project_name),room_id=VALUES(room_id),room_name=VALUES(room_name),
fee_item_name=VALUES(fee_item_name),reduction_amount=VALUES(reduction_amount),raw_sync_time=VALUES(raw_sync_time),std_update_time=CURRENT_TIMESTAMP;

-- ============================================================
-- Step-3: 生成大额欠费财务风险预警结果 cus_model_large_arrear_result
-- 聚合粒度: 项目 + 客户 + 房间
-- 规则:
--   金额等级:
--     >= 100000 -> 一级
--     >= 70000  -> 二级
--     >= 50000  -> 三级
--   账龄等级（最早 due_date 到今天）：
--     >= 730天  -> 一级（约2年）
--     >= 547天  -> 二级（约1.5年）
--     >= 365天  -> 三级（约1年）
--   最终等级: 取金额等级和账龄等级中的最高等级
-- 入模条件:
--   金额达到三级阈值 或 账龄达到三级阈值
-- ============================================================
INSERT INTO cus_model_large_arrear_result (
  model_batch_no,project_id,project_name,customer_id,customer_name,room_id,room_name,arrear_principal_total,earliest_due_date,arrear_age_days,amount_level,age_level,warning_level,warning_title,warning_content
)
SELECT
  @model_batch_no,t.project_id,MAX(t.project_name),t.customer_id,MAX(t.customer_name),t.room_id,MAX(t.room_name),
  SUM(t.arrear_principal),MIN(t.due_date),TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE()),
  CASE WHEN SUM(t.arrear_principal)>=100000 THEN '一级预警' WHEN SUM(t.arrear_principal)>=70000 THEN '二级预警' WHEN SUM(t.arrear_principal)>=50000 THEN '三级预警' ELSE NULL END,
  CASE WHEN TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=730 THEN '一级预警' WHEN TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=547 THEN '二级预警' WHEN TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=365 THEN '三级预警' ELSE NULL END,
  CASE WHEN (SUM(t.arrear_principal)>=100000 OR TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=730) THEN '一级预警'
       WHEN (SUM(t.arrear_principal)>=70000 OR TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=547) THEN '二级预警'
       WHEN (SUM(t.arrear_principal)>=50000 OR TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=365) THEN '三级预警' END,
  CONCAT('大额欠费预警-',IFNULL(MAX(t.customer_name),'未知客户')),
  CONCAT('项目=',IFNULL(MAX(t.project_name),'未知项目'),'，房间=',IFNULL(MAX(t.room_name),'未知房间'),'，累计欠费本金=',FORMAT(SUM(t.arrear_principal),2),'，最早欠费日=',DATE_FORMAT(MIN(t.due_date),'%Y-%m-%d'))
FROM cus_std_arrear_bill t
GROUP BY t.project_id,t.customer_id,t.room_id
HAVING SUM(t.arrear_principal)>=50000 OR TIMESTAMPDIFF(DAY,MIN(t.due_date),CURDATE())>=365;

-- ============================================================
-- Step-4: 生成费用减免合规性探查结果 cus_model_fee_reduction_result
-- 聚合粒度: 年度 + 项目 + 房间
-- 排名方式: ROW_NUMBER() OVER(PARTITION BY stat_year, project_id ORDER BY reduction_amount_total DESC, room_id)
-- 预警等级:
--   TOP1-TOP3   -> 一级预警
--   TOP4-TOP5   -> 二级预警
--   TOP6-TOP10  -> 三级预警
-- ============================================================
INSERT INTO cus_model_fee_reduction_result (
  model_batch_no,stat_year,project_id,project_name,room_id,room_name,reduction_amount_total,project_year_rank,warning_level,warning_title,warning_content
)
SELECT
  @model_batch_no,x.stat_year,x.project_id,x.project_name,x.room_id,x.room_name,x.reduction_amount_total,x.project_year_rank,
  CASE WHEN x.project_year_rank<=3 THEN '一级预警' WHEN x.project_year_rank<=5 THEN '二级预警' WHEN x.project_year_rank<=10 THEN '三级预警' END,
  CONCAT('费用减免预警-',x.stat_year,'-',IFNULL(x.project_name,'未知项目')),
  CONCAT('年度=',x.stat_year,'，项目=',IFNULL(x.project_name,'未知项目'),'，房间=',IFNULL(x.room_name,'未知房间'),'，累计减免=',FORMAT(x.reduction_amount_total,2),'，项目内排名=',x.project_year_rank)
FROM (
  SELECT t.*,ROW_NUMBER() OVER(PARTITION BY t.stat_year,t.project_id ORDER BY t.reduction_amount_total DESC,t.room_id) project_year_rank
  FROM (
    SELECT stat_year,project_id,MAX(project_name) project_name,room_id,MAX(room_name) room_name,SUM(reduction_amount) reduction_amount_total
    FROM cus_std_fee_reduction
    GROUP BY stat_year,project_id,room_id
  ) t
) x
WHERE x.project_year_rank<=10;
