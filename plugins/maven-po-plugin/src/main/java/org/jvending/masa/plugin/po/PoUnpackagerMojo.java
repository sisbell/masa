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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.plugin.po.parser.PoEntry;

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
                String encoding = null;
                try
                {   //READ meta-data for encoding
                    zip = new ZipFile( artifact.getFile() );
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader( zip.getInputStream( zip.getEntry( "strings.po" ) )));
                    String line = in.readLine();

                    while(line != null)
                    {
                    	if(line.startsWith("\"Content-Type:"))
                    	{
                    		int i = line.indexOf("charset=");
                    		try {
								encoding = line.substring(i + 8, line.length()).split(" ")[0];
								this.getLog().info("Encoding found: " + encoding);
							} catch (Exception e) {
							}
							break;
                    	}
                    	line = in.readLine();
                    }
                    
                    if(encoding == null)
                    {
                    	encoding = System.getProperty("file.encoding");
                    }
                    
                    PoTransformer.transformToStrings(  new ZipFile( artifact.getFile() ).getInputStream( zip.getEntry( "strings.po" ) ) , new File( valuesDir, "strings.xml" ), encoding);
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
