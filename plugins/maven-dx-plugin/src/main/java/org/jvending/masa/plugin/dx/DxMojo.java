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
package org.jvending.masa.plugin.dx;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.IOUtil;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @goal dx
 * @phase process-classes
 * @description
 */
public class DxMojo
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
     * @component
     */
    private MavenProjectHelper mavenProjectHelper;

    /**
     * Extra JVM Arguments
     * 
     * @parameter
     * @optional
     */
    private String[] jvmArguments;
    
    /**
     * none, important, lines (debug info)
     * 
     * @parameter
     * @optional
     */
    private String positions;

    /**
     * debug
     * 
     * @parameter
     * @optional
     */   
    private boolean debug;
    
    /**
     * @parameter
     * @optional
     */   
    private boolean noOptimize;    
    
    /**
     * @parameter
     * @optional
     */   
    private boolean statistics;    
    
    /**
     * @parameter
     * @optional
     */   
    private boolean noLocales;   
    
    /**
     * @parameter
     * @optional
     */   
    private boolean verbose;    
    
    /**
     * @parameter
     * @optional
     */   
    private boolean noStrict;     
    
    
    /**
     * @parameter
     * @optional
     */   
    private boolean keepClasses;     
    
    /**
     * @parameter
     * @optional
     */   
    private boolean noFiles;  
    
    /**
     * @parameter
     * @optional
     */   
    private boolean coreLibrary;  
    
    /**
     * @parameter
     * @optional
     */   
    private File dumpTo;        
    
    /**
     * @parameter
     * @optional
     */   
    private int dumpWidth;      
    
    /**
     * New line separated file 
     * 
     * Class name + method names that should be optimized
     * 
     * http://www.java2s.com/Open-Source/Android/android-core/platform-dalvik/com/android/dx/dex/cf/OptimizerOptions.java.htm
     * 
     * @parameter
     * @optional
     */   
    private File optimizeList;     
   
    
    /**
     * New line separated file 
     * 
     * Class name + method names that should not be optimized
     * 
     * @parameter
     * @optional
     */   
    private File noOptimizeList; 

    /**
     * @parameter
     * @optional
     */   
    private int numThreads;
    
    /**
     * @parameter
     * @optional
     */   
    private File classFile;   
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        File outputFile = new File( project.getBuild().getDirectory() + File.separator + "classes.dex" );
        
        //These classes files go from jar into temp output directory (android-classes)
        File inputFile =
            new File( project.getBuild().getDirectory() + File.separator + project.getBuild().getFinalName() + ".jar" );

        // Unpackage all dependent and main classes into this directory
        File classDirectory = new File( project.getBuild().getDirectory(), "android-classes" );
        for ( Artifact artifact : (List<Artifact>) project.getCompileArtifacts() )
        {
            if ( artifact.getGroupId().equals( "com.google.android" ) )
            {
                continue;
            }

            if ( artifact.getFile().isDirectory() )
            {
                throw new MojoExecutionException( "Dependent artifact is directory: Directory = "
                    + artifact.getFile().getAbsolutePath() );
            }

            try
            {
                unjar( new JarFile( artifact.getFile() ), classDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to jar file: File = " + artifact.getFile().getAbsolutePath(),
                                                  e );
            }
        }

        try
        {
            unjar( new JarFile( inputFile ), classDirectory );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "", e );
        }

        List<String> commands = new ArrayList<String>();
        if ( jvmArguments != null )
        {
            for ( String jvmArgument : jvmArguments )
            {
                if ( jvmArgument != null )
                {
                    if ( jvmArgument.startsWith( "-" ) )
                    {
                        jvmArgument = jvmArgument.substring( 1 );
                    }
                    commands.add( "-J" + jvmArgument );
                }
            }
        }
		if (positions != null) 
		{
			if (positions.equals("none") || positions.equals("important")
					|| positions.equals("lines")) 
			{
				commands.add( "--positions=" + positions);			
			} 
			else 
			{
				throw new MojoExecutionException(
						"Unknown positions parameter (none, important, lines): Value = "
								+ positions);
			}
		}
		if(debug) 
		{
			commands.add( "--debug" );
		}
		
		if(noOptimize) 
		{
			commands.add( "--no-optimize" );
		}

		if(statistics) 
		{
			commands.add( "--statistics" );
		}

		if(noLocales) 
		{
			commands.add( "--no-locals" );
		}
		
		if(noOptimize) 
		{
			commands.add( "--no-optimize" );
		}

		if(verbose) 
		{
			commands.add( "--verbose" );
		}

		if(noStrict) 
		{
			commands.add( "--no-strict" );
		}

		if(keepClasses) 
		{
			commands.add( "--keep-classes" );
		}

		if(noFiles) 
		{
			commands.add( "--no-files" );
		}

		if(coreLibrary) 
		{
			commands.add( "--core-library" );
		}
		
		if(dumpTo != null) 
		{
			if(!dumpTo.getParentFile().exists()) 
			{
				if(!dumpTo.getParentFile().mkdirs())
				{
					throw new MojoExecutionException( "Failed to create dump directory: Directory = "  
							+ dumpTo.getParentFile().getAbsolutePath() );
				} else {
					commands.add( "--dump-to" );
					commands.add( dumpTo.getAbsolutePath() );
				}
			}
		}
		
		if( dumpWidth != 0 ) 
		{
			commands.add( "--dump-width" );
			commands.add( String.valueOf( dumpWidth ) );
		}
		
		if( optimizeList != null ) 
		{
			commands.add( "--optimize-list" );
			commands.add( optimizeList.getAbsolutePath() );			
		}
		
		if( noOptimizeList != null ) 
		{
			commands.add( "--no-optimize-list" );
			commands.add( noOptimizeList.getAbsolutePath() );			
		}	
		
		if(numThreads != 0) {
			commands.add( "--num-threads" );
			commands.add( String.valueOf( numThreads ) );			
		}
		
        commands.add( "--dex" );
        commands.add( "--output=" + outputFile.getAbsolutePath() );//classes.dex
        
        if(classFile == null) 
        {
        	commands.add( classDirectory.getAbsolutePath() );	
        } 
        else 
        {
        	if( !classFile.exists() )
        	{
        		throw new MojoExecutionException( "Class source directory not found: " + classFile.getAbsolutePath() ); 	     
        	}
        	String className = classFile.getName();
        	if(classFile.isDirectory() || className.endsWith(".apk") || className.endsWith(".jar") || className.endsWith(".zip") ) 
        	{
        		commands.add( classFile.getAbsolutePath() );
        	} else {
        		throw new MojoExecutionException( "Unrecognized class source (apk, zip, jar): " + classFile.getAbsolutePath());
        	}
        }
        
        getLog().info( "dx " + commands.toString() );
        try
        {
            executor.executeCommand( MasaUtil.getToolnameWithPath( session, project, "dx" ), commands,
                                     project.getBasedir(), false );
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "", e );
        }

        mavenProjectHelper.attachArtifact( project, "jar", project.getArtifact().getClassifier(), inputFile );
    }

    private void unjar( JarFile jarFile, File outputDirectory )
        throws IOException
    {
        for ( Enumeration en = jarFile.entries(); en.hasMoreElements(); )
        {
            JarEntry entry = (JarEntry) en.nextElement();
            File entryFile = new File( outputDirectory, entry.getName() );
            if ( !entryFile.getParentFile().exists() && !entry.getName().startsWith( "META-INF" ) )
            {
                entryFile.getParentFile().mkdirs();
            }
            if ( !entry.isDirectory() && entry.getName().endsWith( ".class" ) )
            {
                final InputStream in = jarFile.getInputStream( entry );
                try
                {
                    final OutputStream out = new FileOutputStream( entryFile );
                    try
                    {

                        IOUtil.copy( in, out );
                    }
                    finally
                    {
                        closeQuietly( out );
                    }
                }
                finally
                {
                    closeQuietly( in );
                }

            }
        }
    }

    private void closeQuietly( final Closeable c )
    {
        try
        {
            c.close();
        }
        catch ( Exception ex )
        {
            getLog().warn( "Failed to close closeable " + c, ex );
        }
    }
}
