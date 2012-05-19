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
package org.jvending.masa.plugin.aapt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

/**
 * @goal lib-package
 * @phase package
 * @description
 */
public final class LibraryPackagerMojo
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
    public String[] sourceRoots;

    /**
     * Maven ProjectHelper.
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File targetDirectory = new File( project.getBuild().getDirectory() );
        if ( !targetDirectory.exists() )
        {
            targetDirectory.mkdirs();
        }

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( getLog() );

        File outputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
            + "-resources.jar" );

        List<String> commands = new ArrayList<String>();
        commands.add( "cMf" );
        commands.add( outputFile.getAbsolutePath() );

        commands.add( "-C" );
        commands.add( project.getBasedir().getAbsolutePath() );

        //   if ( resourceDirectory.exists() )
        //  {       	
        //     commands.add( resourceDirectory.getName());
        // } else {
        commands.add( new File( project.getBasedir(), "res" ).getName() );
        // }

        if ( assetsDirectory.exists() )
        {
            commands.add( assetsDirectory.getName() );
        }

        String command = MasaUtil.getToolnameWithPath( session, project, "jar" );
        getLog().info( command + " " + commands.toString() );
        try
        {
            executor.executeCommand( command, commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        projectHelper.attachArtifact( project, outputFile, "resources" );
    }
}
