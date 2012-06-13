package org.apache.maven.surefire.android;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.surefire.StartupReportConfiguration;
import org.apache.maven.plugin.surefire.report.AndroidReportConfiguration;
import org.apache.maven.plugin.surefire.report.FileReporterFactory;
import org.apache.maven.surefire.android.reporting.AndroidReportEntry;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.util.TestsToRun;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.ddmlib.AdbConnector;
import org.jvending.masa.ddmlib.ConnectionException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;

public class TestRunnerCoreProvider extends AbstractProvider {

	private ProviderParameters providerParameters;

	public TestRunnerCoreProvider(ProviderParameters parameters) {
		this.providerParameters = parameters;
	}

	public Iterator<?> getSuites() {
		return scanClassPath().iterator();
	}

	public RunResult invoke(Object forkTestSet) throws TestSetFailedException,
			ReporterException {
     

        AdbConnector conn = new AdbConnector();       
        IDevice device = null;
        try {
			conn.connectToDevice();
			device = conn.getFirstAttachedDevice();
			
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

        StartupReportConfiguration rconfig = AndroidReportConfiguration.defaultValue(device);
        
		final ReporterFactory reporterFactory = new FileReporterFactory(rconfig);
        final RunListener reporter = reporterFactory.createReporter();      
        final ConsoleOutputReceiver console = (ConsoleOutputReceiver) reporter; 
        
        Properties config = providerParameters.getProviderProperties();
        String testRunner = config.getProperty("testRunner", "android.test.InstrumentationTestRunner");
        String targetPackage = config.getProperty("targetPackage", "org.jvending.masa.test");
        
        RemoteAndroidTestRunner runner =
                new RemoteAndroidTestRunner(targetPackage, 
                		testRunner, device);
        runner.setTestPackageName("org.jvending.masa.test");
        
        try {
			runner.run(new ITestRunListener() {

				private AndroidReportEntry entry;
							
				public void testRunStarted(String runName, int testCount) {	
	
				}

				public void testStarted(TestIdentifier test) {
				   entry = new AndroidReportEntry(test.getClassName());     			
				   reporter.testSetStarting(entry);		   
				}

				public void testFailed(TestFailure status, TestIdentifier test,
						String trace) {
					if(trace != null) {
						console.writeTestOutput(trace.getBytes(), 0, trace.length() - 1, true);
						entry.setMessage(trace);
					}								
					reporter.testFailed(entry);					
				}

				public void testEnded(TestIdentifier test,
						Map<String, String> testMetrics) {
					System.out.println("testEnded: ");
					//reporter.testSucceeded(entry);
				}

				public void testRunFailed(String errorMessage) {				
				}

				public void testRunStopped(long elapsedTime) {
					reporter.testSetCompleted(entry);
				}

				public void testRunEnded(long elapsedTime,
						Map<String, String> runMetrics) {
					reporter.testSetCompleted(entry);
				}
				
			});
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			e.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return reporterFactory.close();

	}

    public void startTestSuite( RunListener reporter)
    {
        ReportEntry report = new SimpleReportEntry( "Reporter", "AndroidTestSuite", "Starting test" );

        try
        {
            reporter.testSetStarting( report );
        }
        catch ( ReporterException e )
        {
            // TODO: remove this exception from the report manager
        }
    }

    public void finishTestSuite( RunListener reporterManager )
        throws ReporterException
    {
        ReportEntry report = new SimpleReportEntry(  "Reporter", "AndroidTestSuite", "finishing test" );

        reporterManager.testSetCompleted( report );
    }
	
	private TestsToRun scanClassPath() {
		ClassLoader testClassLoader = providerParameters.getTestClassLoader();
		DirectoryScanner directoryScanner = providerParameters
				.getDirectoryScanner();

		final TestsToRun scanned = directoryScanner.locateTestClasses(
				testClassLoader, null);
		System.out.println("Tests: " + scanned.size());
		
		return scanned;
		//return runOrderCalculator.orderTestClasses(scanned);
	}
	

}
