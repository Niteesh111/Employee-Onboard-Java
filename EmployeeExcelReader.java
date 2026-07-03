package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.model.EmployeeDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class EmployeeExcelReader {

    public List<EmployeeDetail> readEmployeeDetails(String filePath) {
        List<EmployeeDetail> employeeDetailsList = new ArrayList<>();
        try (InputStream fis = FileStreamUtil.input(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row if present
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Add more fields as per EmployeeDetails model
                employeeDetailsList.add(EmployeeDetail.builder()
                                .employeeName(getCellValueAsString(row.getCell(0)))
                                .employeeEmail(getCellValueAsString(row.getCell(1)))
                                .requestType(getCellValueAsString(row.getCell(2)))
                                .status(getCellValueAsString(row.getCell(3)))
                        .build());
            }

            log.info("Successfully read employee details from Excel");
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
        }
        return employeeDetailsList;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public void updateEmployeeStatus(String filePath, String employeeName, String requestType, String status) {
        try {
            if (filePath == null || filePath.trim().isEmpty() ||
                    employeeName == null || employeeName.trim().isEmpty() ||
                    requestType == null || requestType.trim().isEmpty() ||
                    status == null || status.trim().isEmpty()) {
                log.error("File path, employee name, request type, and status cannot be null or empty");
                throw new IllegalArgumentException("Required parameters are missing");
            }

            try (InputStream fis = FileStreamUtil.input(filePath);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                // Skip header
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                boolean updated = false;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    String empName = getCellValueAsString(row.getCell(0));
                    String reqType = getCellValueAsString(row.getCell(2));
                    if (employeeName.equals(empName) && requestType.equals(reqType)) {
                        Cell statusCell = row.getCell(3);
                        if (statusCell == null) {
                            statusCell = row.createCell(3);
                        }
                        statusCell.setCellValue(status);
                        updated = true;
                        break; // Assuming unique combination
                    }
                }

                if (updated) {
                    try (OutputStream fos = FileStreamUtil.output(filePath)) {
                        workbook.write(fos);
                    }
                    log.info("Employee status updated successfully for employee: {}, request type: {}, new status: {}",
                            employeeName, requestType, status);
                } else {
                    log.warn("No matching employee found to update");
                }
            }


        } catch (Exception e) {
            log.error("Error updating employee status in Excel file: {}", e.getMessage(), e);
        }
    }
}
