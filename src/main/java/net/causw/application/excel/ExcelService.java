package net.causw.application.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelService<T> {

    void generateExcel(HttpServletResponse response, String fileName, List<T> dataList);

    void createSheet(Workbook workbook, String sheetName, List<T> dataList);

    void createHeaderRow(Sheet sheet);

     void createDataRows(Sheet sheet, List<T> dataList);

}
