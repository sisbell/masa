package org.jvending.masa.plugin.jarsigner;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;

/**
 * @goal sign
 * @phase package
 */
public class JarSignerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * Maven ProjectHelper.
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${keystore}"
     */
    public String keystore;

    /**
     * @parameter expression="${alias}" default-value="androiddebugkey"
     */
    public String alias;

    /**
     * @parameter expression="${storepass}" default-value="android"
     */
    public String storepass;

    /**
     * @parameter expression="${keypass}" default-value="android"
     */
    public String keypass;

    /**
     * @parameter expression="${disableStorepass}" default-value="false"
     */
    public boolean disableStorepass;

    /**
     * @parameter expression="${disableKeypass}" default-value="false"
     */
    public boolean disableKeypass;

    public void execute()
        throws MojoExecutionException
    {
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        if ( keystore == null )
        {
            String home = System.getProperty( "user.home" );
            File f = new File( home, ".android/debug.keystore" );
            if ( f.exists() )
            {
                keystore = f.getAbsolutePath();
            }
            else
            {
                f = new File( home, "Local Settings\\Application Data\\Android\\debug.keystore" );
                if ( !f.exists() )
                {
                    throw new MojoExecutionException(
                                                      "Keystore not specificed and could not locate default debug.keystore" );
                }
            }
        }
        List<String> commands = new ArrayList<String>();
        // commands.add("-verbose");
        if ( !disableKeypass )
        {
            commands.add( "-keypass" );
            commands.add( keypass );
        }

        if ( !disableStorepass )
        {
            commands.add( "-storepass" );
            commands.add( storepass );
        }

        commands.add( "-keystore" );
        commands.add( keystore );

        String apk = null;
        for ( Artifact a : (List<Artifact>) project.getAttachedArtifacts() )
        {
            if ( a.getType().equals( "apk" ) )
            {
                apk = a.getFile().getAbsolutePath();
                break;
            }
        }
        if ( apk == null )
        {
            throw new MojoExecutionException( "Could not find source apk" );
        }
        File outputFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
            + "-signed.apk" );
        commands.add( "-signedjar" );
        commands.add( outputFile.getAbsolutePath() );

        commands.add( apk );
        commands.add( alias );

        this.getLog().info( "jarsigner" + commands.toString() );

        try
        {
            executor.executeCommand( "jarsigner", commands, project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        projectHelper.attachArtifact( project, "apk", "signed", outputFile );
    }
}
