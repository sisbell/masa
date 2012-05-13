package org.jvending.masa.plugin.aapt;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;

/**
 * @goal unpack
 * @phase process-resources
 * @requiresDependencyResolution
 * @description
 */
public class LibraryResourceProcessorMojo extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    public MavenSession session;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
    	File outputDirectory = new File(project.getBuild().getDirectory());
    	
    	for(Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() ) {
    		getLog().info("Artifact: id = " + artifact.getArtifactId() +", " + artifact.getType() );
    			File artifactParent = artifact.getFile().getParentFile();
    	     	File lib = new File(artifactParent, artifact.getFile().getName().replace(".jar", "-resources.jar"));
    			getLog().info("Found library dependency: id = " + artifact.getArtifactId() 
    					+ "," + lib.getAbsolutePath());
        		if(lib.exists()) {
            		try {
            			String projectName = artifact.getGroupId() + "-" + artifact.getArtifactId() + "-res";
            			File out = new File(outputDirectory, projectName);
            			if(!out.exists()) {
            				out.mkdirs();
            			}
        				unjar( new JarFile(lib), out ); 
        				
        				unpackAndAddResources(out);
        		    	
        		    	getLog().info("Unpacking library resource: " + out.getAbsolutePath() );
        			} catch (IOException e) {
        				e.printStackTrace();
        			}          			
        		}  			
    	} 	
    }
    
    private void unpackAndAddResources(File out) throws MojoExecutionException {
    	Resource res = new Resource();
    	res.setDirectory( new File(out.getAbsolutePath(), "res").getAbsolutePath() );
    	project.addResource(res);     	
    }

    private void unjar( JarFile jarFile, File outputDirectory )
            throws IOException
        {
            for ( Enumeration en = jarFile.entries(); en.hasMoreElements(); )
            {
                JarEntry entry = (JarEntry) en.nextElement();
                if(entry.getName().contains("META-INF")) {
                	continue;
                }
                File entryFile = new File( outputDirectory, entry.getName() );
                if ( !entryFile.getParentFile().exists() && !entry.getName().startsWith( "META-INF" ) )
                {
                    entryFile.getParentFile().mkdirs();
                }
                
                if(entry.isDirectory()) {
                	new File(entry.getName()).mkdirs();
                } else {
                    final InputStream in = jarFile.getInputStream( entry );
                    try
                    {
                        final OutputStream out = new FileOutputStream( entryFile );
                        try
                        {

                            IOUtil.copy( in, out );
                        }
                        finally
                        {
                            closeQuietly( out );
                        }
                    }
                    finally
                    {
                        closeQuietly( in );
                    }

                }
              
            }
        }

        private void closeQuietly( final Closeable c )
        {
            try
            {
                c.close();
            }
            catch ( Exception ex )
            {
                getLog().warn( "Failed to close closeable " + c, ex );
            }
        }
}
