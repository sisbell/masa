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
package org.jvending.masa.plugin.po;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jvending.masa.plugin.po.parser.PoEntry;

/**
 * @goal transform
 * @requiresDependencyResolution compile
 */
public class PoUnpackagerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;
    
    /**
     * 
     * @parameter expression="${defaultResources}"
     */
    private ArrayList<String> defaultResources;    

    /**
     * @parameter default-value="${project.build.directory}/processed-resources"
     */
    public File resourceDirectory;
    

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts() )
        {
            String type = artifact.getType();
            if ( "android:po".equals( type ) )
            {
                String classifier = artifact.getClassifier();
                if ( classifier == null )
                {
                    throw new MojoExecutionException( "android:po artifacts must have a classifier" );
                }
                boolean isClassifier = defaultResources.contains(classifier );
                File valuesDir = (isClassifier) ? new File( resourceDirectory, "values" ) :
                                new File( resourceDirectory, "values-" + classifier );
                if ( !valuesDir.exists() )
                {
                    valuesDir.mkdirs();
                }
                
                List<Resource> resources = project.getBuild().getResources();
                String resourceDir = (resources.isEmpty()) ? "res" : resources.get(0).getDirectory();
                
                ZipFile zip = null;
                try
                { 
                    zip = new ZipFile( artifact.getFile() );
                    Enumeration<? extends ZipEntry> en = zip.entries();
                    while(en.hasMoreElements())
                    {
                    	ZipEntry entry  = en.nextElement();
                    	if(entry.getName().endsWith(".po")){
                    		writeWithCorrectEncoding(artifact.getFile(), entry.getName(), valuesDir, resourceDir, isClassifier);
                    	}             	
                    }
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException("", e);
                }
 
            }
        }
    }
    
    //TODO: Needs optimization
    private void writeWithCorrectEncoding(File zipFile, String entryName, File valuesDir, String resourceDir, boolean isClassifier) throws IOException
    {
    	String encoding =  null;
    	ZipFile zip = new ZipFile( zipFile );
        BufferedReader in = new BufferedReader(new InputStreamReader( zip.getInputStream( zip.getEntry( entryName) )));
        try {
			String line = in.readLine();

			while(line != null)
			{
				if(line.startsWith("\"Content-Type:"))
				{
					int i = line.indexOf("charset=");
					try {
						encoding = line.substring(i + 8, line.length() -1 ).split(" ")[0];
						this.getLog().info("Encoding found: " + encoding);
					} catch (Exception e) {
					}
					break;
				}
				line = in.readLine();
			}
			
			if(encoding == null)
			{
				encoding = System.getProperty("file.encoding");
			}
			File outputFile = new File( valuesDir, entryName.substring(0, entryName.lastIndexOf(".")) + ".xml" );
			
			PoTransformer.transformToStrings(  new ZipFile( zipFile ).getInputStream( zip.getEntry( entryName ) ) , 
					outputFile, encoding);
			/**
			 * We need to copy default resources to main resource directory. Not ideal but it keeps the Android Eclipse plugin working
			 */
			if(isClassifier)
			{
				FileUtils.copyFileToDirectory(outputFile, new File(resourceDir, "values"));			
			}
		} 
        finally
        {
            if(zip != null)
            {
                try
                {
                    zip.close();
                }
                catch ( IOException e )
                {
                }
            }
            if(in != null)
            {
            	in.close();
            }
        }
    }
}
