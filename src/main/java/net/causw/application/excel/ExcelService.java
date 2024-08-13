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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExcelService {

    public void generateCircleExcel(HttpServletResponse response, String circleName, List<CircleMemberResponseDto> awaitingMembers, List<CircleMemberResponseDto> activeMembers) {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSheet(workbook, "Await members", awaitingMembers);
            createSheet(workbook, "Active members", activeMembers);

            String encodedFileName = URLEncoder.encode(circleName + "_부원명단.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void createSheet(Workbook workbook, String sheetName, List<CircleMemberResponseDto> members) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet);
        createDataRows(sheet, members);
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("이름");

        cell = headerRow.createCell(1);
        cell.setCellValue("학번");

        cell = headerRow.createCell(2);
        cell.setCellValue("전화번호");

    }

    private void createDataRows(Sheet sheet, List<CircleMemberResponseDto> userList) {
        int rowNum = 1;
        for (CircleMemberResponseDto member : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(member.getUser().getName());
            row.createCell(1).setCellValue(member.getUser().getStudentId());
            //전화번호
//            row.createCell(2).setCellValue(member.getUser().());

        }
    }


}