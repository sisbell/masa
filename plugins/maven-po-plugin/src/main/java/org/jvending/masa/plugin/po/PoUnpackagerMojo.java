package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal transform
 * @requiresDependencyResolution compile
 */
public class PoUnpackagerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;
    
    /**
     * 
     * @parameter expression="${defaultResource}"
     */
    private String defaultResource;    

    /**
     * @parameter default-value="${project.build.directory}/processed-resources"
     */
    public File resourceDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            String type = artifact.getType();
            if ( "android:po".equals( type ) )
            {
                String classifier = artifact.getClassifier();
                if ( classifier == null )
                {
                    throw new MojoExecutionException( "android:po artifacts must have a classifier" );
                }

                File valuesDir = (defaultResource.equals( classifier ) ) ? new File( resourceDirectory, "values" ) :
                                new File( resourceDirectory, "values-" + classifier );
                if ( !valuesDir.exists() )
                {
                    valuesDir.mkdirs();
                }

                ZipFile zip = null;
                try
                {
                    zip = new ZipFile( artifact.getFile() );
                    PoTransformer.transformToStrings( zip.getInputStream( zip.getEntry( "strings.po" ) ),
                                                      new File( valuesDir, "strings.xml" ) );
                }
                catch ( ZipException e )
                {
                    throw new MojoExecutionException("", e);  
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException("", e);
                }
                finally
                {
                    if(zip != null)
                    {
                        try
                        {
                            zip.close();
                        }
                        catch ( IOException e )
                        {
                        }
                    }
                }
            }
        }
    }
}
