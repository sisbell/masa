package org.jvending.masa.ddmlib;

public class ConnectionException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 4728803709587003832L;

    public ConnectionException()
    {
        super();
    }

    public ConnectionException( String message, Throwable throwable )
    {
        super( message, throwable );
    }

    public ConnectionException( String message )
    {
        super( message );
    }

    public ConnectionException( Throwable throwable )
    {
        super( throwable );
    }
    
    

}
