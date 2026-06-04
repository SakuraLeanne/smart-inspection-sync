package com.example.smartinspection.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerServiceRow {
    private Integer id;
    private String voucherNo;
    private Integer regionId;
    private Integer serviceSource;
    private String customerName;
    private String phone;
    private LocalDateTime finishTimeRequired;
    private LocalDateTime acceptedDate;
    private String details;
    private Integer serviceType;
    private LocalDateTime appointmentTime;
    private String place;
    private String weChatOpenId;
    private String evaluation;
    private Integer quality;
    private Integer attitude;
    private Integer punctuality;
    private LocalDateTime createTime;
    private Integer comment;
    private String commentContent;
    private Integer complaintStatus;
    private LocalDateTime confirmTime;
    private LocalDateTime dispatchFinishTime;
    private Integer dispatchPriorityId;
    private LocalDateTime dispatchTime;
    private Integer dispatchTypeId;
    private Boolean commentByCustomer;
    private LocalDateTime startProcessTime;
    private Integer organizationItemId;
    private Integer complainType;
    private Integer complaintStatusBeforeInValid;
    private String createdById;
    private BigDecimal postponeHours;
    private Integer repairTypeSourceOption;
    private Integer complaintApproverId;
    private LocalDateTime commentDate;
    private Integer complaintApprovalStatus;
    private Integer repairStatus;
    private Integer repairStatusBeforeInValid;
    private String returnVisitPerson;
    private String returnVisitPhone;
    private String createByPhone;
    private Integer propPatrolProblemId;
    private Integer buildingManagerId;
    private Integer customerId;
    private Integer overTimeStatus;
    private Integer serviceChannel;
    private String address;
    private String latitude;
    private String longitude;
}
