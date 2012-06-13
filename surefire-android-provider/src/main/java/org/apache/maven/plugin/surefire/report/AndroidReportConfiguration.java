package org.apache.maven.plugin.surefire.report;

import java.io.File;

import org.apache.maven.plugin.surefire.StartupReportConfiguration;

import com.android.ddmlib.IDevice;

public class AndroidReportConfiguration extends StartupReportConfiguration {

    private String reportNameSuffix;;

    private File reportsDirectory;

    private boolean trimStackTrace;
    
    private IDevice device;
    
	public AndroidReportConfiguration(boolean useFile, boolean printSummary,
			String reportFormat, boolean redirectTestOutputToFile,
			boolean disableXmlReport, File reportsDirectory,
			boolean trimStackTrace, String reportNameSuffix,
			String configurationHash, boolean requiresRunHistory, IDevice device) 
	{
		super(useFile, printSummary, reportFormat, redirectTestOutputToFile,
				disableXmlReport, reportsDirectory, trimStackTrace,
				reportNameSuffix, configurationHash, requiresRunHistory);
		this.reportNameSuffix = reportNameSuffix;
		this.reportsDirectory = reportsDirectory;
		this.trimStackTrace = trimStackTrace;
		this.device = device;
	}
	
    public static StartupReportConfiguration defaultValue(IDevice device)
    {
        File target = new File( "./target" );
        return new AndroidReportConfiguration( true, true, "PLAIN", false, false, target, false, null, "TESTHASH",
                                               false, device );
    }

	@Override
	public XMLReporter instantiateXmlReporter() {
		if (!isDisableXmlReport()) {
			return new AndroidXmlReporter(trimStackTrace, reportsDirectory,
					reportNameSuffix, device);
		}
		return null;
	}

}
