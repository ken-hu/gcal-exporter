package exporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Setup {

    private static final Logger logger = LoggerFactory.getLogger(Setup.class);

    /** Application name. */
    public static final String APPLICATION_NAME =
            "Google Calendar Exporter";

    /** Directory to store user credentials for this application. */
    public static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-exporter");

    /** Global instance of the {@link FileDataStoreFactory}. */
    public static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    public static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    public static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by calendar-exporter.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-exporter
     */
    public static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY, SheetsScopes.SPREADSHEETS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                Gcal.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        logger.info(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

}