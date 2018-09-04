package exporter;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class ExporterOptions extends OptionsBase {

    @Option(
    name = "help",
    abbrev = 'h',
    help = "Prints usage info.",
    defaultValue = "true"
    )
    public boolean help;

    @Option(
    name = "timeStart",
    abbrev = 's',
    help = "Start date (inclusive) yy.MM.dd",
    category = "startup",
    defaultValue = "" 
    )
    public String start;

    @Option(
    name = "timeEnd",
    abbrev = 'e',
    help = "End date (inclusive) yy.MM.dd",
    category = "startup",
    defaultValue = "" 
    )
    public String end;
}
