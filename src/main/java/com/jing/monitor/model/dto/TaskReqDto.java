package com.jing.monitor.model.dto;

import lombok.Data;

@Data
public class TaskReqDto {
    // Unique Id
    private String sectionId;

    private String courseDisplayName;
    private String courseId;
    private Boolean enabled;
}