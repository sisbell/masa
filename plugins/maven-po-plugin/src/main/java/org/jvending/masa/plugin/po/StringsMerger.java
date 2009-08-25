package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.plugin.po.parser.PoEntry;
import org.jvending.masa.plugin.po.parser.PoParser;

public class StringsMerger {
	
	private Log log;
	
	public StringsMerger(Log log)
	{
		this.log = log;
	}
	
    public void mergeFiles(File inputFileA, File inputFileB, File outputFile, MavenProject project )
	    throws MojoExecutionException, MojoFailureException
	{
		File a;
		File b;
		try {
			a = File.createTempFile(String.valueOf(Math.random()), ".po");
			b = File.createTempFile(String.valueOf(Math.random()), ".po");
		} catch (IOException e) {
			throw new MojoExecutionException("", e);
		}
	
		List<PoEntry> entriesA;
		List<PoEntry> entriesB;
		try {
			String encodingA = PoTransformer.createTemplateFromStringsXml(inputFileA, a, project);//TODO: optimize for batch processing
	    	String encodingB = PoTransformer.createTemplateFromStringsXml(inputFileB, b, project);
	
			entriesA = PoParser.readEntries( new FileInputStream( a ), encodingA);
			entriesB = PoParser.readEntries( new FileInputStream( b ), encodingB );
			log.info("Merging files: A = " +  a.getName() + ", B = " + b.getName() + ", Output = " + outputFile.getName());
	    	merge(entriesA, entriesB);    
	    	PoTransformer.writePoFile(entriesA, outputFile, encodingB);
		} catch (IOException e) {
			throw new MojoExecutionException("", e);
		}
	}
	
	private void merge(List<PoEntry> entriesA, List<PoEntry> entriesB)
	{
		double x =0, y = 0;
		for(PoEntry a : entriesA)
		{
			String messageId = getMessageIdFrom(entriesB, a.message.messageContext);
			if(messageId != null)
			{
				x++;
				a.message.messageString = messageId;
			}
			else
			{
				y++;
				if(a.message.messageContext != null)
					this.log.info("Missing translation: Context = " + a.message.messageContext);
			}
		}
	
		int total = (int) (x + y);
		double p = x/total;
		this.log.info("Translation: Total =  " + total + ", % Translated = "  + p);
	}
	
	private String getMessageIdFrom(List<PoEntry> entries, String messageContext)
	{
		for(PoEntry po : entries)
		{
			if(po.message.messageContext == null)
			{
				continue;
			}
			
			if(po.message.messageContext.equals(messageContext))
			{
				return po.message.messageId;
			}
		}
		return null;
}
}
