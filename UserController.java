package com.accenture.ai.onboardingagentgitclient.controller;

import com.accenture.ai.onboardingagentgitclient.model.AccessRequest;
import com.accenture.ai.onboardingagentgitclient.model.EmployeeDetail;
import com.accenture.ai.onboardingagentgitclient.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboard")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<List<EmployeeDetail>> getAllEmployees() {
        try {
            List<EmployeeDetail> employeeDetailsList = userService.getAllEmployeeDetailsList();
            return ResponseEntity.ok(employeeDetailsList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/getEmployeeDetails")
    public ResponseEntity<List<EmployeeDetail>> getEmployeeDetails(@RequestParam String employeeName) {
        try {
            if (employeeName == null || employeeName.isEmpty()) {
                throw new Exception("Employee name is null or empty");
            }
            List<EmployeeDetail> employeeDetailsList = userService.getEmployeeDetailsByName(employeeName);
            return ResponseEntity.ok(employeeDetailsList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/access")
    public ResponseEntity<String> sendRequestEmail(@Valid @RequestBody AccessRequest accessRequest) {
        try {
            userService.processAccessRequest(accessRequest);
            return ResponseEntity.ok("Request processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to send request email: " + e.getMessage());
        }
    }
}
