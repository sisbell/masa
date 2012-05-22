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
package org.jvending.masa.plugins.toolchains;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.model.PersistedToolchains;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.apache.maven.toolchain.model.io.xpp3.MavenToolchainsXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jvending.masa.MasaUtil;

/**
 * @goal toolchain
 * @phase validate
 * @requiresDependencyResolution compile
 */
public class ToolchainMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter
     */
    private Toolchains toolchains;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private File toolchainsFile;

    private static final String sdkErrorMessage = "Android SDK not configured. Either place the android tools directory on classpath or configure toolchains.xml file";

    private static final String versionErrorMessage = "Could not find android version. Check that pom dependency for 'com.android:android:' exists or configure toolchains.xml file";

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        pluginDescriptor.setGroupId( "org.jvending.masa.plugins" );
        pluginDescriptor.setArtifactId( PluginDescriptor.getDefaultPluginArtifactId( "toolchains" ) );

        String dependencyAndroidVersion = getAndroidVersionFromDependency();

        if ( toolchains == null )
        {
            if ( !MasaUtil.isSdkOnPath() )
            {
                throw new MojoExecutionException( sdkErrorMessage );
            }

            getLog().info( "No toolchains.xml configured for this build." );
            if ( dependencyAndroidVersion == null )
            {
                throw new MojoExecutionException( versionErrorMessage );
            }
            session.getPluginContext( pluginDescriptor, project ).put( "androidVersion", dependencyAndroidVersion );

            return;
        }

        if ( toolchainsFile == null )
        {
            //Check if toolchains.xml exists within project directory first
            toolchainsFile = new File( project.getBasedir(), "toolchains.xml" );
            //Check if toolchains.xml exists within local .m2 directory
            if ( !toolchainsFile.exists() )
            {
                toolchainsFile = new File( new File( session.getLocalRepository().getBasedir() ).getParentFile(),
                                           "toolchains.xml" );

            }
        }

        if ( !toolchainsFile.exists() )
        {
            if ( !MasaUtil.isSdkOnPath() )
            {
                throw new MojoExecutionException( sdkErrorMessage );
            }

            getLog().info( "No toolchains.xml file found." );
            if ( dependencyAndroidVersion == null )
            {
                throw new MojoExecutionException( versionErrorMessage );
            }
            session.getPluginContext( pluginDescriptor, project ).put( "androidVersion", dependencyAndroidVersion );
            return;

        }

        PersistedToolchains toolchainModels = null;
        Reader in = null;
        try
        {
            in = ReaderFactory.newXmlReader( toolchainsFile );
            toolchainModels = new MavenToolchainsXpp3Reader().read( in );
        }
        catch ( Exception e )
        {
            getLog().info( "Problem reading toolchains.xml. Could not process" );
            if ( dependencyAndroidVersion == null )
            {
                throw new MojoExecutionException( versionErrorMessage );
            }
            session.getPluginContext( pluginDescriptor, project ).put( "androidVersion", dependencyAndroidVersion );
            return;
        }
        finally
        {
            IOUtil.close( in );
        }

        Map<String, ToolchainModel> models = new HashMap<String, ToolchainModel>();

        // Capabilities
        List<List<Capability>> m = new ArrayList<List<Capability>>();
        for ( ToolchainModel model : (List<ToolchainModel>) toolchainModels.getToolchains() )
        {
            if ( !model.getType().equals( "android" ) )
            {
                continue;
            }

            List<Capability> c = new ArrayList<Capability>();
            Xpp3Dom dom = (Xpp3Dom) model.getProvides();
            for ( Xpp3Dom child : dom.getChildren() )
            {
                if ( child.getName().equals( "id" ) )
                {
                    models.put( child.getValue(), model );
                }
                c.add( new Capability( child.getName(), child.getValue() ) );
            }
            m.add( c );
        }
        Matcher matcher = new Matcher( m );

        // Requirements from pom
        String capabilityId = matcher.findMatchIdFor( toolchains.android );

        if ( capabilityId == null )
        {
            throw new MojoExecutionException( "Could not match capability to toolchain requirements" );
        }

        session.getPluginContext( pluginDescriptor, project ).put( "toolchain", models.get( capabilityId ) );
        session.getPluginContext( pluginDescriptor, project ).put( "androidVersion", getAndroidVersionFromToolchain() );

        getLog().info( "ID = " + capabilityId + " : " + models.get( capabilityId ).getType() + " : " + getAndroidVersionFromToolchain()  );

    }

    private String getAndroidVersionFromToolchain()
    {
        return toolchains.android.get( "version" );
    }

    private String getAndroidVersionFromDependency()
    {
        List<Artifact> artifacts = (List<Artifact>) project.getCompileArtifacts();
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.getGroupId().equals( "com.android" ) && artifact.getArtifactId().equals( "android" ) )
            {
                return artifact.getVersion();
            }
        }
        return null;
    }

}