package com.example.smartinspection.service;

import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.repository.MysqlCustomerServiceWriter;
import com.example.smartinspection.repository.SqlServerCustomerServiceReader;
import com.example.smartinspection.repository.SyncCheckpointRepository;
import com.example.smartinspection.repository.SyncTaskLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceSyncService.class);

    private final SqlServerCustomerServiceReader reader;
    private final MysqlCustomerServiceWriter writer;
    private final SyncTaskLogRepository log;
    private final SyncCheckpointRepository checkpointRepository;

    @Value("${sync.batch-size.customer-service:3000}")
    private int batchSize;

    @Value("${sync.incremental.customer-service.create-time-lookback-days:14}")
    private int createTimeLookbackDays;

    public CustomerServiceSyncService(SqlServerCustomerServiceReader reader,
                                      MysqlCustomerServiceWriter writer,
                                      SyncTaskLogRepository log,
                                      SyncCheckpointRepository checkpointRepository) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
        this.checkpointRepository = checkpointRepository;
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

    /**
     * 增量方案：仅按 CustomerService.Id 高水位同步新增工单。
     * 已同步工单即使源端发生状态、评价、投诉等后续变化，也不做回刷或覆盖。
     */
    public void syncIncremental() {
        String task = "sync_customer_service_incremental";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        try {
            int last = checkpointRepository.getOrDefault(task, "CustomerService", "ID").getLastId();
            LOGGER.info("Start incremental sync task={}, batchNo={}, strategy=ID_WATERMARK_ONLY, lastId={}",
                    task, batchNo, last);
            while (true) {
                List<CustomerServiceRow> rows = reader.readByIdGreaterThan(last, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                checkpointRepository.save(task, "CustomerService", "ID", last, null);
            }
            log.finishSuccess(logId, read, write);
            LOGGER.info("Finish incremental sync task={}, batchNo={}, status=SUCCESS, read={}, write={}, finalLastId={}",
                    task, batchNo, read, write, last);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            LOGGER.error("Finish incremental sync task={}, batchNo={}, status=FAILED, read={}, write={}",
                    task, batchNo, read, write, e);
            throw e;
        }
    }
}
