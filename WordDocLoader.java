package com.accenture.ai.onboardingagentgitclient.util;

import com.accenture.ai.onboardingagentgitclient.enums.SourceType;
import com.accenture.ai.onboardingagentgitclient.model.SourceRequest;
import com.accenture.ai.onboardingagentgitclient.service.SourceExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordDocLoader implements SourceExtractor {


    @Override
    public SourceType supports() {
        return SourceType.WORD;
    }

    @Override
    public List<String> extract(SourceRequest request) {
        return loadParagraphs(request.getLocation());
    }

    public List<String> loadParagraphs(String location) {
        List<String> contents = new ArrayList<>();
        try {
            try (InputStream is = FileStreamUtil.input(location)) {
                XWPFDocument document = new XWPFDocument(is);

                /* --------------------
                 * Headers
                 * -------------------- */
                for (XWPFHeader header : document.getHeaderList()) {
                    addIfNotBlank(contents, header.getText());
                }

                /* --------------------
                 * Normal paragraphs
                 * -------------------- */
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    String text = paragraph.getText();
                    if (text != null && !text.isBlank()) {
                        contents.add(text.trim());
                    }
                }

                /* --------------------
                 * Table Data
                 * -------------------- */
                for (XWPFTable table : document.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        StringBuilder rowText = new StringBuilder();
                        for (XWPFTableCell cell : row.getTableCells()) {
                            rowText.append(cell.getText().trim()).append(" | ");
                        }
                        if (!rowText.isEmpty()) {
                            contents.add(rowText.toString());
                        }
                    }
                }

                /* --------------------
                 * Footers
                 * -------------------- */
                for (XWPFFooter footer : document.getFooterList()) {
                    addIfNotBlank(contents, footer.getText());
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Word document: " + location, e);
        }
        return contents;
    }

    private void addIfNotBlank(List<String> list, String text) {
        if (text != null && !text.isBlank()) {
            list.add(text.trim());
        }
    }
}
