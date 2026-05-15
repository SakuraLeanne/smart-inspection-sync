package com.example.smartinspection.job;

import com.example.smartinspection.service.CusRepeatRepairModelService;
import com.example.smartinspection.service.CusStandardTableBuildService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CusStandardBuildJob {
    private final CusStandardTableBuildService standardTableBuildService;
    private final CusRepeatRepairModelService repeatRepairModelService;

    public CusStandardBuildJob(CusStandardTableBuildService standardTableBuildService,
                               CusRepeatRepairModelService repeatRepairModelService) {
        this.standardTableBuildService = standardTableBuildService;
        this.repeatRepairModelService = repeatRepairModelService;
    }

    @Scheduled(cron = "0 30 1 * * ?")
    public void buildAndCalculate() {
        standardTableBuildService.buildStdWorkOrderHistory();
        standardTableBuildService.buildStdWorkOrder();
        repeatRepairModelService.calculateRepeatRepairModel();
    }
}
