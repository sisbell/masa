package org.maven.reslinks.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;

/**
 * 
 * @goal link
 * @requiresProject true
 * 
 * @phase generate-sources
 */
public class ResourceLinkerMojo extends AbstractMojo {
    
	/**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;
    
    /**
     * @parameter
     */
    public String[] resourceFolders;
    
    private HashMap<String, String> fileMap = new HashMap<String, String>();
    
    private String baseResourcesDirName;
	
	public void execute() throws MojoExecutionException {
		if(resourceFolders == null) {
			return;
		}
		 baseResourcesDirName = new File(  project.getBasedir(), "resources").getAbsolutePath() + File.separator;
			
		 mapGenericResourceDirectory();
		 for(String resourceFolder: resourceFolders) {
			 File resourceDir = new File(  project.getBasedir(), "resources" + File.separator + resourceFolder );
			 if(!resourceDir.exists()) {
				 throw new MojoExecutionException("Resource folder does not exist: " + resourceDir.getAbsolutePath());
			 }
			 if(!"generic".equals(resourceFolder)) {
				 mapDeviceResourceDirectory(resourceFolder);
			 }			 
		 }

		 cleanResourceDirectory();
		 
		 CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
	     executor.setLogger( getLog() );
	        	     
		 for(Map.Entry<String, String> e : fileMap.entrySet()) {
			 File targetDir = new File("res", e.getKey());
			 targetDir.getParentFile().mkdirs();
			 
			 List<String> commands = new ArrayList<String>();
			 commands.add("-s");
			 commands.add(new File("resources", e.getValue() + File.separator + e.getKey()).getAbsolutePath());
			 commands.add(targetDir.getAbsolutePath());
			 this.getLog().debug(commands.toString());
			 try
		        {
		            executor.executeCommand( "ln", commands, project.getBasedir(), false );
		        }
		        catch ( ExecutionException ex )
		        {
		            throw new MojoExecutionException( "", ex );
		        }
		        
		 }
	}
	
	private void cleanResourceDirectory() throws MojoExecutionException {
		 CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
	     executor.setLogger( getLog() );
	     List<String> commands = new ArrayList<String>();
		 commands.add("-fR");
		 commands.add(new File("res").getAbsolutePath());
		 this.getLog().debug(commands.toString());
		 try
	        {
	            executor.executeCommand( "rm", commands, project.getBasedir(), false );
	        }
	        catch ( ExecutionException ex )
	        {
	            throw new MojoExecutionException( "", ex );
	        }	     
	}
	
	private void mapDeviceResourceDirectory(String resourceFolder) throws MojoExecutionException {
		File resourceDir = new File(  project.getBasedir(), "resources" + File.separator + resourceFolder );
		addFiles(resourceDir);	
	}
	
	private void mapGenericResourceDirectory() throws MojoExecutionException {
		File resourceDir = new File(  project.getBasedir(), "resources" + File.separator + "generic" );	 
		 if(!resourceDir.exists()) {
			 throw new MojoExecutionException("Generic resource folder does not exist");
		 }	 
		 addFiles(resourceDir);
	}
	
	private void addFiles(File root) {	
		if(root.isDirectory()) {
			for(File f : root.listFiles()) {
				addFiles(f);				
			}
		} else {
			String fragment = root.getAbsolutePath().replace(baseResourcesDirName, "");
			int index = fragment.indexOf(File.separator);
			String value = fragment.substring(0, index);
			String key = fragment.substring(index + 1, fragment.length());
			fileMap.put(key, value);
		}
	}
}
