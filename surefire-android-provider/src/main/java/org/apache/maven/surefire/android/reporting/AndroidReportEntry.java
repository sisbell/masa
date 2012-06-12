package org.apache.maven.surefire.android.reporting;

import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;

public class AndroidReportEntry extends SimpleReportEntry {

	protected String message;
	
	public AndroidReportEntry(String name) {
		super(name);
	}

	public AndroidReportEntry(String source, String name, Integer elapsed) {
		super(source, name, elapsed);
	}

	public AndroidReportEntry(String source, String name,
			StackTraceWriter stackTraceWriter, Integer elapsed, String message) {
		super(source, name, stackTraceWriter, elapsed, message);
	}

	public AndroidReportEntry(String source, String name,
			StackTraceWriter stackTraceWriter, Integer elapsed) {
		super(source, name, stackTraceWriter, elapsed);
	}

	public AndroidReportEntry(String source, String name,
			StackTraceWriter stackTraceWriter) {
		super(source, name, stackTraceWriter);
	}

	public AndroidReportEntry(String source, String name, String message) {
		super(source, name, message);
	}

	public AndroidReportEntry(String source, String name) {
		super(source, name);
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() == null ? message: super.getMessage();
	}
	
}
