package com.example.smartinspection.service;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import com.example.smartinspection.repository.MysqlCustomerServiceHistoryWriter;
import com.example.smartinspection.repository.SqlServerCustomerServiceHistoryReader;
import com.example.smartinspection.repository.SyncTaskLogRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceHistorySyncService {
    private final SqlServerCustomerServiceHistoryReader reader;
    private final MysqlCustomerServiceHistoryWriter writer;
    private final SyncTaskLogRepository log;

    @Value("${sync.batch-size.customer-service-history:5000}")
    private int batchSize;

    public CustomerServiceHistorySyncService(SqlServerCustomerServiceHistoryReader reader,
                                             MysqlCustomerServiceHistoryWriter writer,
                                             SyncTaskLogRepository log) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
    }

    public void syncFull() {
        String task = "sync_customer_service_history_full";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<CustomerServiceHistoryRow> rows = reader.readByIdGreaterThan(last, batchSize);
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
