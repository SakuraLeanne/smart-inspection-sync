package com.example.smartinspection.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SyncAdminService {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;
    public SyncAdminService(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }

    public void resetIncrementCheckpoint() {
        mysqlJdbcTemplate.update("DELETE FROM cus_sync_task_checkpoint WHERE task_code IN (?,?)",
            "sync_customer_service_new", "sync_customer_service_history_new");
    }

    public void truncateRawTables() {
        mysqlJdbcTemplate.execute("TRUNCATE TABLE cus_raw_customer_service_history");
        mysqlJdbcTemplate.execute("TRUNCATE TABLE cus_raw_customer_service");
    }
}
