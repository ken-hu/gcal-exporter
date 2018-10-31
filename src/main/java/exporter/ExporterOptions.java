package exporter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ExporterOptions {

    private static final Options options = new Options();

    private static final Option timeStart = Option.builder("s")
        .required(true)
        .longOpt("start")
        .hasArg()
        .desc("Start date (inclusive) yy.MM.dd")
        .build();

    private static final Option timeEnd = Option.builder("e")
        .required(true)
        .longOpt("end")
        .hasArg()
        .desc("End date (inclusive) yy.MM.dd")
        .build();

    static {
        options.addOption(timeStart);
        options.addOption(timeEnd);
    }

    private String start;
    private String end;

    public String getTimeStart() {
        return start;
    }

    public String getTimeEnd() {
        return end;
    }

    public boolean parseArgs(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("start")) {
                start = cmd.getOptionValue("start");
            }

            if (cmd.hasOption("end")) {
                end = cmd.getOptionValue("end");
            }
        } catch (Exception exp) {
            System.out.println("Unexpected exception: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("gcal-exporter -e <arg> -s <arg>", options);
            return false;
        }
        return true;
    }
}
