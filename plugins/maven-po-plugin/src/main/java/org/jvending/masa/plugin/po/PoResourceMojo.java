package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal resources
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @description
 */
public class PoResourceMojo
    extends AbstractMojo
{

    /**
     * Source directory containing the copied class files.
     * 
     * @parameter expression = "${resourceDirectory}" default-value="${project.basedir}/po"
     * @required
     */
    private String resourceDirectory;

    /**
     * Output directory
     * 
     * @parameter expression = "${outputDirectory}" default-value="${project.build.directory}/po"
     * @required
     */
    private String outputDirectory;

    /**
     * @parameter expression = "${includes}"
     */
    private String[] includes;

    /**
     * @parameter expression = "${excludes}"
     */
    private String[] excludes;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !new File( resourceDirectory ).exists() )
        {
            getLog().info( "ANDROID-904-001: No resource files to copy" );
            return;
        }

        if ( !new File( outputDirectory ).exists() )
        {
            new File( outputDirectory ).mkdirs();
        }
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( resourceDirectory );

        List<String> excludeList = new ArrayList<String>();
        // target files
        excludeList.add( "target/**" );

        List<String> includeList = new ArrayList<String>();
        includeList.add( "**/*" );

        directoryScanner.setIncludes( includeList.toArray( includes ) );
        for ( int i = 0; i < excludes.length; ++i )
        {
            excludeList.add( excludes[i] );
        }
        directoryScanner.setExcludes( excludeList.toArray( excludes ) );
        directoryScanner.addDefaultExcludes();

        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();
        getLog().info(
                       "ANDROID-904-002: Copying resource files: From = " + resourceDirectory + ",  To = "
                           + outputDirectory + ", File Count = " + files.length );
        for ( String file : files )
        {
            try
            {
                File sourceFile = new File( resourceDirectory, file );
                File targetFile = new File( outputDirectory, file );
                if ( sourceFile.lastModified() > targetFile.lastModified() )
                {
                    FileUtils.copyFile( sourceFile, targetFile );
                    targetFile.setLastModified( System.currentTimeMillis() );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "ANDROID-904-000: Unable to process resources", e );
            }
        }
    }
}
