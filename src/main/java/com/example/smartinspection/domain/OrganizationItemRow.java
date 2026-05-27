package com.example.smartinspection.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrganizationItemRow {
    private Integer id;
    private Integer parentId;
    private String name;
    private String code;
    private Integer itemType;
    private String address;
    private String tel;
    private String remark;
    private Integer regionId;
    private Integer buildingId;
    private Integer companyId;
    private Integer groupId;
    private Integer unitId;
    private Integer cityId;
    private Integer buildingType;
    private Integer floors;
    private Integer floor;
    private Integer purpose;
    private Integer houseStatus;
    private Integer houseState;
    private Integer houseTypeId;
    private String discriminator;
    private String hierarchicalPath;
    private String contact;
    private String contactPhone;
    private String phone;
    private String email;
    private Boolean isVirtual;
    private Boolean isDisabled;
    private Boolean isRentable;
    private Boolean isRented;
    private Boolean isSold;
    private Boolean isLeaseOut;
    private String uuid;
    private String number;
    private String terminalCode;
    private String brandCode;
    private LocalDateTime checkInDate;
    private LocalDateTime handoverDate;
    private LocalDateTime soldTime;
    private LocalDateTime updateTime;
}
