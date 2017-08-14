package exporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gcal {

    private static final Logger logger = LoggerFactory.getLogger(Gcal.class);

    private static com.google.api.services.calendar.Calendar service;
    private static CalendarList calendarList;
    private DateTime timeStart;
    private DateTime timeEnd;
    private Integer MAX_RESULTS = 2500;
    public static String timeZone;

    public Gcal(Date timeStart, Date timeEnd) {
        TimeZone.setDefault(TimeZone.getTimeZone(Gcal.timeZone));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Gcal.timeZone));
        cal.setTime(timeStart);
        this.timeStart = new DateTime(cal.getTime());
        cal.setTime(timeEnd);
        cal.add(Calendar.DATE, 1);
        this.timeEnd = new DateTime(cal.getTime());
    }

    public class Row {
        private String eventName;
        private String eventStartData;
        private String eventEndData;
        private Double duration;

        public  Row (String eventName, String eventStartData, String eventEndData, Double duration) {
            this.setEventName(eventName);
            this.setEventStartData(eventStartData);
            this.setEventEndData(eventEndData);
            this.setDuration(duration);
        }

        /**
         * @return the eventEndData
         */
        public String getEventEndData() {
            return eventEndData;
        }

        /**
         * @param eventEndData the eventEndData to set
         */
        public void setEventEndData(String eventEndData) {
            this.eventEndData = eventEndData;
        }

        /**
         * @return the eventName
         */
        public String getEventName() {
            return eventName;
        }

        /**
         * @param eventName the eventName to set
         */
        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        /**
         * @return the eventStartData
         */
        public String getEventStartData() {
            return eventStartData;
        }

        /**
         * @param eventStartData the eventStartData to set
         */
        public void setEventStartData(String eventStartData) {
            this.eventStartData = eventStartData;
        }

        /**
         * @return the duration
         */
        public Double getDuration() {
            return duration;
        }

        /**
         * @param duration the duration to set
         */
        public void setDuration(Double duration) {
            this.duration = duration;
        }
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
    public void getCalendarInfo() throws IOException {
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
            List<Row> events = getDataFromCalendar(calendarListEntry, MAX_RESULTS);
            for (Row row : events) {
                System.out.println(row);
            }
            System.out.println();
        }
    }

    /**
     * Get all the events from a specific calendar.
     * @return List<List<eventName, eventStartData, eventEndData, duration>>
     * @throws IOException
     */
    public List<Row> getDataFromCalendar(CalendarListEntry calendar) throws IOException {
        return getDataFromCalendar(calendar, MAX_RESULTS);
    }

    public List<Row> getDataFromCalendar(CalendarListEntry calendar, Integer numberOfEvents) throws IOException {
        String calendarName = calendar.getSummary();
        Events events = getEvents(calendar.getId(), numberOfEvents);
        List<Event> items = events.getItems();
        List<Row> data = new ArrayList<>();
        if (items.size() == 0) {
            logger.info("No events found in {}.", calendarName);
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
                long interval = end.getValue() - start.getValue();
                String eventName = event.getSummary();
                String eventStartData = dateFormater.format(start.getValue());
                String eventEndData = dateFormater.format(end.getValue());
                double duration = (new BigDecimal(interval).divide(
                        new BigDecimal(3.6e6), 2, RoundingMode.HALF_UP)).
                        doubleValue();
                Row row = new Row(eventName, eventStartData, eventEndData, duration);
                data.add(row);
                //System.out.printf("%s %s %s %.2f\n", eventName, eventStartData, eventEndData, duration);
            }
        }
        logger.info("{} events extracted from calendar {}.",  items.size(), calendarName);
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
}