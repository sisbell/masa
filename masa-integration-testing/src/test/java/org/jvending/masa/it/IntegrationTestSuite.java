package org.jvending.masa.it;

import java.io.PrintStream;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IntegrationTestSuite extends TestCase
{
    private static PrintStream out = System.out;

    public static Test suite()
        throws VerificationException
    {
        Verifier verifier = null;
        try
        {
            verifier = new Verifier( "" );
            String mavenVersion = verifier.getMavenVersion();

            out.println( "Running integration tests for Masa with Maven Version: " + mavenVersion);

            System.setProperty( "maven.version", mavenVersion );
        }
        finally
        {
            if ( verifier != null )
            {
                verifier.resetStreams();
            }
        }

        TestSuite suite = new TestSuite();

        suite.addTestSuite( MavenITmasa30LocalizedResourcesTest.class );
        suite.addTestSuite( MavenITmasa29PoTest.class );

        /*
         * Add tests in reverse alpha order above.
         */

        return suite;
    }


}
