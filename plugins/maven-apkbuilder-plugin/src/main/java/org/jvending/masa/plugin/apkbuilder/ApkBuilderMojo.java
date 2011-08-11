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
package org.jvending.masa.plugin.apkbuilder;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

import com.android.sdklib.build.ApkBuilder;
import com.android.sdklib.build.ApkCreationException;
import com.android.sdklib.build.SealedApkException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal build
 * @phase package
 * @description
 */
public class ApkBuilderMojo
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
     * Maven ProjectHelper.
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        File outputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-unsigned.apk" );

        File packagedResourceFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );
        File dexFile = new File( project.getBuild().getDirectory(), "classes.dex" );
        File resourcesDir = new File( project.getBuild().getSourceDirectory() ) ;
		try {
			ApkBuilder builder = new ApkBuilder(outputFile.getAbsolutePath(),
					packagedResourceFile.getAbsolutePath(), dexFile.getAbsolutePath(), null,
					null);
			//builder.
			//builder.addZipFile(zipFile);
			builder.sealApk();
		} catch (ApkCreationException e) {
			e.printStackTrace();
			throw new MojoExecutionException( "ApkCreationException", e );
		} catch (SealedApkException e) {
			e.printStackTrace();
			throw new MojoExecutionException( "SealedApkException", e );
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException( "", e );
		}
  
        /*
        List<String> commands = new ArrayList<String>();
        commands.add( outputFile.getAbsolutePath() );
        commands.add( "-u" );//unsigned

        commands.add( "-z" );//add zip
        commands.add( new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" ).getAbsolutePath() );
        commands.add( "-f" );//add dex
        commands.add( new File( project.getBuild().getDirectory(), "classes.dex" ).getAbsolutePath() );
        commands.add( "-rf" );//resources
        commands.add( new File( project.getBuild().getSourceDirectory() ).getAbsolutePath() );

        
        getLog().info( "apkbuilder " + commands.toString() );
        try
        {
            executor.executeCommand( MasaUtil.getToolnameWithPath( session, project, "apkbuilder" ), commands,
                                     project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }
*/
        projectHelper.attachArtifact( project, "apk", "unsigned", outputFile );
    }
}
