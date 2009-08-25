package org.jvending.masa.plugin.po;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * @goal merge-batch
 * @requiresProject false
 * @description
 */
public class StringsMergerBatchMojo extends AbstractMojo {
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
     * @parameter expression = "${inputFile}"
     * @required 
     */
    private File inputFile;

    /**
     * Strings input directory with files to merge to
     * 
     * @parameter expression = "${inputDir}"
     * @required
     */
    private File inputDir;
    
    /**
     * Po output directory
     * 
     * @parameter expression = "${outputDir}"
     * @required
     */
    private File outputDir;    
    
    public void execute()
    	throws MojoExecutionException, MojoFailureException
    {
    	if(!outputDir.exists())
    	{
    		outputDir.mkdirs();
    	}
    	
    	StringsMerger merger = new StringsMerger(this.getLog());
    	for(File inputFileB : inputDir.listFiles())
    	{
    		File outputFile = new File(outputDir, inputFileB.getName() + ".po");
        	merger.mergeFiles(inputFile, inputFileB, outputFile, project);   		
    	}

    }
}
