package com.example.smartinspection.service;

import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.repository.MysqlCustomerServiceWriter;
import com.example.smartinspection.repository.SqlServerCustomerServiceReader;
import com.example.smartinspection.repository.SyncTaskLogRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceSyncService {
    private final SqlServerCustomerServiceReader reader;
    private final MysqlCustomerServiceWriter writer;
    private final SyncTaskLogRepository log;

    @Value("${sync.batch-size.customer-service:3000}")
    private int batchSize;

    public CustomerServiceSyncService(SqlServerCustomerServiceReader reader,
                                      MysqlCustomerServiceWriter writer,
                                      SyncTaskLogRepository log) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
    }

    public void syncFull() {
        String task = "sync_customer_service_full";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<CustomerServiceRow> rows = reader.readByIdGreaterThan(last, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }
}
