-- ============================================================
-- 文件: 08_calculate_cus_charge_risk_model.sql
-- 目标: 兼容多种 raw 字段命名，稳定产出标准层与模型层
-- 本版增强: 解决“标准表0行”问题，扩大金额字段候选并兼容负数减免
-- ============================================================
SET @model_batch_no = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s');
SET @schema_name = DATABASE();

SET @c_id = (SELECT CASE
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='id') THEN 'r.id'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='source_id') THEN 'r.source_id'
  ELSE '0' END);

SET @c_project_id = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='project_id') THEN 'r.project_id' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='region_id') THEN 'r.region_id' ELSE 'NULL' END);
SET @c_project_name = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='project_name') THEN 'r.project_name' ELSE 'NULL' END);
SET @c_customer_id = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='customer_id') THEN 'r.customer_id' ELSE 'NULL' END);
SET @c_customer_name = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='customer_name') THEN 'r.customer_name' ELSE 'NULL' END);
SET @c_room_id = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='room_id') THEN 'r.room_id' ELSE 'NULL' END);
SET @c_room_name = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='room_name') THEN 'r.room_name' ELSE 'NULL' END);
SET @c_bill_no = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='bill_no') THEN 'r.bill_no' ELSE 'NULL' END);
SET @c_fee_item_name = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='fee_item_name') THEN 'r.fee_item_name' ELSE 'NULL' END);
SET @c_bill_date = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='bill_date') THEN 'r.bill_date' ELSE 'NULL' END);
SET @c_due_date = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='due_date') THEN 'r.due_date' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='should_pay_date') THEN 'r.should_pay_date' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='charge_date') THEN 'r.charge_date' ELSE 'NULL' END);
SET @c_charge_date = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='charge_date') THEN 'r.charge_date' ELSE 'NULL' END);
SET @c_sync_time = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='sync_time') THEN 'r.sync_time' ELSE 'NOW()' END);

-- 欠费金额候选（优先直接欠费字段，其次应收-实收，再次未收/欠费别名）
SET @c_arrear_direct = (SELECT CASE
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_principal') THEN 'r.arrear_principal'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_amount') THEN 'r.arrear_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='owing_amount') THEN 'r.owing_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='unpaid_amount') THEN 'r.unpaid_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='debt_amount') THEN 'r.debt_amount'
  ELSE 'NULL' END);
SET @c_receivable = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_principal') THEN 'r.receivable_principal' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_amount') THEN 'r.receivable_amount' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='should_amount') THEN 'r.should_amount' ELSE '0' END);
SET @c_received = (SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='received_principal') THEN 'r.received_principal' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='received_amount') THEN 'r.received_amount' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='pay_amount') THEN 'r.pay_amount' WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='paid_amount') THEN 'r.paid_amount' ELSE '0' END);
SET @arrear_expr = CONCAT('GREATEST(COALESCE(',@c_arrear_direct,', IFNULL(',@c_receivable,',0)-IFNULL(',@c_received,',0), 0),0)');

-- 减免字段候选（兼容负数入账，使用 ABS）
SET @c_reduction = (SELECT CASE
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='reduction_amount') THEN 'r.reduction_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='discount_amount') THEN 'r.discount_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='derate_amount') THEN 'r.derate_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='waiver_amount') THEN 'r.waiver_amount'
  WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='minus_amount') THEN 'r.minus_amount'
  ELSE '0' END);
SET @reduction_expr = CONCAT('ABS(IFNULL(',@c_reduction,',0))');

SET @sql_std_arrear = CONCAT(
"INSERT INTO cus_std_arrear_bill (source_system,source_detail_id,project_id,project_name,customer_id,customer_name,room_id,room_name,bill_no,fee_item_name,bill_date,due_date,arrear_principal,raw_sync_time) ",
"SELECT 'property',",@c_id,",",@c_project_id,",",@c_project_name,",",@c_customer_id,",",@c_customer_name,",",@c_room_id,",",@c_room_name,",",@c_bill_no,",",@c_fee_item_name,",DATE(",@c_bill_date,"),DATE(",@c_due_date,"),",@arrear_expr,",",@c_sync_time," ",
"FROM cus_raw_charge_customerchargedetail r WHERE ",@arrear_expr," > 0 ",
"ON DUPLICATE KEY UPDATE project_id=VALUES(project_id),project_name=VALUES(project_name),customer_id=VALUES(customer_id),customer_name=VALUES(customer_name),room_id=VALUES(room_id),room_name=VALUES(room_name),bill_no=VALUES(bill_no),fee_item_name=VALUES(fee_item_name),bill_date=VALUES(bill_date),due_date=VALUES(due_date),arrear_principal=VALUES(arrear_principal),raw_sync_time=VALUES(raw_sync_time),std_update_time=CURRENT_TIMESTAMP"
);
PREPARE stmt1 FROM @sql_std_arrear; EXECUTE stmt1; DEALLOCATE PREPARE stmt1;

SET @sql_std_reduction = CONCAT(
"INSERT INTO cus_std_fee_reduction (source_system,source_detail_id,stat_year,project_id,project_name,room_id,room_name,fee_item_name,reduction_amount,raw_sync_time) ",
"SELECT 'property',",@c_id,",YEAR(COALESCE(",@c_bill_date,",",@c_charge_date,",NOW())),",@c_project_id,",",@c_project_name,",",@c_room_id,",",@c_room_name,",",@c_fee_item_name,",@reduction_expr,",",@c_sync_time," FROM cus_raw_charge_customerchargedetail r ",
"WHERE ",@reduction_expr," > 0 ",
"ON DUPLICATE KEY UPDATE stat_year=VALUES(stat_year),project_id=VALUES(project_id),project_name=VALUES(project_name),room_id=VALUES(room_id),room_name=VALUES(room_name),fee_item_name=VALUES(fee_item_name),reduction_amount=VALUES(reduction_amount),raw_sync_time=VALUES(raw_sync_time),std_update_time=CURRENT_TIMESTAMP"
);
PREPARE stmt2 FROM @sql_std_reduction; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

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
