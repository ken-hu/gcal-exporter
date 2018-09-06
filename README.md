# Google Calendar Exporter

[![Build Status](https://travis-ci.org/ken-hu/gcal-exporter.svg?branch=master)](https://travis-ci.org/ken-hu/gcal-exporter)

Export Google Calendar to Google Spreadsheets with _gcal-exporter_ in Java. All the methods in Gsheet.java and Gspread.java are reusable.


## Prerequisites

* Java 1.8 or greater
* Gradle 4.10 or greater

## Run

```
$ gradle run
```

## Creating .jar

```
$ gradle fatJar
```
After entering the start date and end date of the calendar events to export, it will open a new window or tab in the default browser window for the authorization.

## Google Calendar API

### Get Calendars List
```java
Gcal gcal = new Gcal(timeStart, timeEnd);
List<CalendarListEntry> calendars = gcal.getCalendars();
```

### Print Calendars or Events
```java
// Print name, id, and time zone of calendars
gcal.printCalendarInfo();

// Print all the events in all calendars
gcal.printAllEvents();
```

### Get Data from Calendars
```java
// Get all the events from a calendar
List<List<String>> data = gcal.getDataFromCalendar(CalendarListEntry calendar);

// Get a number of events from a calendar
List<List<String>> data = gcal.getDataFromCalendar(CalendarListEntry calendar, Integer numberOfEvents);
```

## Google Spreadsheets API

### Get a Spreadsheet
```java
Gsheet gsheet = new Gsheet();

// Create a spreadsheet
spreadsheet = gsheet.createNewSpreadsheet(String spreadsheetTitle);

// Find a spreadsheet
spreadsheet = gsheet.getSpreadsheet(String spreadsheetId);
```

### Print Information of a Spreadsheet
```java
// Print sheet name and sheet id
gsheet.printSheetInfo(Spreadsheet spreadsheet);

// Print data in a sheet
gsheet.printSheetData(Spreadsheet spreadsheet, String sheetTitle);
```

### Update Spreadsheet Title
```java
gsheet.updateSpreadsheetTitle(Spreadsheet spreadsheet, String spreadsheetTitle);
```

### Get a Sheet
```java
// Add a sheet
newSheet = gsheet.addNewSheet(Spreadsheet spreadsheet, String sheetTitle);

// Find a sheet
sheet = gsheet.findSheet(Spreadsheet spreadsheet, String sheetTitle);
```

### Delete a Sheet
```java
gsheet.deleteSheet(Spreadsheet spreadsheet, Integer sheetId);
```

### Clear a Sheet
```java
gsheet.clearSheet(Spreadsheet spreadsheet, Sheet sheet);
```

### Append Row of Data to a Sheet
```java
// Append data to the default sheet created with the spreadsheet
gsheet.appendRowDtata(Spreadsheet spreadsheet, List<String> rowData);

// Append data to a new sheet
gsheet.appendRowDtata(Spreadsheet spreadsheet, Sheet sheet, List<String> rowData);
```

### Import Data to a Sheet
```java
// Import data to the default sheet
gsheet.importData(Spreadsheet spreadsheet, List<List<String>> data);

// Import data to a new sheet
gsheet.importData(Spreadsheet spreadsheet, Sheet sheet, List<List<String>> data);
```

### Format a Sheet
```java
// Resize all columns based on the contents of the cells in the default sheet
gsheet.resizeColumns(Spreadsheet spreadsheet);

// Resize all columns based on the contents of the cells in a given sheet
gsheet.resizeColumns(Spreadsheet spreadsheet, Sheet sheet);
```

### Sort Data
```java
// Sort data in rows based on a sort order of a column in the default sheet
gsheet.sortByColumn(Spreadsheet spreadsheet, Integer columnIndex, Integer startRowIndex, String sortSpec);

// Sort data in rows based on a sort order of a column in the given sheet
gsheet.sortByColumn(Spreadsheet spreadsheet, Sheet sheet, Integer columnIndex, Integer startRowIndex, String sortSpec);
```
