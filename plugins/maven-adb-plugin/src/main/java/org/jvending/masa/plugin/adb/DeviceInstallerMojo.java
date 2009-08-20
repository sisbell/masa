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
package org.jvending.masa.plugin.adb;

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
 * @goal install
 * @phase install
 * @description
 */
public final class DeviceInstallerMojo
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

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( System.getProperty( "masa.debug" ) == null )
        {
            getLog().info( "Debug flag not set. Skipping emulator install" );
            return;
        }
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );
        File inputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-signed.apk" );

        List<String> commands = new ArrayList<String>();
        commands.add( "install" );
        commands.add( "-r" );
        commands.add( inputFile.getAbsolutePath() );
        String c = MasaUtil.getToolnameWithPath( session, project, "adb" );
        getLog().info( c + ":" + commands.toString() );
        try
        {
            executor.executeCommand( c, commands );
        }
        catch ( ExecutionException e )
        {
        }
    }
}
