package org.jvending.masa.plugin.lint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

/**
 *
 * @goal verify
 * 
 * @phase process-sources
 */
public class LintMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * @parameter expression="${session}
     */
    public MavenSession session;

    /**
     * @parameter
     */
    public File config;

    /**
     * @parameter
     */
    public String disable;

    /**
     * @parameter
     */
    public String enable;

    /**
     * @parameter
     */
    public String check;

    /**
     * @parameter default-value="false"
     */
    public boolean disableConfigFile;

    /**
     * @parameter default-value="true"
     */
    public boolean generateHtml;

    /**
     * @parameter default-value="false"
     */
    public boolean generateXml;

    /**
     * @parameter default-value="false"
     */
    public boolean noWarn;

    /**
     * @parameter default-value="true"
     */
    public boolean quiet;

    /**
     * @parameter default-value="false"
     */
    public boolean noLines;

    /**
     * @parameter default-value="false"
     */
    public boolean fullPath;

    private File target;

    public void execute()
        throws MojoExecutionException
    {

        if ( !generateHtml && !generateXml )
        {
            throw new MojoExecutionException(
                                              "Must specify generateXml and/or generateHtml parameters as true. Check your plugin configuration." );
        }

        target = new File( project.getBuild().getDirectory(), "lint" );
        if ( !target.exists() )
        {
            target.mkdirs();
        }

        /*
        try
        {
            copyResourceFilesToTargetDir();
            copySourceFilesToTargetDir();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy source files to target directory.", e );
        }
*/
        if ( generateHtml )
        {
            List<String> commands = new ArrayList<String>();
            commands.add( "--html" );
            commands.add( new File( target, "index.html" ).getAbsolutePath() );
            executeLint( commands );
        }

        if ( generateXml )
        {
            List<String> commands = new ArrayList<String>();
            commands.add( "--xml" );
            commands.add( new File( target, "lint-report.xml" ).getAbsolutePath() );
            executeLint( commands );
        }
    }

    private void executeLint( Collection<String> args )
        throws MojoExecutionException
    {
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( getLog() );

        List<String> commands = new ArrayList<String>();

        if ( getLintFile().exists() && !disableConfigFile )
        {
            commands.add( "--config" );
            commands.add( getLintFile().getAbsolutePath() );
        }

        if ( noLines )
        {
            commands.add( "--nolines" );
        }

        if ( quiet )
        {
            commands.add( "--quiet" );
        }

        if ( noWarn )
        {
            commands.add( "--nowarn" );
        }

        if ( fullPath )
        {
            commands.add( "--fullpath" );
        }

        if ( disable != null )
        {
            commands.add( "--disable" );
            commands.add( disable );
        }

        if ( enable != null )
        {
            commands.add( "--enable" );
            commands.add( enable );
        }

        if ( check != null )
        {
            commands.add( "--check" );
            commands.add( check );
        }

        commands.addAll( args );
        commands.add( project.getBasedir().getAbsolutePath() );
        
        //commands.add( target.getAbsolutePath() );

        String lintCommand = MasaUtil.getToolnameWithPath( session, project, "lint" );
        getLog().info( lintCommand + ":" + commands.toString() );
        try
        {
            executor.executeCommand( lintCommand, commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }

    private File getLintFile()
    {
        return ( config != null && config.exists() ) ? config : new File( project.getBasedir(), "lint.xml" );
    }

    private void copyResourceFilesToTargetDir()
        throws IOException
    {
        for ( Resource res : (List<Resource>) project.getResources() )
        {
            File resDir = new File( res.getDirectory() );
            FileUtils.copyDirectory( resDir, new File( target, resDir.getName() ) );
        }
        
        File manifest = new File(project.getBasedir(), "AndroidManifest.xml");
        FileUtils.copyFileToDirectory( manifest, target );
    }

    private void copySourceFilesToTargetDir()
        throws IOException
    {
        for ( String src : (List<String>) project.getCompileSourceRoots() )
        {
            File srcDir = new File( src );
            FileUtils.copyDirectory( srcDir, new File( target, srcDir.getName() ) );
        }
        
        File srcDir = new File( project.getBuild().getDirectory(), "classes" );
        FileUtils.copyDirectoryStructure( srcDir, new File( target, "classes" ) );
       
        srcDir = new File( project.getBuild().getDirectory(), "gen-mvn" );
        FileUtils.copyDirectoryStructure( srcDir, new File( target, "classes" ) );        
    }
}
