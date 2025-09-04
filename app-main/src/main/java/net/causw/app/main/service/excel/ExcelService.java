package net.causw.app.main.service.excel;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import jakarta.servlet.http.HttpServletResponse;

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
