package org.jvending.masa.plugin.po;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jvending.masa.plugin.po.parser.PoEntry;
import org.jvending.masa.plugin.po.parser.PoParser;

public class PoTransformer
{

    public static void transformToStrings( InputStream inputStream, File outputFile )
    throws IOException
{
    if ( inputStream == null )
    {
        throw new IllegalArgumentException( "inputFile: null" );
    }

    if ( outputFile == null )
    {
        throw new IllegalArgumentException( "outputFile: null" );
    }

    List<PoEntry> entries = PoParser.readEntries( inputStream );
    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    try
    {
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter( new FileOutputStream( outputFile ) );
        writer.writeStartDocument( "1.1" );
        writer.writeStartElement( "resources" );
        for ( PoEntry entry : entries )
        {
            writer.writeStartElement( "string" );
            writer.writeAttribute( "name", entry.message.messageContext );
            writer.writeCharacters( entry.message.messageString );
            writer.writeEndElement();
        }

        writer.writeEndElement();
        writer.writeEndDocument();

    }
    catch ( XMLStreamException e )
    {
        throw new IOException( e.getMessage() );
    }
}
    
    public static void transformToStrings( File inputFile, File outputFile )
        throws IOException
    {
        transformToStrings(new FileInputStream(inputFile), outputFile);
    }

    public static void createTemplateFromStringsXml( File inputFile, File outputFile )
        throws IOException
    {
        if ( inputFile == null )
        {
            throw new IllegalArgumentException( "inputFile: null" );
        }

        if ( outputFile == null )
        {
            throw new IllegalArgumentException( "outputFile: null" );
        }
        BufferedWriter bos = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ) ) );

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE );
        xmlInputFactory.setProperty( XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE );

        XMLStreamReader xmlStreamReader = null;
        try
        {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader( new FileInputStream( inputFile ) );

            for ( ;; xmlStreamReader.next() )
            {
                int type = xmlStreamReader.getEventType();
                switch ( type )
                {
                    case XMLStreamConstants.START_ELEMENT:// all strings
                    {
                        if ( xmlStreamReader.getLocalName().equals( "string" ) )
                        {
                            String name = xmlStreamReader.getAttributeValue( 0 );

                            bos.write( "msgctxt \"" );
                            bos.write( name );
                            bos.write( "\"\n" );

                            xmlStreamReader.next();

                            // String c = new String(xmlStreamReader.getElementText());
                            String s = xmlStreamReader.getText();
                            /*
                             * if(s == null || s.trim().length() == 0)//contains html { s = c;
                             * System.out.println("FOLLOW: " + s); }
                             */
                            bos.write( "msgid \"" );
                            bos.write( s );
                            bos.write( "\"\n" );

                            bos.write( "msgstr \"\"\n\n" );
                        }

                        break;
                    }
                    case XMLStreamConstants.END_DOCUMENT:
                    {
                        return;
                    }
                }
            }
        }
        catch ( XMLStreamException e )
        {
            throw new IOException( ":" + e.toString() );
        }
        finally
        {
            if ( xmlStreamReader != null )
            {
                try
                {
                    xmlStreamReader.close();
                }
                catch ( XMLStreamException e )
                {
                    e.printStackTrace();
                }
            }
            if ( bos != null )
            {
                bos.close();
            }
        }
    }
}
