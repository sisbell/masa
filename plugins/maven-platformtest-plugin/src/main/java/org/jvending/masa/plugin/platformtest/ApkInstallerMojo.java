package org.jvending.masa.plugin.platformtest;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.ApkInstaller;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @goal install
 * @phase integration-test
 * @requiresDependencyResolution compile
 * @description
 */
public class ApkInstallerMojo
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
    * @component
    */
    protected RepositorySystem repositorySystem;

   /**
    * @parameter default-value="${repositorySystemSession}"
    */
    protected RepositorySystemSession repositorySystemSession;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String adbPath = MasaUtil.getToolnameWithPath( session, project, "adb" );

        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            if ( ( artifact.getType().equals( "apk" ) ) )
            {
               
                try
                {
                    File mainFile = resolveArtifactFile( artifact);
                    getLog().info( "Installing main apk to device: " + mainFile.getAbsolutePath() );
                    ApkInstaller.install( mainFile, adbPath, getLog() );
                }
                catch ( ExecutionException e )
                {
                    //   throw new MojoExecutionException( "Failed to execute apk install", e );
                }
                catch ( ArtifactResolutionException e )
                {
                    throw new MojoExecutionException("");
                }
            }
        }

        for ( Artifact a : (List<Artifact>) project.getAttachedArtifacts() )
        {
            if ( ( a.getType().equals( "apk" ) || a.getType().equals( "apk-test" ) )
                && "signed-aligned".equals( a.getClassifier() ) )
            {
                try
                {
                    getLog().info( "Installing test apk to device: " + a.getFile().getAbsolutePath() );
                    ApkInstaller.install( a.getFile(), adbPath, getLog() );
                }
                catch ( ExecutionException e )
                {
                    //   throw new MojoExecutionException( "Failed to execute apk install", e );
                }
            }
        }
    }

    private File resolveArtifactFile( Artifact artifact )
        throws ArtifactResolutionException
    {
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setRepositories( project.getRemoteProjectRepositories() );
        artifactRequest.setArtifact( new DefaultArtifact( artifact.getGroupId(),
                                                          artifact.getArtifactId(),
                                                          "signed-aligned",
                                                          artifact.getType(),
                                                          artifact.getVersion()) );
        ArtifactResult artifactResult = repositorySystem.resolveArtifact( repositorySystemSession, artifactRequest );
        return artifactResult.getArtifact().getFile();

    }
}