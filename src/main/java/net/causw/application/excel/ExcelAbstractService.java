package net.causw.application.excel;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class ExcelAbstractService<T> implements ExcelService<T> {

    @Override
    public void generateExcel(
            HttpServletResponse response,
            String fileName,
            LinkedHashMap<String, List<T>> sheetNameDataMap
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {
            for (String sheetName : sheetNameDataMap.keySet()) {
                createSheet(workbook, sheetName, sheetNameDataMap.get(sheetName));
            }

            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FAIL_TO_GENERATE_EXCEL_FILE);
        }
    }

    @Override
    public void createSheet(Workbook workbook, String sheetName, List<T> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet);
        createDataRows(sheet, dataList);
    }

    @Override
    public abstract void createHeaderRow(Sheet sheet);

    @Override
    public abstract void createDataRows(Sheet sheet, List<T> dataList);

}
