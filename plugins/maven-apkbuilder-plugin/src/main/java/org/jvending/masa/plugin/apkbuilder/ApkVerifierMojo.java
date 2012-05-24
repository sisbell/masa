package org.jvending.masa.plugin.apkbuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;

/**
 * @goal verify
 * @phase verify
 * @description
 */
public class ApkVerifierMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        List<String> commands = new ArrayList<String>();
        commands.add( "-verify" );

        String apk = null;
        for ( Artifact a : (List<Artifact>) project.getAttachedArtifacts() )
        {
            if ( a.getType().equals( "apk" ) )
            {
                apk = a.getFile().getAbsolutePath();
                break;
            }
        }
        if ( apk == null )
        {
            throw new MojoExecutionException( "Could not find source apk" );
        }

        commands.add( apk );

        this.getLog().info( "jarsigner" + commands.toString() );

        try
        {
            executor.executeCommand( "jarsigner", commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        if ( executor.getResult() == 1 )
        {
            throw new MojoExecutionException( "Unable to validate apk" );
        }
        getLog().info( "Apk is valid");
    }
}
