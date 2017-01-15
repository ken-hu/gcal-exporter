import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

public class Exporter {

    public static void exportGcalToGsheet(String start, String end) throws IOException {
        Date timeStart = null, timeEnd = null;
        SimpleDateFormat dateFormater = new SimpleDateFormat("yy.MM.dd");
        try {
            timeStart = dateFormater.parse(start);
            timeEnd = dateFormater.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Gcal gcal = new Gcal(timeStart, timeEnd);
        List<CalendarListEntry> calendars = gcal.getCalendars();
        gcal.printCalendarInfo();
        Gsheet gsheet = new Gsheet();
        String spreadsheetTitle = "[Gcal] " + start + "-" + end;
        Spreadsheet spreadsheet = gsheet.createNewSpreadsheet(spreadsheetTitle);

        for (CalendarListEntry calendar : calendars) {
            String calendarName = calendar.getSummary();

            List<List<String>> data = gcal.getDataFromCalendar(calendar);

            List<String> rowData = Arrays.asList("Event", "Start", "End", "Duration");
            Sheet newSheet = gsheet.addNewSheet(spreadsheet, calendarName);
            gsheet.appendRowDtata(spreadsheet, newSheet, rowData);
            gsheet.importData(spreadsheet, newSheet, data);
            gsheet.importData(spreadsheet, newSheet, data);
            gsheet.sortByColumn(spreadsheet, newSheet, 1, 2, "ASCENDING");
            gsheet.resizeColumns(spreadsheet, newSheet);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Please enter the start date yy.MM.dd: ");
        Scanner scanner  = new Scanner(System.in);
        String Startdate = scanner.next();
        System.out.printf("date entered: %s\n", Startdate);

        System.out.println("Please enter the end date yy.MM.dd: ");
        String EndDate = scanner.next();
        System.out.printf("date entered: %s\n", EndDate);
        scanner.close();

        exportGcalToGsheet(Startdate, EndDate);
    }
}