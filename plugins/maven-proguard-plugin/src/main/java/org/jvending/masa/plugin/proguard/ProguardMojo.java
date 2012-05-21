package org.jvending.masa.plugin.proguard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

/**
 *
 * @goal proguard
 * 
 */
public class ProguardMojo
    extends AbstractMojo
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

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}-small.jar";
     */
    private File outjars;

    /**
     * @parameter default-value="${project.basedir}/proguard.cfg";
     */
    private File configFile;

    /**
     * @parameter
     */
    private boolean skip;

    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Plugin configured to skip proguard for this build" );
            return;
        }

        if ( isSkip() )
        {
            getLog().info( "Property configured to skip proguard for this build" );
            return;
        }

        File inputFile = new File( project.getBuild().getDirectory() + File.separator
            + project.getBuild().getFinalName() + ".jar" );

        File proFile = MasaUtil.getProguardJarFile( project );

        if ( proFile == null )
        {
            getLog().info( "Proguard not configured for this build" );
            return;
        }

        File reportDirectory = new File( project.getBasedir(), "proguard" );
        if ( !reportDirectory.exists() )
        {
            reportDirectory.mkdirs();
        }

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        List<String> commands = new ArrayList<String>();

        //Run proguard jar
        commands.add( "-jar" );
        commands.add( proFile.getAbsolutePath() );

        commands.add( "-injars" );
        commands.add( inputFile.getAbsolutePath() );

        commands.add( "-outjars" );
        commands.add( outjars.getAbsolutePath() );

        commands.add( "-include" );
        commands.add( configFile.getAbsolutePath() );

        //Reporting		
        commands.add( "-printseeds" );
        commands.add( new File( reportDirectory, "seeds.txt" ).getAbsolutePath() );

        commands.add( "-printmapping" );
        commands.add( new File( reportDirectory, "mapping.txt" ).getAbsolutePath() );

        commands.add( "-printusage" );
        commands.add( new File( reportDirectory, "usage.txt" ).getAbsolutePath() );

        commands.add( "-dump" );
        commands.add( new File( reportDirectory, "dump.txt" ).getAbsolutePath() );

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            commands.add( "-libraryjars" );
            commands.add( artifact.getFile().getAbsolutePath() );
        }

        try
        {
            executor.executeCommand( "java", commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }

    private static boolean isSkip()
    {
        return Boolean.getBoolean( "proguard.skip" );
    }
}
