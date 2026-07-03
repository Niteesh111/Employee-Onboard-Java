package com.accenture.ai.onboardingagentgitclient.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class OnboardingProperties {

    @Value("${onboarding.word-path}")
    private String wordPath;

    @Value("${onboarding.excel-path}")
    private String excelPath;

    @Value("${onboarding.employee-data-path}")
    private String employeeDetailsPath;
}
