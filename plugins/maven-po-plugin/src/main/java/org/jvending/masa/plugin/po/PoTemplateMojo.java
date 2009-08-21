package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal generate
 * @requiresProject false
 * @description
 */
public class PoTemplateMojo
    extends AbstractMojo
{

    /**
     * @required
     */
    private File inputFile;

    /**
     * @required
     */
    private File outputFile;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            PoTransformer.createTemplateFromStringsXml( inputFile, outputFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "", e );
        }
    }
}
