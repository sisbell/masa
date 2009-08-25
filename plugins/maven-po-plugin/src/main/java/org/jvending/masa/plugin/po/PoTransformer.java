
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.maven.project.MavenProject;
import org.jvending.masa.plugin.po.parser.PoEntry;
import org.jvending.masa.plugin.po.parser.PoParser;

public class PoTransformer
{
	public static void writeEntriesToStringsFile(List<PoEntry> entries, File outputFile)
		throws IOException
	{
	    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	    try
	    {
	        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter( new FileOutputStream( outputFile ) );
	        writer.writeStartDocument( "1.1" );
	        writer.writeStartElement( "resources" );
	        for ( PoEntry entry : entries )
	        {
	        	if(entry.message.messageContext == null)
	        	{
	        		continue;
	        	}
	            writer.writeStartElement( "string" );
	            writer.writeAttribute( "name", entry.message.messageContext );
	            if(entry.message.messageString != null) 
	            {
	            	writer.writeCharacters( entry.message.messageString ); 
	            }
	            else 
	            {
	            	writer.writeCharacters( "");
	            }
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
	
    public static void transformToStrings( InputStream inputStream, File outputFile, String encoding )
    	throws IOException
	{
	    if ( inputStream == null )
	    {
	        throw new IllegalArgumentException( "inputStream: null" );
	    }
	
	    if ( outputFile == null )
	    {
	        throw new IllegalArgumentException( "outputFile: null" );
	    }
	
	    List<PoEntry> entries = PoParser.readEntries( inputStream, encoding );
	    writeEntriesToStringsFile(entries, outputFile);
	}
    
    public static void transformToStrings( File inputFile, File outputFile, String encoding )
        throws IOException
    {
        transformToStrings(new FileInputStream(inputFile), outputFile, encoding);
    }
    
    private static boolean verifyHeaders(List<String> headers)
    {
    	for(String header : headers)
    	{
    		System.out.println(headers);
    		if(!header.trim().equals(""))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void writePoFile(List<PoEntry> entries, File outputFile, String encoding)
    	throws IOException 
    {
    	BufferedWriter bos = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ), encoding) );
    	
    	for(PoEntry po : entries)
    	{
    		if(po.hasHeaders && verifyHeaders(po.headers))
    		{
                bos.write( "msgid \"\"\n" );
                bos.write( "msgstr \"\"\n" );
                for(String header : po.headers)
                {
                	if(!header.trim().equals(""))
                	{
                    	bos.write(header);    
                    	bos.write("\n");
                	}
                }
                bos.write("\n");
                continue;
    		}
    		
    		if(po.message.messageContext != null)
    		{
                bos.write( "msgctxt \"" );
                bos.write( po.message.messageContext );
                bos.write( "\"\n" );   			
    		}

            bos.write( "msgid \"" );
            if( po.message.messageId != null) 
            {
            	bos.write( po.message.messageId );
            }
            bos.write( "\"\n" );

            bos.write( "msgstr \"" ); 
            if(po.message.messageString != null)
            	bos.write( po.message.messageString );
            bos.write( "\"\n" );
            
            bos.write( "\n" );
    	}
    	bos.close();
    }

    public static String createTemplateFromStringsXml( File inputFile, File outputFile, MavenProject project )
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
        BufferedWriter bos = null;

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE );
        xmlInputFactory.setProperty( XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE );

        XMLStreamReader xmlStreamReader = null;
        try
        {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader( new FileInputStream( inputFile ) );
            
        	String encoding = xmlStreamReader.getEncoding();
            bos = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ), encoding) );
            //Headers
            bos.write( "msgid \"\"\n" );
            bos.write( "msgstr \"\"\n" );
            if(project != null)
            {
            	bos.write("\"Project-Id-Version:" + project.getArtifactId() + "-" + project.getVersion() +"\"\n");	
            	Calendar c = Calendar.getInstance();
            	c.setTimeInMillis(System.currentTimeMillis());
            	bos.write("\"PO-Revision-Date:" + DateFormat.getDateInstance().format(c.getTime()) +"\"\n");	
            }
            bos.write("\"Content-Type: text/plain; charset=" + encoding +"\"\n");
            bos.write( "\n" );
            
            for ( ;; xmlStreamReader.next() )
            {
                int type = xmlStreamReader.getEventType();
                switch ( type )
                {
                    case XMLStreamConstants.START_ELEMENT:// all strings
                    {
                        if ( xmlStreamReader.getLocalName().equals( "string" ) )
                        {
                            bos.write( "msgctxt \"" );
                            bos.write( xmlStreamReader.getAttributeValue( 0 ) );
                            bos.write( "\"\n" );

                            xmlStreamReader.next();
                            if(xmlStreamReader.getEventType() == XMLStreamConstants.CHARACTERS)
                            { 
                                String s = nodeToString(xmlStreamReader);

                                bos.write( "msgid " );
                              	BufferedReader reader = new BufferedReader(new StringReader(s));
                            	String line = reader.readLine();
                            	while(line != null)
                            	{
                            		if(!line.trim().equals(""))
                            		{
                                        bos.write( "\"" );
                                        bos.write( line );
                                        bos.write( "\"\n" );                       			
                            		}

                                    line = reader.readLine();
                            	}
                                bos.write( "msgstr \"\"\n\n" );    
                            }
                        }
                        break;
                    } 
                    case XMLStreamConstants.END_DOCUMENT:
                    {
                        return encoding;
                    }
                }
            }
        }
        catch ( XMLStreamException e )
        {
            throw new IOException("File = " + inputFile.getAbsolutePath() +"\n" + e.toString() );
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
    private static String nodeToString(XMLStreamReader xmlStreamReader) throws XMLStreamException
    {
    	StringBuilder b = new StringBuilder();
    	   for ( ;; xmlStreamReader.next() )
           {
               int type = xmlStreamReader.getEventType();

               switch ( type )
               {
                   case XMLStreamConstants.START_ELEMENT:
                   {
                	   b.append("<").append(xmlStreamReader.getLocalName()).append(">");
                	   break;
                   }
                   case XMLStreamConstants.END_ELEMENT:
                   {
                	   if(xmlStreamReader.getLocalName().equals("string")) 
                	   {
                		   //System.out.print(b.toString());
                		   return b.toString();
                	   }
                	   b.append("</").append(xmlStreamReader.getLocalName()).append(">");
                	   break;
                   }
                   case XMLStreamConstants.CHARACTERS:
                   {
                      b.append(xmlStreamReader.getText());
                      break;
                   }                   
               }
           }  	   
    }
}
