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
 * @goal package
 * @phase package
 * @description
 */
public final class AaptPackagerMojo
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
        executor.setLogger( getLog() );

        if ( androidManifestFile == null )
        {
            androidManifestFile = new File( resourceDirectory.getParent(), "AndroidManifest.xml" );
        }

        File androidJar = MasaUtil.getAndroidJarFile( project );
        File outputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );

        List<String> commands = new ArrayList<String>();
        commands.add( "package" );

        commands.add( "-f" );
        commands.add( "-M" );
        commands.add( androidManifestFile.getAbsolutePath() );
        if ( resourceDirectory.exists() )
        {
            commands.add( "-S" );
            commands.add( resourceDirectory.getAbsolutePath() );
        }
        if ( assetsDirectory.exists() )
        {
            commands.add( "-A" );
            commands.add( assetsDirectory.getAbsolutePath() );
        }
        commands.add( "-I" );
        commands.add( androidJar.getAbsolutePath() );
        commands.add( "-F" );
        commands.add( outputFile.getAbsolutePath() );

        String aaptCommand = MasaUtil.getToolnameWithPath( session, project, "aapt" );
        getLog().info( aaptCommand + " " + commands.toString() );
        try
        {
            executor.executeCommand( aaptCommand, commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
