package net.causw.application.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.LinkedHashMap;
import java.util.List;

public interface ExcelService<T> {

    void generateExcel(HttpServletResponse response,
                       String fileName,
                       List<String> headerStringList,
                       LinkedHashMap<String, List<T>> sheetNameDataMap
    );

    void createSheet(Workbook workbook, String sheetName, List<String> headerStringList, List<T> dataList);

    void createHeaderRow(Sheet sheet, List<String> headerStringList);

    void createDataRows(Sheet sheet, List<T> dataList);

}
