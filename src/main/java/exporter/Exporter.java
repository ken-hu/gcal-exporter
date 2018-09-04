package exporter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.devtools.common.options.OptionsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exporter {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

    public static void exportGcalToGsheet(String start, String end) throws IOException {
        Date timeStart = null;
        Date timeEnd = null;
        TimeZone.setDefault(TimeZone.getTimeZone(Gcal.timeZone));
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
        //gcal.getCalendarInfo();
        Gsheet gsheet = new Gsheet();
        String spreadsheetTitle = "[Gcal] " + start + "-" + end;
        Spreadsheet spreadsheet = gsheet.createNewSpreadsheet(spreadsheetTitle);
        logger.info("Exporting data from Calendar to Spreadsheet.");

        int numOfEvents = 0;
        int numOfCalendar = 0;
        for (CalendarListEntry calendar : calendars) {
            String calendarName = calendar.getSummary();

            List<Gcal.Row> data = gcal.getDataFromCalendar(calendar);
            if (data.isEmpty()) continue;

            List<String> rowData = Arrays.asList("Event", "Start", "End", "Duration");
            Sheet newSheet = gsheet.addNewSheet(spreadsheet, calendarName);
            gsheet.appendRowDtata(spreadsheet, newSheet, rowData);
            gsheet.importData(spreadsheet, newSheet, data);
            //gsheet.sortByColumn(spreadsheet, newSheet, 1, 2, "ASCENDING");
            gsheet.resizeColumns(spreadsheet, newSheet);
            numOfEvents += data.size();
            numOfCalendar++;
        }
        gsheet.deleteSheet(spreadsheet, 0);
        logger.info("Succeeded! {} events exported from {} Calendar(s) to the Spreadsheet '{}' in Google Drive.", numOfEvents, numOfCalendar, spreadsheetTitle);
    }

    public static void main(String[] args) throws IOException {
        OptionsParser parser = OptionsParser.newOptionsParser(ExporterOptions.class);
        parser.parseAndExitUponError(args);
        ExporterOptions options = parser.getOptions(ExporterOptions.class);

        if (options.start.isEmpty() || options.end.isEmpty()) {
            // print usage
            System.out.println(parser.describeOptions(Collections.<String, String>emptyMap(),
                        OptionsParser.HelpVerbosity.LONG));
            return;
        }

        exportGcalToGsheet(options.start, options.end);

        //System.out.println("Please enter the start date (inclusive) yy.MM.dd: ");
        //Scanner scanner  = new Scanner(System.in);
        //String Startdate = scanner.next();
        //logger.info("date entered: {}", Startdate);

        //System.out.println("Please enter the end date (inclusive) yy.MM.dd: ");
        //String EndDate = scanner.next();
        //logger.info("date entered: {}", EndDate);
        //scanner.close();

        //exportGcalToGsheet(Startdate, EndDate);
    }
}
