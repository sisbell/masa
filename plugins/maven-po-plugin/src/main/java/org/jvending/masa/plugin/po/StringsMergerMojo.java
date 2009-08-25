package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.plugin.po.parser.PoEntry;
import org.jvending.masa.plugin.po.parser.PoParser;

/**
 * 
 * @goal merge
 * @requiresProject false
 * @description
 */
public class StringsMergerMojo extends AbstractMojo
{
	
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * Strings input
     * 
     * @parameter expression = "${inputFileA}"
     * @required 
     */
    private File inputFileA;

    /**
     * Strings input
     * 
     * @parameter expression = "${inputFileB}"
     * @required
     */
    private File inputFileB;
    
    /**
     * Po output file
     * 
     * @parameter expression = "${outputFile}"
     * @required
     */
    private File outputFile;    

    public void execute()
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
			String encodingA = PoTransformer.createTemplateFromStringsXml(inputFileA, a, project);
	    	String encodingB = PoTransformer.createTemplateFromStringsXml(inputFileB, b, project);

			entriesA = PoParser.readEntries( new FileInputStream( a ), encodingA);
			entriesB = PoParser.readEntries( new FileInputStream( b ), encodingB );

	    	merge(entriesA, entriesB);    
	    	PoTransformer.writePoFile(entriesA, outputFile, encodingB);
	    	//PoTransformer.writeEntriesToFile(entriesA, outputFile);
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
    				this.getLog().info("Missing translation: Context = " + a.message.messageContext);
    		}
    	}

    	int total = (int) (x + y);
    	double p = x/total;
    	this.getLog().info("Translation: Count =  " + total + ", % Translated = "  + p);
    }
    
    private String getMessageIdFrom(List<PoEntry> entries, String messageContext)
    {
    	for(PoEntry po : entries)
    	{
    		if(po.message.messageContext == null)
    		{
    		//	this.getLog().info("No message context found: ID = " + po.message.messageId);
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
