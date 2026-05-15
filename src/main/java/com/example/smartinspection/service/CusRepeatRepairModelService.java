package com.example.smartinspection.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CusRepeatRepairModelService {
    private final JdbcTemplate mysqlJdbcTemplate;

    public CusRepeatRepairModelService(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    public void calculateRepeatRepairModel() {
        mysqlJdbcTemplate.execute("SET SESSION group_concat_max_len = 10240");
        String batchNo = mysqlJdbcTemplate.queryForObject("SELECT DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')", String.class);
        mysqlJdbcTemplate.update("DELETE FROM cus_model_repeat_repair_result WHERE model_batch_no = ?", batchNo);
        mysqlJdbcTemplate.update("INSERT INTO cus_model_repeat_repair_result (model_batch_no,project_id,project_name,location_key,location_name,dispatch_type_name,dispatch_type_parent_name,content_keyword,stat_start_time,stat_end_time,repeat_count,warning_level,warning_title,warning_content,related_order_ids,related_order_nos) " +
            "SELECT ?,t.project_id,t.project_name,t.location_key,t.location_name,t.dispatch_type_name,t.dispatch_type_parent_name,t.content_keyword,DATE_SUB(NOW(),INTERVAL 90 DAY),NOW(),COUNT(*) repeat_count," +
            "CASE WHEN COUNT(*)>=10 THEN '一级预警' WHEN COUNT(*)>=7 THEN '二级预警' ELSE '三级预警' END," +
            "CONCAT('重复维修预警-',IFNULL(t.content_keyword,'未知')),CONCAT('近90天内重复次数=',COUNT(*),'，位置=',t.location_key)," +
            "GROUP_CONCAT(CAST(t.source_order_id AS CHAR) ORDER BY t.report_time DESC SEPARATOR ','),GROUP_CONCAT(IFNULL(t.order_no,'') ORDER BY t.report_time DESC SEPARATOR ',') " +
            "FROM (SELECT w.*,COALESCE(NULLIF(TRIM(w.location_name),''),CASE WHEN w.location_id IS NOT NULL THEN CONCAT('LOC-',w.location_id) END,NULLIF(TRIM(w.location_text),''),'未知位置') location_key FROM cus_std_work_order w WHERE w.is_repair_order=1 AND w.is_valid=1 AND w.is_finished=1 AND w.report_time>=DATE_SUB(NOW(),INTERVAL 90 DAY) AND w.content_keyword IS NOT NULL AND w.content_keyword<>'' AND w.content_keyword<>'其他') t " +
            "GROUP BY t.project_id,t.location_key,t.dispatch_type_parent_name,t.dispatch_type_name,t.content_keyword HAVING COUNT(*)>=3", batchNo);
    }
}
