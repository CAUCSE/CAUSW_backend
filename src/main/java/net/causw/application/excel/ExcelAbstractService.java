package net.causw.application.excel;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.exceptions.BadRequestException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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
            List<String> headerStringList,
            LinkedHashMap<String, List<T>> sheetNameDataMap
    ) {
        if (headerStringList == null || headerStringList.isEmpty()) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FAIL_TO_GENERATE_EXCEL_FILE);
        }
        try (
            Workbook workbook = new XSSFWorkbook();
            ServletOutputStream outputStream = response.getOutputStream()
        ) {
            for (String sheetName : sheetNameDataMap.keySet()) {
                createSheet(workbook, sheetName, headerStringList, sheetNameDataMap.get(sheetName));
            }
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FAIL_TO_GENERATE_EXCEL_FILE);
        }
    }

    @Override
    public void createSheet(Workbook workbook, String sheetName, List<String> headerStringList, List<T> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet, headerStringList);
        createDataRows(sheet, dataList);
    }

    @Override
    public void createHeaderRow(Sheet sheet, List<String> headerStringList) {
        Row headerRow = sheet.createRow(0);

        Cell cell;

        for (int i = 0; i < headerStringList.size(); i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(headerStringList.get(i));
        }

    }

    @Override
    public abstract void createDataRows(Sheet sheet, List<T> dataList);

}
