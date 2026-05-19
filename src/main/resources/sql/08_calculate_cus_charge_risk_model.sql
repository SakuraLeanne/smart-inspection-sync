SET @model_batch_no = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s');

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
