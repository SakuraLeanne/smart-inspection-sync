-- ============================================================
-- 文件: 09_validate_cus_charge_risk_precheck.sql
-- 目标: 在执行 08_calculate_cus_charge_risk_model.sql 前做字段命中与预计入数预检查
-- 用法: 先执行本脚本，确认映射命中与预计行数，再执行 08
-- ============================================================

SET @schema_name = DATABASE();

-- 1) 展示 raw 表当前字段
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = @schema_name
  AND table_name = 'cus_raw_charge_customerchargedetail'
ORDER BY ordinal_position;

-- 2) 字段映射命中检查（与08脚本保持一致）
SELECT
  CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='id') THEN 'id'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='source_id') THEN 'source_id'
    ELSE 'NOT_FOUND' END AS source_detail_id_field,
  CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='project_id') THEN 'project_id'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='region_id') THEN 'region_id'
    ELSE 'NOT_FOUND' END AS project_id_field,
  CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_principal') THEN 'arrear_principal'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_amount') THEN 'arrear_amount'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='owing_amount') THEN 'owing_amount'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_principal') THEN 'receivable_principal - received*'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_amount') THEN 'receivable_amount - received*'
    ELSE 'NOT_FOUND' END AS arrear_logic_field,
  CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='reduction_amount') THEN 'reduction_amount'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='discount_amount') THEN 'discount_amount'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='derate_amount') THEN 'derate_amount'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='waiver_amount') THEN 'waiver_amount'
    ELSE 'NOT_FOUND' END AS reduction_field;

-- 3) 预计入数（欠费）
SET @arrear_expr = '0';
SET @arrear_expr = (
  SELECT CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_principal') THEN 'GREATEST(IFNULL(r.arrear_principal,0),0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='arrear_amount') THEN 'GREATEST(IFNULL(r.arrear_amount,0),0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='owing_amount') THEN 'GREATEST(IFNULL(r.owing_amount,0),0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_principal') THEN 'GREATEST(IFNULL(r.receivable_principal,0)-IFNULL(r.received_principal,0),0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='receivable_amount') THEN 'GREATEST(IFNULL(r.receivable_amount,0)-IFNULL(r.received_amount,0),0)'
    ELSE '0' END
);
SET @sql_arrear_cnt = CONCAT('SELECT COUNT(*) AS expected_arrear_rows FROM cus_raw_charge_customerchargedetail r WHERE ', @arrear_expr, ' > 0');
PREPARE stmt_a FROM @sql_arrear_cnt; EXECUTE stmt_a; DEALLOCATE PREPARE stmt_a;

-- 4) 预计入数（减免）
SET @reduction_expr = (
  SELECT CASE
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='reduction_amount') THEN 'IFNULL(r.reduction_amount,0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='discount_amount') THEN 'IFNULL(r.discount_amount,0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='derate_amount') THEN 'IFNULL(r.derate_amount,0)'
    WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='cus_raw_charge_customerchargedetail' AND column_name='waiver_amount') THEN 'IFNULL(r.waiver_amount,0)'
    ELSE '0' END
);
SET @sql_reduction_cnt = CONCAT('SELECT COUNT(*) AS expected_reduction_rows FROM cus_raw_charge_customerchargedetail r WHERE ', @reduction_expr, ' > 0');
PREPARE stmt_r FROM @sql_reduction_cnt; EXECUTE stmt_r; DEALLOCATE PREPARE stmt_r;

-- 5) 执行后可对比（执行08后再跑）
SELECT COUNT(*) AS std_arrear_rows FROM cus_std_arrear_bill;
SELECT COUNT(*) AS std_reduction_rows FROM cus_std_fee_reduction;
