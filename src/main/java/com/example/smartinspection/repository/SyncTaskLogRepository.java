package com.example.smartinspection.repository;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SyncTaskLogRepository {
    private final JdbcTemplate mysqlJdbcTemplate;
    public SyncTaskLogRepository(JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }
    public long start(String taskCode,String batchNo,String syncType){
        mysqlJdbcTemplate.update("INSERT INTO ic_sync_task_log(task_code,sync_batch_no,sync_type,start_time,status,read_count,write_count) VALUES(?,?,?,?, 'RUNNING',0,0)",taskCode,batchNo,syncType,java.sql.Timestamp.valueOf(LocalDateTime.now()));
        return mysqlJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
    public void finishSuccess(long id,int read,int write){ mysqlJdbcTemplate.update("UPDATE ic_sync_task_log SET end_time=?,status='SUCCESS',read_count=?,write_count=? WHERE id=?",java.sql.Timestamp.valueOf(LocalDateTime.now()),read,write,id);}    
    public void finishFail(long id,int read,int write,String err){ mysqlJdbcTemplate.update("UPDATE ic_sync_task_log SET end_time=?,status='FAILED',read_count=?,write_count=?,error_message=? WHERE id=?",java.sql.Timestamp.valueOf(LocalDateTime.now()),read,write,err,id);}    
}
