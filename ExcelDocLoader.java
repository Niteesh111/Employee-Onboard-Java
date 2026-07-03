package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.enums.SourceType;
import com.accenture.ai.onboardingagentgitclient.model.SourceRequest;
import com.accenture.ai.onboardingagentgitclient.service.SourceExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelDocLoader implements SourceExtractor {

    @Override
    public SourceType supports() {
        return SourceType.EXCEL;
    }

    @Override
    public List<String> extract(SourceRequest request) {
        return loadContents(request.getLocation());
    }


    public List<String> loadContents(String location) {
        List<String> contents = new ArrayList<>();

        try {
            try (InputStream is = FileStreamUtil.input(location);
                 Workbook workbook = new XSSFWorkbook(is)) {

                FormulaEvaluator evaluator =
                        workbook.getCreationHelper().createFormulaEvaluator();
                for (Sheet sheet : workbook) {
                    contents.add("SHEET: " + sheet.getSheetName());
                    for (Row row : sheet) {
                        StringBuilder rowText = new StringBuilder();
                        for (Cell cell : row) {
                            rowText.append(readCell(cell, evaluator))
                                    .append(" | ");
                        }
                        String rowString = rowText.toString().trim();
                        if (!rowString.isBlank()) {
                            contents.add(rowString);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read Excel document: " + location, e);
        }
        return contents;
    }

    /* -----------------------
     * Cell Reader (Critical)
     * ----------------------- */
    private String readCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case FORMULA -> readFormulaCell(cell, evaluator);
            default -> "";
        };
    }

    private String readFormulaCell(Cell cell, FormulaEvaluator evaluator) {
        CellValue cv = evaluator.evaluate(cell);
        if (cv == null) return "";

        return switch (cv.getCellType()) {
            case STRING -> cv.getStringValue();
            case BOOLEAN -> String.valueOf(cv.getBooleanValue());
            case NUMERIC -> String.valueOf(cv.getNumberValue());
            default -> "";
        };
    }

}
