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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class PoTransformerTest
{

    private File basedir;

    private File resourceDir;

    @Before
    public void setup()
    {
        resourceDir = new File( "src/test/resources" );
    }

    @Test
    public void transform()
        throws IOException, ParserConfigurationException, SAXException
    {
        JXPathContext ctx = JXPathContext.newContext( getRootNode( "basic" ) );
        assertEquals( "123", ctx.getValue( "string[1]" ) );
    }

    @Test
    public void transformMultilineMessageString()
        throws IOException, ParserConfigurationException, SAXException
    {
        JXPathContext ctx = JXPathContext.newContext( getRootNode( "multiline-message-string" ) );
        assertEquals( "123\\nadb", ctx.getValue( "string[1]" ) );
    }

    @Test
    public void createTemplate()
        throws IOException, ParserConfigurationException, SAXException
    {
        JXPathContext ctx = JXPathContext.newContext( getRootNodeForTemplate( "strings" ) );
        // assertEquals("123\\nadb", ctx.getValue("string[1]"));
    }

    private Node getRootNode( String filePath )
        throws IOException, ParserConfigurationException, SAXException
    {
        File input = new File( resourceDir, filePath + "/file.po" );
        File output = File.createTempFile( String.valueOf( Math.random() ), ".tmp" );

        PoTransformer.transformToStrings( input, output, "UTF-8" );
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse( output );
        Node node = document.getFirstChild();
        return node;
    }

    private Node getRootNodeForTemplate( String filePath )
        throws IOException, ParserConfigurationException, SAXException
    {
        File input = new File( resourceDir, filePath + "/strings.xml" );
        File output = File.createTempFile( String.valueOf( Math.random() ), ".tmp" );
        System.out.println( "-----------------" + output.getAbsolutePath() );

        PoTransformer.createTemplateFromStringsXml( input, output, null );
        /*
         * DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); DocumentBuilder builder =
         * factory.newDocumentBuilder(); Document document = builder.parse(output); Node node =
         * document.getFirstChild(); return node;
         */
        return null;
    }
}
