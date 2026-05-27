package com.example.smartinspection.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrganizationItemRow {
    private Integer id;
    private String name;
    private LocalDateTime updateTime;
}
