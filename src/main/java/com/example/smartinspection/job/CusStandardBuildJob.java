package com.example.smartinspection.job;

import com.example.smartinspection.service.CusRepeatRepairModelService;
import com.example.smartinspection.service.CusStandardTableBuildService;
import com.example.smartinspection.service.CusChargeRiskModelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CusStandardBuildJob {
    private final CusStandardTableBuildService standardTableBuildService;
    private final CusRepeatRepairModelService repeatRepairModelService;
    private final CusChargeRiskModelService chargeRiskModelService;

    public CusStandardBuildJob(CusStandardTableBuildService standardTableBuildService,
                               CusRepeatRepairModelService repeatRepairModelService,
                               CusChargeRiskModelService chargeRiskModelService) {
        this.standardTableBuildService = standardTableBuildService;
        this.repeatRepairModelService = repeatRepairModelService;
        this.chargeRiskModelService = chargeRiskModelService;
    }

    /**
     * 标准层与模型层一体化定时任务。
     *
     * 执行顺序（必须保持）：
     * 1) buildStdWorkOrderHistory：先生成 cus_std_work_order_history，提供主表汇总依赖；
     * 2) buildStdWorkOrder：再生成 cus_std_work_order（包含 history_count/last_history_*）；
     * 3) calculateRepeatRepairModel：最后基于标准主表计算近90天重复维修预警结果。
     *
     * 当前 Cron：每天 01:30（0 30 1 * * ?）。
     * 如需与 raw 同步解耦，可改为两个任务：01:30 构建标准表，02:00 计算模型。
     */
//    @Scheduled(cron = "0 30 1 * * ?")
    public void buildAndCalculate() {
        standardTableBuildService.buildStdWorkOrderHistory();
        standardTableBuildService.buildStdWorkOrder();
        repeatRepairModelService.calculateRepeatRepairModel();
        chargeRiskModelService.buildArrearAndReductionStdTables();
        chargeRiskModelService.calculateChargeRiskModels();
    }
}
