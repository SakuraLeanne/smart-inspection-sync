package com.example.smartinspection.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SyncCheckpoint {
    private String taskCode;
    private String sourceTable;
    private String checkpointType;
    private Integer lastId;
    private LocalDateTime lastTime;
}
