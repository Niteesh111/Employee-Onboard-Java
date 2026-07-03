package com.accenture.ai.onboardingagentgitclient.service;

import com.accenture.ai.onboardingagentgitclient.config.OnboardingProperties;
import com.accenture.ai.onboardingagentgitclient.model.AccessRequest;
import com.accenture.ai.onboardingagentgitclient.model.EmployeeDetail;
import com.accenture.ai.onboardingagentgitclient.util.EmailParser;
import com.accenture.ai.onboardingagentgitclient.util.EmployeeExcelReader;
import com.accenture.ai.onboardingagentgitclient.util.GitClientUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {

    private List<EmployeeDetail> employeeDetailsList;
    private final OnboardingProperties onboardingProperties;
    private final EmployeeExcelReader employeeExcelReader;
    private final GitClientUtil gitClientUtil;
    private final EmailParser emailParser;

    @PostConstruct
    public void loadEmployeeDetails() {

        try {
            var employeeDetailsFilePath = onboardingProperties.getEmployeeDetailsPath();
            log.info("Loading employee details from file: {}", employeeDetailsFilePath);

            // Validate file path
            if (employeeDetailsFilePath == null || employeeDetailsFilePath.trim().isEmpty()) {
                log.error("Employee details file path is not configured");
                throw new IllegalArgumentException("Employee details file path is not configured");
            }

            // Read employee details from Excel file
            employeeDetailsList = employeeExcelReader.readEmployeeDetails(employeeDetailsFilePath);

            // Validate loaded data
            if (CollectionUtils.isEmpty(employeeDetailsList)) {
                log.warn("No employee details were loaded from the file");
                employeeDetailsList = new ArrayList<>();
            } else {
                log.info("Successfully loaded {} employee records", employeeDetailsList.size());
            }

        } catch (Exception e) {
            log.error("Unexpected error while loading employee details: {}", e.getMessage(), e);
            employeeDetailsList = new ArrayList<>();
        }
    }

    public List<EmployeeDetail> getAllEmployeeDetailsList() {
        return employeeDetailsList;
    }

    public List<EmployeeDetail> getEmployeeDetailsByName(String employeeName) {
        if(Objects.isNull(employeeName) || employeeName.isEmpty()){
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }
        return employeeDetailsList.stream()
                .filter(details -> Objects.nonNull(details)
                        && Objects.nonNull(details.employeeName())
                        &&  employeeName.equalsIgnoreCase(details.employeeName()))
                .toList();
    }

    public void processAccessRequest(AccessRequest accessRequest) {
        String requestId = accessRequest.requestId();
        if(Objects.isNull(requestId) || requestId.isEmpty()){
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        String prompt = """
            You have received a new access request with the following ID: %s.
            Please fetch the details of this request from the document and prepare an email.
            The response should include:
            - Access Type Requested
            - Justification for Access
            - Any additional notes
            - To | CC | Bcc
           \s
            STRICT INSTRUCTION: Do not search the web or external sources.\s
            If any information above is missing, use "Information Not Found" as the value.
       \s""".formatted(requestId);

        String bodyContent = gitClientUtil.callChat(List.of(
                Map.of("role", "user", "content", prompt)))
                .map(gitClientUtil::extractText).block();

        Map<String, Object> emailDetails = emailParser.extractEmailDetails(bodyContent);

        //SendEmail logic should be invoked here

        CompletableFuture.runAsync(() -> updateEmployeeRequestStatus(accessRequest));
    }

    public void updateEmployeeRequestStatus(AccessRequest accessRequest) {
        try {
            String employeeName = accessRequest.name();
            String requestType = accessRequest.requestId();

            if (Objects.isNull(employeeName) || employeeName.isEmpty() ||
                    Objects.isNull(requestType) || requestType.isEmpty()) {
                log.error("Employee name or request type cannot be null or empty");
                throw new IllegalArgumentException("Employee name and request type are required");
            }

            // Find the employee in the list
            List<EmployeeDetail> matchingEmployees = getEmployeeDetailsByName(employeeName);
            if (matchingEmployees.isEmpty()) {
                log.warn("No employee found with name: {}", employeeName);
                return;
            }

            // Update status in Excel file
            employeeExcelReader.updateEmployeeStatus(
                    onboardingProperties.getEmployeeDetailsPath(),
                    employeeName,
                    requestType,
                    "Pending"
            );

            log.info("Successfully updated status to 'Requested' for employee: {} with request type: {}",
                    employeeName, requestType);

            loadEmployeeDetails(); //to update the preloaded employee details

        } catch (Exception e) {
            log.error("Error updating employee request status: {}", e.getMessage(), e);
        }
    }
}
