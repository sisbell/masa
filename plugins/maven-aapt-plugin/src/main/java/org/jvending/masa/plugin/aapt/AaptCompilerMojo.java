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
package org.jvending.masa.plugin.aapt;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 * @description
 */
public class AaptCompilerMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value=true
     */
    private boolean createPackageDirectories;

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
     * @parameter default-value="${project.build.directory}/processed-resources"
     */
    public File resourceDirectory;

    /**
     * @parameter default-value="assets"
     */
    public File assetsDirectory;

    /**
     * @parameter
     */
    public File androidManifestFile;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        // Get rid of this annoying Thumbs.db problem on windows

        File thumbs = new File( resourceDirectory, "drawable/Thumbs.db" );
        if ( thumbs.exists() )
        {
            getLog().info( "Deleting thumbs.db from resource directory" );
            thumbs.delete();
        }

        if ( androidManifestFile == null )
        {
            androidManifestFile = new File(  project.getBasedir(), "AndroidManifest.xml" );
        }
        
        if ( !androidManifestFile.exists() )
        {
            throw new MojoExecutionException( "Android manifest file not found: File = "
                + androidManifestFile.getAbsolutePath() );
        }
        
        String generatedSourceDirectoryName = project.getBasedir() + File.separator + "gen";
        new File( generatedSourceDirectoryName ).mkdirs();

        File androidJar = MasaUtil.getAndroidJarFile( project );
        if ( !androidJar.exists() )
        {
            throw new MojoExecutionException( "Android jar file not found: File = " + androidJar.getAbsolutePath() );
        }
        
        List<String> commands = new ArrayList<String>();
        commands.add( "package" );

        if ( createPackageDirectories )
        {
            commands.add( "-m" );
        }
        commands.add( "-J" );
        commands.add( generatedSourceDirectoryName );
        commands.add( "-M" );
        commands.add( androidManifestFile.getAbsolutePath() );
        
    	commands.add( "-S" );
        if ( resourceDirectory.exists() )
        {
            commands.add( resourceDirectory.getAbsolutePath() );
        } else {
        	commands.add(  new File(  project.getBasedir(), "res" ).getAbsolutePath());
        }
        
        if ( assetsDirectory.exists() )
        {
            commands.add( "-A" );
            commands.add( assetsDirectory.getAbsolutePath() );
        }
        commands.add( "-I" );
        commands.add( androidJar.getAbsolutePath() );

        String apptCommand = MasaUtil.getToolnameWithPath( session, project, "aapt" );
        getLog().info( apptCommand + ":" + commands.toString() );
        try
        {
            executor.executeCommand( apptCommand, commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        project.addCompileSourceRoot( generatedSourceDirectoryName );

        // if(System.getProperty("masa.debug") != null &&
        // platformUnitTestDirectory.exists())
        // {
        // project.addCompileSourceRoot(platformUnitTestDirectory.getAbsolutePath());
        // }
    }

}
