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

import com.android.sdklib.build.ApkBuilder;
import com.android.sdklib.build.ApkCreationException;
import com.android.sdklib.build.SealedApkException;

import java.io.*;

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
    
    /**
     * Sets the debug mode. In debug mode, when native libraries are present, the packaging
     * will also include one or more copies of gdbserver in the final APK file.
     *
     * These are used for debugging native code, to ensure that gdbserver is accessible to the
     * application.
     *
     * There will be one version of gdbserver for each ABI supported by the application.
     *
     * the gbdserver files are placed in the libs/abi/ folders automatically by the NDK.
     *
     * @parameter
     */
    private boolean debugMode;
    
    /*
     * Root folder containing native libraries to include in the application package.
     * 
     * @parameter
     */
    private NativeLibraries nativeLibraries;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File outputFile =
            new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-unsigned.apk" );

        File packagedResourceFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".ap_" );
        File dexFile = new File( project.getBuild().getDirectory(), "classes.dex" );
       // File resourcesDir = new File( project.getBuild().getSourceDirectory() ) ;
		try {
			ApkBuilder builder = new ApkBuilder(outputFile.getAbsolutePath(),
					packagedResourceFile.getAbsolutePath(), dexFile.getAbsolutePath(), null,
					null);
			if(debugMode) {
				builder.setDebugMode(true);
			}	
			if(nativeLibraries != null) {
				builder.addNativeLibraries(nativeLibraries.path, nativeLibraries.abiFilter);
			}
			
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
  
        projectHelper.attachArtifact( project, "apk", "unsigned", outputFile );
    }
}
