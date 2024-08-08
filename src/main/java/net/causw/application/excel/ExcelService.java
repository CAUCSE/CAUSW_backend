package net.causw.application.excel;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public void generateCircleExcel(List<CircleMemberResponseDto> userList, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Circle Members");
            createHeaderRow(sheet);
            createDataRows(sheet, userList);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=circle_members.xlsx");

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("User Name");

        cell = headerRow.createCell(1);
        cell.setCellValue("User StudentID");

    }

    private void createDataRows(Sheet sheet, List<CircleMemberResponseDto> userList) {
        int rowNum = 1;
        for (CircleMemberResponseDto member : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(member.getUser().getName());
            row.createCell(1).setCellValue(member.getUser().getStudentId());

        }
    }


}
