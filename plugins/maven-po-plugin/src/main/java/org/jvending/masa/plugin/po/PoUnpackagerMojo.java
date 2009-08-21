/*
 * Copyright (C) 2007-2008 JVending Masa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
