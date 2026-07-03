package com.accenture.ai.onboardingagentgitclient.model;

import lombok.*;

@Builder
public record EmployeeDetail(String employeeName, String employeeEmail, String requestType, String status) {

}
