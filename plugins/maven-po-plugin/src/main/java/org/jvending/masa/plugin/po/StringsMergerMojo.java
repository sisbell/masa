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
     * 
     * @parameter expression = "${removeEmptyEntries}" default-value="true"
     */
    private boolean removeEmptyEntries;   
    
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
    	StringsMerger merger = new StringsMerger(this.getLog());
    	merger.mergeFiles(inputFileA, inputFileB, outputFile, project, removeEmptyEntries);
    }
}
