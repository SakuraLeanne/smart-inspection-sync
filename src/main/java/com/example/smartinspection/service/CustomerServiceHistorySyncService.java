package com.example.smartinspection.service;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import com.example.smartinspection.repository.MysqlCustomerServiceHistoryWriter;
import com.example.smartinspection.repository.SqlServerCustomerServiceHistoryReader;
import com.example.smartinspection.repository.SyncCheckpointRepository;
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
    private final SyncCheckpointRepository checkpointRepository;

    @Value("${sync.batch-size.customer-service-history:5000}")
    private int batchSize;

    public CustomerServiceHistorySyncService(SqlServerCustomerServiceHistoryReader reader,
                                             MysqlCustomerServiceHistoryWriter writer,
                                             SyncTaskLogRepository log,
                                             SyncCheckpointRepository checkpointRepository) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
        this.checkpointRepository = checkpointRepository;
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

    /**
     * 增量方案：CustomerServiceHistory 是工单历史/流水表，按业务语义采用 Id 高水位。
     * 历史表默认只追加不修改；若后续确认存在历史记录修改，可再增加 CreateDate 小窗口回溯。
     */
    public void syncIncremental() {
        String task = "sync_customer_service_history_incremental";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        int last = checkpointRepository.getOrDefault(task, "CustomerServiceHistory", "ID").getLastId();
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
                checkpointRepository.save(task, "CustomerServiceHistory", "ID", last, null);
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }
}
