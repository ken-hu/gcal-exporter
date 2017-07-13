package exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.AppendDimensionRequest;
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.SortRangeRequest;
import com.google.api.services.sheets.v4.model.SortSpec;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

public class Gsheet {

    private static Sheets service;
    public Gsheet() {}

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = Setup.authorize();
        return new Sheets.Builder(Setup.HTTP_TRANSPORT, Setup.JSON_FACTORY, credential)
                .setApplicationName(Setup.APPLICATION_NAME)
                .build();
    }

    static {
        try {
            Gsheet.service = getSheetsService();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BatchUpdateSpreadsheetResponse postRequest(Spreadsheet spreadsheet, Request request) throws IOException {
        List<Request> requests = Arrays.asList(request);
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests)
                .setIncludeSpreadsheetInResponse(true);
        BatchUpdateSpreadsheetResponse response =
                service.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
        return response;
    }

    private BatchUpdateSpreadsheetResponse postRequests(Spreadsheet spreadsheet, List<Request> requests) throws IOException {
        BatchUpdateSpreadsheetRequest body =
                new BatchUpdateSpreadsheetRequest().setRequests(requests).setIncludeSpreadsheetInResponse(true);
        BatchUpdateSpreadsheetResponse response =
                service.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
        return response;
    }

    /**
     * Create a new spreadsheet.
     * @return an empty spreadsheet
     * @throws IOException
     */
    public Spreadsheet createNewSpreadsheet(String spreadsheetTitle) throws IOException {
        Spreadsheet newSpreadsheet = service.spreadsheets().create(null).execute();
        updateSpreadsheetTitle(newSpreadsheet, spreadsheetTitle);
        System.out.printf("New SpreadsheetID is %s\n", newSpreadsheet.getSpreadsheetId());
        return newSpreadsheet;
    }

    /**
     * @return the spreadsheet at the given ID
     * @throws IOException
     */
    public Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException {
        return service.spreadsheets().get(spreadsheetId).execute();
    }

    /**
     * Print name and id of all the existing sheets.
     * @throws IOException
     */
    public void printSheetInfo(Spreadsheet spreadsheet) throws IOException {
        List<Sheet> sheetList = spreadsheet.getSheets();
        for (Sheet sheet : sheetList) {
            String sheetName = sheet.getProperties().getTitle();
            Integer sheetId = sheet.getProperties().getSheetId();
            System.out.printf("%s %d\n", sheetName, sheetId);
        }
    }

    /**
     * Print the data of a sheet.
     * @throws IOException
     */
    public void printSheetData(Spreadsheet spreadsheet, String sheetTitle) throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheet.getSpreadsheetId(), sheetTitle)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
        } else {
            for (List<Object> row : values) {
                System.out.printf("%s\n", row);
            }
        }
    }

    /**
     * Update the title of the spreadsheet.
     * @throws IOException
     */
    public void updateSpreadsheetTitle(Spreadsheet spreadsheet, String spreadsheetTitle) throws IOException {
        Request request = new Request()
                .setUpdateSpreadsheetProperties(new UpdateSpreadsheetPropertiesRequest()
                        .setProperties(new SpreadsheetProperties()
                                .setTitle(spreadsheetTitle))
                        .setFields("title"));
        List<Request> requests = Arrays.asList(request);
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
    }

    /**
     * Create a new sheet in a specific spreadsheet.
     * @return an empty sheet
     * @throws IOException
     */
    public Sheet addNewSheet(Spreadsheet spreadsheet, String sheetTitle) throws IOException {
        Request request = new Request()
                .setAddSheet(new AddSheetRequest()
                        .setProperties(new SheetProperties()
                                .setTitle(sheetTitle)));
        BatchUpdateSpreadsheetResponse response = postRequest(spreadsheet, request);
        Spreadsheet updatedSpreadsheet = response.getUpdatedSpreadsheet(); // Critical
        Sheet sheet = findSheet(updatedSpreadsheet, sheetTitle);
        System.out.printf("Sheet \"%s\" is created\n", sheetTitle);
        return sheet;
    }

    /**
     * Delete a sheet in a specific spreadsheet.
     * @throws IOException
     */
    public void deleteSheet(Spreadsheet spreadsheet, Integer sheetId) throws IOException {
        Request request = new Request()
                .setDeleteSheet(new DeleteSheetRequest()
                        .setSheetId(sheetId));
        postRequest(spreadsheet, request);
        System.out.printf("Sheet ID \"%s\" is deleted\n", sheetId);
    }

    /**
     * @return The sheet in the spreadsheet.
     * @throws IOException
     */
    public Sheet findSheet(Spreadsheet spreadsheet, String sheetTitle) throws IOException {
        List<Sheet> sheetList = spreadsheet.getSheets();
        for (Sheet sheet : sheetList) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return sheet;
            }
        }
        System.err.printf("Sheet %s is NOT found\n", sheetTitle);
        System.err.printf("There are %d worksheets\n", sheetList.size());
        return null;
    }

    /**
     * Clear all the values in a sheet
     * @throws IOException
     */
    public void clearSheet(Spreadsheet spreadsheet, Sheet sheet) throws IOException {
        Request request = new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setRange(new GridRange()
                                .setSheetId(sheet.getProperties().getSheetId()))
                        .setFields("userEnteredValue"));
        postRequest(spreadsheet, request);
    }

    /**
     * Append new cells after the last row with data in a sheet.
     * @throws IOException
     */
    public void appendRowDtata(Spreadsheet spreadsheet, List<String> rowData) throws IOException {
        Sheet sheet = spreadsheet.getSheets().get(0);
        appendRowDtata(spreadsheet, sheet, rowData);
    }

    public void appendRowDtata(Spreadsheet spreadsheet, Sheet sheet, List<String> rowData) throws IOException {
        Integer sheetId = sheet.getProperties().getSheetId();
        List<RowData> rows = new ArrayList<>();
        List<CellData> cellData = new ArrayList<>();
        for (String cell : rowData) {
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(cell)));
        }
        rows.add(new RowData().setValues(cellData));

        Request request = new Request()
                .setAppendCells(new AppendCellsRequest()
                        .setSheetId(sheetId)
                        .setRows(rows)
                        .setFields("userEnteredValue"));
        postRequest(spreadsheet, request);
    }

    /**
     * Insert the values to the cells starting from left corner.
     * @returns a request of insert the values
     */
    private Request insertValues(Integer sheetId, List<Gcal.Row> data) {
        GridCoordinate grid = new GridCoordinate()
                .setSheetId(sheetId)
                .setRowIndex(1)
                .setColumnIndex(0);

        List<RowData> rowData = new ArrayList<>();
        for (Gcal.Row row : data) {
            List<CellData> cellData = new ArrayList<>();
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(row.getEventName())));
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(row.getEventStartData())));
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(row.getEventEndData())));
            cellData.add(new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(row.getDuration())));
            rowData.add(new RowData().setValues(cellData));
        }

        Request request = new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(grid)
                        .setRows(rowData)
                        .setFields("userEnteredValue"));
        return request;
    }

    /**
     * Insert a matrix of strings to a sheet starting from left corner.
     * @throws IOException
     */
    public void importData(Spreadsheet spreadsheet, List<Gcal.Row> data) throws IOException {
        Sheet sheet = spreadsheet.getSheets().get(0);
        importData(spreadsheet, sheet, data);
    }

    public void importData(Spreadsheet spreadsheet, Sheet sheet, List<Gcal.Row> data) throws IOException {
        Integer	sheetId = sheet.getProperties().getSheetId();
        List<Request> requests = new ArrayList<Request>();
        // Default new sheet has 1000 rows,
        // append empty rows if more than 1000 rows to insert.
        if (data.size() > 1000) {
            requests.add(appendEmptyRows(sheetId, data.size() - 1000));
        }
        requests.add(insertValues(sheetId, data));
        postRequests(spreadsheet, requests);
    }

    private Request appendEmptyRows(Integer sheetId, Integer length) throws IOException {
        Request request = new Request()
                .setAppendDimension(new AppendDimensionRequest()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setLength(length));
        return request;
    }

    /**
     * Resize all columns based on the contents of the cells.
     * @throws IOException
     */
    public void resizeColumns(Spreadsheet spreadsheet) throws IOException {
        Sheet sheet = spreadsheet.getSheets().get(0);
        resizeColumns(spreadsheet, sheet);
    }

    public void resizeColumns(Spreadsheet spreadsheet, Sheet sheet) throws IOException {
        Integer	sheetId = sheet.getProperties().getSheetId();
        Request request = new Request()
                .setAutoResizeDimensions(new AutoResizeDimensionsRequest()
                        .setDimensions(new DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension("COLUMNS")));
        postRequest(spreadsheet, request);
    }

    /**
     * Sort data in rows based on a sort order of a column.
     * @throws IOException
     */
    public void sortByColumn(Spreadsheet spreadsheet, Integer columnIndex, Integer startRowIndex, String sortSpec) throws IOException {
        Sheet sheet = spreadsheet.getSheets().get(0);
        sortByColumn(spreadsheet, sheet, columnIndex, startRowIndex, sortSpec);
    }

    public void sortByColumn(Spreadsheet spreadsheet, Sheet sheet, Integer columnIndex, Integer startRowIndex, String sortSpec) throws IOException {
        Integer	sheetId = sheet.getProperties().getSheetId();
        List<SortSpec> sortSpecs = Arrays.asList(
                new SortSpec()
                .setDimensionIndex(columnIndex - 1) // column index, zero based.
                .setSortOrder(sortSpec)); // ASCENDING or DESCENDING

        Request request = new Request()
                .setSortRange(new SortRangeRequest()
                        .setRange(new GridRange()
                                .setSheetId(sheetId)
                                .setStartRowIndex(startRowIndex - 1)
                                //.setEndRowIndex(0)
                                //.setStartColumnIndex(0)
                                //.setEndColumnIndex(0)
                                )
                        .setSortSpecs(sortSpecs));
        postRequest(spreadsheet, request);
    }
}