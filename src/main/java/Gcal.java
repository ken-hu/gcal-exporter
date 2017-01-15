import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class Gcal {

    private static com.google.api.services.calendar.Calendar service;
    private static CalendarList calendarList;
    private static String timeZone;
    private DateTime timeStart;
    private DateTime timeEnd;
    private Integer MAX_RESULTS = 2500;

    public Gcal(Date timeStart, Date timeEnd) {
        TimeZone.setDefault(TimeZone.getTimeZone(Gcal.timeZone));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Gcal.timeZone));
        cal.setTime(timeStart);
        this.timeStart = new DateTime(cal.getTime());
        cal.setTime(timeEnd);
        cal.add(Calendar.DATE, 1);
        this.timeEnd = new DateTime(cal.getTime());
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    private static com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = Setup.authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                Setup.HTTP_TRANSPORT, Setup.JSON_FACTORY, credential)
                .setApplicationName(Setup.APPLICATION_NAME)
                .build();
    }

    static {
        try {
            Gcal.service = getCalendarService();
            Gcal.calendarList = Gcal.service.calendarList().list().execute();
            Gcal.timeZone = Gcal.calendarList.getItems().get(0).getTimeZone();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * @return Calendars that are present on the user's calendar list.
     * @throws IOException
     */
    public List<CalendarListEntry> getCalendars() throws IOException {
        List<CalendarListEntry> calendars = Gcal.calendarList.getItems();
        return calendars;
    }

    /**
     * Print the name, id, and time zone of calendars
     * that are present on the user's calendar list.
     * @throws IOException
     */
    public void printCalendarInfo() throws IOException {
        List<CalendarListEntry> calendars = Gcal.calendarList.getItems();
        String calendarName, calendarId;

        System.out.printf("You have %d calendars\n", calendars.size());
        System.out.println("CalendarName CalendarID");
        for (CalendarListEntry calendarListEntry : calendars) {
            calendarName = calendarListEntry.getSummary();
            calendarId = calendarListEntry.getId();
            String calendarTimeZone = calendarListEntry.getTimeZone();
            System.out.printf("%s %s %s\n", calendarName, calendarId, calendarTimeZone);
        }
    }

    /**
     * Print all the events in all calendars.
     * @throws IOException
     */
    public void printAllEvents() throws IOException {
        List<CalendarListEntry> calendars = Gcal.calendarList.getItems();

        for (CalendarListEntry calendarListEntry : calendars) {
            String calendarName = calendarListEntry.getSummary();
            String calendarId = calendarListEntry.getId();

            System.out.printf("In Calendar %s, Events from %s to %s\n", calendarName, this.timeStart, this.timeEnd);
            System.out.printf("Calendar ID: %s\n", calendarId);
            List<List<String>> events = getDataFromCalendar(calendarListEntry, MAX_RESULTS);
            for (List<String> rows : events) {
                for (String row : rows) {
                    System.out.println(row);
                }
            }
            System.out.println();
        }
    }

    /**
     * Get all the events from a specific calendar.
     * @return List<List<eventName, eventStartData, eventEndData, duration>>
     * @throws IOException
     */
    public List<List<String>> getDataFromCalendar(CalendarListEntry calendar) throws IOException {
        return getDataFromCalendar(calendar, MAX_RESULTS);
    }

    public List<List<String>> getDataFromCalendar(CalendarListEntry calendar, Integer numberOfEvents) throws IOException {
        Events events = getEvents(calendar.getId(), numberOfEvents);
        List<Event> items = events.getItems();
        List<List<String>> data = new ArrayList<>();
        if (items.size() == 0) {
            System.out.println("No events found.");
        } else {
            for (Event event : items) {
                SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                // Get date for all day events
                if (start == null) {
                    start = event.getStart().getDate();
                    end   = event.getEnd().getDate();
                }
                double interval = end.getValue() - start.getValue();
                String eventName = event.getSummary();
                String eventStartData = dateFormater.format(start.getValue());
                String eventEndData = dateFormater.format(end.getValue());
                String duration = doubleToString(interval/3.6e6);
                List<String> row =
                        Arrays.asList(eventName, eventStartData, eventEndData, duration);
                data.add(row);
                //System.out.printf("%s %s %s %s\n", eventName, eventStartData, eventEndData, duration);
            }
        }
        return data;
    }

    private Events getEvents(String calendarId, Integer numberOfEvents) throws IOException {
        Events events = Gcal.service.events().list(calendarId)
                .setMaxResults(numberOfEvents)
                .setTimeMax(this.timeEnd)
                .setTimeMin(this.timeStart)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events;
    }

    // Get rid of unnecessary 0 tailings.
    private String doubleToString(double d) {
        if (d == (long)d) {
            return String.format("%d", (long)d);
        }
        return String.format("%.1f", d);
    }
}