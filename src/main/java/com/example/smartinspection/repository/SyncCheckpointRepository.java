package com.example.smartinspection.repository;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SyncCheckpointRepository {
    private final JdbcTemplate mysqlJdbcTemplate;
    public SyncCheckpointRepository(JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }
    public int getLastMaxId(String taskCode) {
        Integer val = mysqlJdbcTemplate.query("SELECT last_max_id FROM ic_sync_task_checkpoint WHERE task_code=?", rs -> rs.next()?rs.getInt(1):0, taskCode);
        return val == null ? 0 : val;
    }
    public void saveOrUpdate(String taskCode, int lastMaxId) {
        mysqlJdbcTemplate.update("INSERT INTO ic_sync_task_checkpoint(task_code,last_max_id,last_sync_time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE last_max_id=VALUES(last_max_id),last_sync_time=VALUES(last_sync_time)", taskCode, lastMaxId, java.sql.Timestamp.valueOf(LocalDateTime.now()));
    }
}
