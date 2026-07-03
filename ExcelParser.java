package com.accenture.ai.onboardingagentgitclient.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelParser {

    public List<Map<String, String>> extractSheets(InputStream is) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(is)) {
            for (Sheet sheet : wb) {
                Row header = sheet.getRow(0);
                if (header == null) continue;

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    Map<String, String> data = new LinkedHashMap<>();
                    for (int c = 0; c < header.getLastCellNum(); c++) {
                        String key = header.getCell(c).getStringCellValue();
                        Cell cell = row.getCell(c);
                        data.put(key, cell == null ? "" : cell.toString());
                    }
                    rows.add(data);
                }
            }
        }
        return rows;
    }
}
