package com.example.smartinspection.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CustomerServiceHistoryRow {
    private Integer id; private String createdById; private LocalDateTime createDate; private String content;
    private Integer customerServiceId; private Integer operationStatus; private Integer complaintHandleId;
    private Integer dispatchId; private String wordContent;
}
