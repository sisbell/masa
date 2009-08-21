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
package org.jvending.masa.plugin.po.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PoParser
{

    public static List<PoEntry> readEntries( InputStream input )
        throws IOException
    {
        List<PoEntry> entries = new ArrayList<PoEntry>();

        BufferedReader in = new BufferedReader( new InputStreamReader( input ) );
        String line = in.readLine();

        while ( line != null )
        {
            if ( line.trim().length() == 0 || line.trim().equals( "#" ) )
            {
                PoEntry e = readEntry( in );
                if ( e != null )
                {
                    entries.add( e );
                }
                else
                {
                    return entries;
                }
            }
            line = in.readLine();
        }
        return entries;
    }

    private static final int ID = 0;

    private static final int STR = 1;

    private static final int CTX = 2;

    private static PoEntry readEntry( BufferedReader reader )
        throws IOException
    {
        String line = reader.readLine();
        if ( line == null )
        {
            return null;
        }
        PoEntry entry = new PoEntry();
        PoMessage msg = new PoMessage();
        entry.message = msg;

        int flag = -1;
        while ( line != null )
        {
            if ( line.startsWith( "#" ) )// TODO: Handle headers
            {
                flag = -1;
            }
            else if ( line.startsWith( "msgid" ) )
            {
                flag = ID;
                msg.messageId = getContextBetweenQuotes( line );
            }
            else if ( line.startsWith( "msgstr" ) )
            {
                flag = STR;
                msg.messageString = getContextBetweenQuotes( line );
            }
            else if ( line.startsWith( "msgctxt" ) )
            {
                flag = CTX;
                msg.messageContext = getContextBetweenQuotes( line );
            }
            else if ( line.startsWith( "\"" ) )
            {
                switch ( flag )
                {
                    case ID:
                        msg.messageId = msg.messageId + getContextBetweenQuotes( line );
                        break;
                    case STR:
                        msg.messageString = msg.messageString + getContextBetweenQuotes( line );
                        break;
                }
            }
            line = reader.readLine();

        }
        return entry;
    }

    private static String getContextBetweenQuotes( String line )
    {
        return line.substring( line.indexOf( "\"" ) + 1, line.lastIndexOf( "\"" ) );
    }
}
