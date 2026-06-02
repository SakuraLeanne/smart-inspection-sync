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

    @PostMapping("/customer-service-history/full")
    public Map<String, Object> triggerCustomerServiceHistoryFullSync() {
        historySyncService.syncFull();
        return success("customer service history full sync finished");
    }

    @PostMapping("/organization-item/full")
    public Map<String, Object> triggerOrganizationItemFullSync() {
        organizationItemSyncService.syncFull();
        return success("organization item full sync finished");
    }

    @PostMapping("/materials-inventory-request/full")
    public Map<String, Object> triggerMaterialsInventoryRequestFullSync() {
        materialsInventoryRequestFullSyncService.syncFull();
        return success("materials inventory request and detail full sync finished");
    }

    @PostMapping("/full")
    public Map<String, Object> triggerAllFullSync() {
        organizationItemSyncService.syncFull();
        customerServiceSyncService.syncFull();
        historySyncService.syncFull();
        materialsInventoryRequestFullSyncService.syncFull();
        return success("all full sync tasks finished");
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("triggerTime", LocalDateTime.now().toString());
        return resp;
    }
}
