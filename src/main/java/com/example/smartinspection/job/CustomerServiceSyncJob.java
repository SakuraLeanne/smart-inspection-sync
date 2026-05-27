package com.example.smartinspection.job;

import com.example.smartinspection.service.CustomerServiceHistorySyncService;
import com.example.smartinspection.service.CustomerServiceSyncService;
import com.example.smartinspection.service.OrganizationItemSyncService;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceSyncJob {
    private final CustomerServiceSyncService customerServiceSyncService;
    private final CustomerServiceHistorySyncService historySyncService;
    private final OrganizationItemSyncService organizationItemSyncService;

    public CustomerServiceSyncJob(CustomerServiceSyncService c, CustomerServiceHistorySyncService h, OrganizationItemSyncService o) {
        this.customerServiceSyncService = c;
        this.historySyncService = h;
        this.organizationItemSyncService = o;
    }

    public void syncCustomerServiceNew(){ customerServiceSyncService.syncNew(); }
    public void syncCustomerServiceHistoryNew(){ historySyncService.syncNew(); }
    public void refreshCustomerServiceRecent(){ customerServiceSyncService.refreshRecent(); }
    public void refreshCustomerServiceHistoryRecent(){ historySyncService.refreshRecent(); }
    public void syncOrganizationItemFull(){ organizationItemSyncService.syncFullById(); }
    public void syncOrganizationItemIncrement(){ organizationItemSyncService.syncIncrementByUpdateTime(); }
}
