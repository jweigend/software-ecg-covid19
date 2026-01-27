package de.qaware.ekg.awb.metricanalyzer.ui.chartng.export;

import javafx.scene.chart.XYChart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Helper that transforms the given series data to POI excel format
 * and write it to an excel sheet
 */
public class ExcelExporter {

    private File targetFile;

    public ExcelExporter(File exportFile) {
        this.targetFile = exportFile;
    }

    public void createExcelSheetFromSeriesData(List<ExcelExportSeriesData> seriesData) {

        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
             SXSSFWorkbook workbook = new SXSSFWorkbook(xssfWorkbook, 1000);
             FileOutputStream out = new FileOutputStream(targetFile)) {

            Sheet workSheet = workbook.createSheet("Chart data");

            // write the header with series column names
            writeHeader(workbook, workSheet, seriesData);

            // write the series data to excel-sheet
            writeSeriesData(workbook, workSheet, seriesData);

            workbook.write(out);

            // dispose of temporary files backing this workbook on disk
            workbook.dispose();

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }


    private void writeHeader(SXSSFWorkbook workbook, Sheet workSheet, List<ExcelExportSeriesData> seriesDataList) {

        Row headerRow = workSheet.createRow(0);

        Font defaultFont= workbook.createFont();
        defaultFont.setFontHeightInPoints((short)8);
        defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(true);
        defaultFont.setItalic(false);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        cellStyle.setFont(defaultFont);


        int columnIndex = 0;
        for(ExcelExportSeriesData seriesData : seriesDataList){

            // styling
            workSheet.setColumnWidth(columnIndex, 8000);

            // header values
            Cell timestampCell = headerRow.createCell(columnIndex++, CellType.STRING);
            timestampCell.setCellStyle(cellStyle);
            Cell valueCell = headerRow.createCell(columnIndex++, CellType.STRING);
            valueCell.setCellStyle(cellStyle);

            timestampCell.setCellValue("Date/Time - " + seriesData.getSeriesName());
            valueCell.setCellValue("Value - " + seriesData.getSeriesName());
        }
    }

    private void writeSeriesData(SXSSFWorkbook workbook, Sheet workSheet, List<ExcelExportSeriesData> seriesDataList) {

        boolean hasNextRow = true;
        int rowIndex = 0;

        while (hasNextRow) {

            hasNextRow = false;
            int columnIndex = 0;

            // we use rowIndex plus one because the first line in the sheet is reserved for the header and
            // so we start in the line above
            Row seriesDataRow = workSheet.createRow(rowIndex + 1);

            for(ExcelExportSeriesData seriesData : seriesDataList){

                XYChart.Data<Long, Double> dataPoint = seriesData.getDataAt(rowIndex);

                if (dataPoint != null) {
                    createDateTimeCell(workbook, seriesDataRow, columnIndex++, dataPoint.getXValue());
                    Cell valueCell = seriesDataRow.createCell(columnIndex++, CellType.NUMERIC);
                    valueCell.setCellValue(dataPoint.getYValue());

                    hasNextRow = true;
                } else {
                    columnIndex += 2;
                }
            }

            rowIndex++;
        }
    }


    private void createDateTimeCell(SXSSFWorkbook workbook, Row row, int columnIndex, long timestamp) {

        CreationHelper createHelper = workbook.getCreationHelper();

        // we style the second cell as a date (and time).  It is important to
        // create a new cell style from the workbook otherwise you can end up
        // modifying the built in style and effecting not only this cell but other cells.
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy hh:mm:ss.000"));
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(new Date(timestamp));
        cell.setCellStyle(cellStyle);
    }

}
