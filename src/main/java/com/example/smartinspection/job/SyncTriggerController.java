package com.example.smartinspection.job;

import com.example.smartinspection.service.CustomerServiceHistorySyncService;
import com.example.smartinspection.service.CustomerServiceSyncService;
import com.example.smartinspection.service.MaterialsInventoryRequestFullSyncService;
import com.example.smartinspection.service.OrganizationItemSyncService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncTriggerController {
    private final CustomerServiceSyncService customerServiceSyncService;
    private final CustomerServiceHistorySyncService historySyncService;
    private final OrganizationItemSyncService organizationItemSyncService;
    private final MaterialsInventoryRequestFullSyncService materialsInventoryRequestFullSyncService;

    public SyncTriggerController(CustomerServiceSyncService customerServiceSyncService,
                                 CustomerServiceHistorySyncService historySyncService,
                                 OrganizationItemSyncService organizationItemSyncService,
                                 MaterialsInventoryRequestFullSyncService materialsInventoryRequestFullSyncService) {
        this.customerServiceSyncService = customerServiceSyncService;
        this.historySyncService = historySyncService;
        this.organizationItemSyncService = organizationItemSyncService;
        this.materialsInventoryRequestFullSyncService = materialsInventoryRequestFullSyncService;
    }

    @PostMapping("/customer-service/full")
    public Map<String, Object> triggerCustomerServiceFullSync() {
        customerServiceSyncService.syncFull();
        return success("customer service full sync finished");
    }

    @PostMapping("/customer-service/incremental")
    public Map<String, Object> triggerCustomerServiceIncrementalSync() {
        // 增量方案：Id 高水位捕获新增工单，CreateTime 回溯窗口重刷近期工单变化。
        customerServiceSyncService.syncIncremental();
        return success("customer service incremental sync finished: Id watermark + CreateTime lookback");
    }

    @PostMapping("/customer-service-history/full")
    public Map<String, Object> triggerCustomerServiceHistoryFullSync() {
        historySyncService.syncFull();
        return success("customer service history full sync finished");
    }

    @PostMapping("/customer-service-history/incremental")
    public Map<String, Object> triggerCustomerServiceHistoryIncrementalSync() {
        // 增量方案：历史流水表按 Id 高水位同步，默认只追加不回刷历史记录。
        historySyncService.syncIncremental();
        return success("customer service history incremental sync finished: Id watermark");
    }

    @PostMapping("/organization-item/full")
    public Map<String, Object> triggerOrganizationItemFullSync() {
        organizationItemSyncService.syncFull();
        return success("organization item full sync finished");
    }

    @PostMapping("/organization-item/incremental")
    public Map<String, Object> triggerOrganizationItemIncrementalSync() {
        // 增量方案：UpdateTime 非空走 UpdateTime+Id 水位；UpdateTime 为空走 Id 水位捕获新增。
        organizationItemSyncService.syncIncremental();
        return success("organization item incremental sync finished: UpdateTime+Id and null-UpdateTime Id watermarks");
    }

    @PostMapping("/materials-inventory-request/full")
    public Map<String, Object> triggerMaterialsInventoryRequestFullSync() {
        materialsInventoryRequestFullSyncService.syncFull();
        return success("materials inventory request and detail full sync finished");
    }

    @PostMapping("/materials-inventory-request/incremental")
    public Map<String, Object> triggerMaterialsInventoryRequestIncrementalSync() {
        // 增量方案：主表 Id 高水位 + RequestDate 回溯；明细 Id 高水位 + 跟随本批主表 Id 重刷。
        materialsInventoryRequestFullSyncService.syncIncremental();
        return success("materials inventory request and detail incremental sync finished: request Id watermark + RequestDate lookback, detail Id watermark + parent refresh");
    }

    @PostMapping("/full")
    public Map<String, Object> triggerAllFullSync() {
        organizationItemSyncService.syncFull();
        customerServiceSyncService.syncFull();
        historySyncService.syncFull();
        materialsInventoryRequestFullSyncService.syncFull();
        return success("all full sync tasks finished");
    }

    @PostMapping("/incremental")
    public Map<String, Object> triggerAllIncrementalSync() {
        // 全量顺序的增量版本：先同步组织，再同步工单与历史，最后同步物料主表和明细。
        organizationItemSyncService.syncIncremental();
        customerServiceSyncService.syncIncremental();
        historySyncService.syncIncremental();
        materialsInventoryRequestFullSyncService.syncIncremental();
        return success("all incremental sync tasks finished");
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("triggerTime", LocalDateTime.now().toString());
        return resp;
    }
}
