package org.jvending.masa.plugin.zipalign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;

/**
 *
 * @goal align
 * 
 */
public class ZipAlignMojo
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
     * @parameter default-value="${project.attachedArtifacts}
     * @required
     * @readonly
     */
    private List<Artifact> attachedArtifacts;

    /**
     * Maven ProjectHelper.
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter
     */
    private boolean isVerbose;

    /**
     * @parameter default-value="4"
     */
    private int align;

    public void execute()
        throws MojoExecutionException
    {
        File alignedOutputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
            + "-signed-aligned.apk" );

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        List<String> commands = new ArrayList<String>();

        if ( isVerbose )
        {
            commands.add( "-v" );
        }
        commands.add( "-f" );

        commands.add( String.valueOf( align ) );
        for ( Artifact artifact : attachedArtifacts )
        {
            if ( "signed".equals( artifact.getClassifier() ) )
            {
                commands.add( artifact.getFile().getAbsolutePath() );
                //Output file

                commands.add( alignedOutputFile.getAbsolutePath() );
                break;
            }
        }
        getLog().info( "Zipalign: " + commands.toString() );
        try
        {
            executor.executeCommand( "zipalign", commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        projectHelper.attachArtifact( project, "apk", "signed-aligned", alignedOutputFile );

    }
}
