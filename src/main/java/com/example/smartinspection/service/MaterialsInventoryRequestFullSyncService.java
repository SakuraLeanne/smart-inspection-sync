package com.example.smartinspection.service;

import com.example.smartinspection.domain.MaterialsInventoryRequestDetailRow;
import com.example.smartinspection.domain.MaterialsInventoryRequestRow;
import com.example.smartinspection.repository.MysqlMaterialsInventoryRequestDetailWriter;
import com.example.smartinspection.repository.MysqlMaterialsInventoryRequestWriter;
import com.example.smartinspection.repository.SqlServerMaterialsInventoryRequestDetailReader;
import com.example.smartinspection.repository.SqlServerMaterialsInventoryRequestReader;
import com.example.smartinspection.repository.SyncCheckpointRepository;
import com.example.smartinspection.repository.SyncTaskLogRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MaterialsInventoryRequestFullSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialsInventoryRequestFullSyncService.class);

    private final SqlServerMaterialsInventoryRequestReader requestReader;
    private final MysqlMaterialsInventoryRequestWriter requestWriter;
    private final SqlServerMaterialsInventoryRequestDetailReader detailReader;
    private final MysqlMaterialsInventoryRequestDetailWriter detailWriter;
    private final SyncTaskLogRepository log;
    private final SyncCheckpointRepository checkpointRepository;

    @Value("${sync.batch-size.materials-inventory-request:300}")
    private int requestBatchSize;

    @Value("${sync.batch-size.materials-inventory-request-detail:500}")
    private int detailBatchSize;

    @Value("${sync.throttle-sleep-ms.materials-inventory-request:100}")
    private long sleepMillisAfterBatch;

    @Value("${sync.incremental.materials-inventory-request.request-date-lookback-days:30}")
    private int requestDateLookbackDays;

    public MaterialsInventoryRequestFullSyncService(SqlServerMaterialsInventoryRequestReader requestReader,
                                                    MysqlMaterialsInventoryRequestWriter requestWriter,
                                                    SqlServerMaterialsInventoryRequestDetailReader detailReader,
                                                    MysqlMaterialsInventoryRequestDetailWriter detailWriter,
                                                    SyncTaskLogRepository log,
                                                    SyncCheckpointRepository checkpointRepository) {
        this.requestReader = requestReader;
        this.requestWriter = requestWriter;
        this.detailReader = detailReader;
        this.detailWriter = detailWriter;
        this.log = log;
        this.checkpointRepository = checkpointRepository;
    }

    public void syncFull() {
        syncRequestsFull();
        syncDetailsFull();
    }

    /**
     * 增量方案：物料主表使用“Id 高水位 + RequestDate 回溯”，明细表使用“Id 高水位 + 跟随主表重刷”。
     * 主表本批命中的 Id 会传给明细重刷，覆盖明细无更新时间但随主表业务动作发生修改的场景。
     */
    public void syncIncremental() {
        LOGGER.info("Start materials inventory incremental sync, requestStrategy=ID_WATERMARK_REQUEST_DATE_LOOKBACK, detailStrategy=ID_WATERMARK_PARENT_REFRESH");
        Set<Integer> changedRequestIds = syncRequestsIncremental();
        syncDetailsIncremental(changedRequestIds);
        LOGGER.info("Finish materials inventory incremental sync, changedRequestCount={}", changedRequestIds.size());
    }

    private void syncRequestsFull() {
        String task = "sync_materials_inventory_request_full";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<MaterialsInventoryRequestRow> rows = requestReader.readByIdGreaterThan(last, requestBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                requestWriter.upsertBatch(rows);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    private void syncDetailsFull() {
        String task = "sync_materials_inventory_request_detail_full";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<MaterialsInventoryRequestDetailRow> rows = detailReader.readByIdGreaterThan(last, detailBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                detailWriter.upsertBatch(rows);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    private Set<Integer> syncRequestsIncremental() {
        String task = "sync_materials_inventory_request_incremental";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        Set<Integer> changedRequestIds = new LinkedHashSet<>();
        try {
            int last = checkpointRepository.getOrDefault(task, "MaterialsInventoryRequest", "ID").getLastId();
            LOGGER.info("Start incremental sync task={}, batchNo={}, strategy=ID_WATERMARK_REQUEST_DATE_LOOKBACK, lastId={}, requestDateLookbackDays={}",
                    task, batchNo, last, requestDateLookbackDays);
            while (true) {
                List<MaterialsInventoryRequestRow> rows = requestReader.readByIdGreaterThan(last, requestBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                requestWriter.upsertBatch(rows);
                write += rows.size();
                changedRequestIds.addAll(toRequestIds(rows));
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                checkpointRepository.save(task, "MaterialsInventoryRequest", "ID", last, null);
                sleepQuietly();
            }

            LocalDateTime since = LocalDateTime.now().minusDays(requestDateLookbackDays);
            LOGGER.info("Replay materials inventory request lookback task={}, batchNo={}, since={}",
                    task, batchNo, since);
            int lookbackLastId = 0;
            while (true) {
                List<MaterialsInventoryRequestRow> rows = requestReader.readByRequestDateSince(since, lookbackLastId, requestBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                requestWriter.upsertBatch(rows);
                write += rows.size();
                changedRequestIds.addAll(toRequestIds(rows));
                lookbackLastId = Math.max(lookbackLastId, rows.get(rows.size() - 1).getId());
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
            LOGGER.info("Finish incremental sync task={}, batchNo={}, status=SUCCESS, read={}, write={}, finalLastId={}, lookbackLastId={}, changedRequestCount={}",
                    task, batchNo, read, write, last, lookbackLastId, changedRequestIds.size());
            return changedRequestIds;
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            LOGGER.error("Finish incremental sync task={}, batchNo={}, status=FAILED, read={}, write={}, changedRequestCount={}",
                    task, batchNo, read, write, changedRequestIds.size(), e);
            throw e;
        }
    }

    private void syncDetailsIncremental(Set<Integer> changedRequestIds) {
        String task = "sync_materials_inventory_request_detail_incremental";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        int last = checkpointRepository.getOrDefault(task, "MaterialsInventoryRequestDetail", "ID").getLastId();
        try {
            LOGGER.info("Start incremental sync task={}, batchNo={}, strategy=ID_WATERMARK_PARENT_REFRESH, lastId={}, parentRefreshCount={}",
                    task, batchNo, last, changedRequestIds.size());
            while (true) {
                List<MaterialsInventoryRequestDetailRow> rows = detailReader.readByIdGreaterThan(last, detailBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                detailWriter.upsertBatch(rows);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                checkpointRepository.save(task, "MaterialsInventoryRequestDetail", "ID", last, null);
                sleepQuietly();
            }

            List<Integer> ids = new ArrayList<>(changedRequestIds);
            for (int start = 0; start < ids.size(); start += requestBatchSize) {
                List<Integer> slice = ids.subList(start, Math.min(start + requestBatchSize, ids.size()));
                List<MaterialsInventoryRequestDetailRow> rows = detailReader.readByRequestIds(slice);
                if (rows.isEmpty()) {
                    continue;
                }
                read += rows.size();
                detailWriter.upsertBatch(rows);
                write += rows.size();
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
            LOGGER.info("Finish incremental sync task={}, batchNo={}, status=SUCCESS, read={}, write={}, finalLastId={}, parentRefreshCount={}",
                    task, batchNo, read, write, last, changedRequestIds.size());
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            LOGGER.error("Finish incremental sync task={}, batchNo={}, status=FAILED, read={}, write={}, parentRefreshCount={}",
                    task, batchNo, read, write, changedRequestIds.size(), e);
            throw e;
        }
    }

    private List<Integer> toRequestIds(List<MaterialsInventoryRequestRow> rows) {
        return rows.stream().map(MaterialsInventoryRequestRow::getId).collect(Collectors.toList());
    }

    private String newBatchNo() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private void sleepQuietly() {
        if (sleepMillisAfterBatch <= 0) {
            return;
        }
        try {
            Thread.sleep(sleepMillisAfterBatch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("materials inventory request full sync interrupted", e);
        }
    }
}
