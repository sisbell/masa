package org.jvending.masa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public final class ApplicationRequirements
{

    private final Xpp3Dom dom;

    public ApplicationRequirements( Xpp3Dom dom )
    {
        if ( dom == null )
        {
            throw new IllegalArgumentException( "dom: null" );
        }

        this.dom = dom;
    }

    public List<String> getLanguages()
    {
        Xpp3Dom lang = dom.getChild( "languages" );
        return ( lang != null ) ? Arrays.asList( lang.getValue().split( "," ) ) : new ArrayList<String>();
    }

    public boolean matches( String[] qualifiers )
    {
        if ( qualifiers == null || qualifiers.length == 0 )
        {
            return true;
        }

        List<String> all = new ArrayList<String>();
        all.addAll( getLanguages() );

        for ( String qualifier : qualifiers )
        {
            if ( !all.contains( qualifier ) )
            {
                return false;
            }
        }
        return true;
    }
}
