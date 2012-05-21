package org.jvending.masa.plugin.localization;

/*
 * Copyright (C) 2007-2008 JVending Masa
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @goal copy
 * 
 */
public class CopyResourcesMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * Location of the localized resources
     * 
     * @parameter default-value="${project.basedir}/localized-resources" 
     */
    private File resourcesDirectory;

    /**
     * Location of the output directory for the localized resources.
     * 
     * @parameter default-value ="${project.basedir}/res"
     */
    private File outputDirectory;

    /**
     * Locales of resources to copy
     * 
     * @parameter
     */
    private List<String> locales;

    public void execute()
        throws MojoExecutionException
    {
        if ( !resourcesDirectory.exists() )
        {
            getLog().info( "No localized-resources directory found: File = " + resourcesDirectory.getAbsolutePath() );
            return;
        }

        if ( locales == null )
        {
            getLog().info( "No localized-resources found." );
            return;
        }

        if ( project.getBuild().getResources().size() == 0 )
        {
            return;
        }

        for ( String locale : locales )
        {
            File res = new File( resourcesDirectory, "res-" + locale );
            if ( res.exists() )
            {
                DirectoryScanner directoryScanner = new DirectoryScanner();
                directoryScanner.setBasedir( res );
                directoryScanner.addDefaultExcludes();

                directoryScanner.scan();
                String[] files = directoryScanner.getIncludedFiles();
                getLog().info( "ANDROID-904-002: Copying resource files: From = " + resourcesDirectory + ",  To = "
                                   + outputDirectory + ", File Count = " + files.length );
                for ( String file : files )
                {
                    try
                    {
                        File sourceFile = new File( res, file );
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
                /*
                try
                {
                    FileUtils.copyDirectory( res, outputDirectory );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException("Failed to copy resource: File = " + res.getAbsolutePath(), e);
                }
                getLog().info( "Copied resource. File = " + res.getName() );
                */
            }
        }
    }
}
