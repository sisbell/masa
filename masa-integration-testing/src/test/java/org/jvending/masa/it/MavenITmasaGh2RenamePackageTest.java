package org.jvending.masa.it;

import java.io.File;
import java.util.Properties;

import org.apache.maven.it.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class MavenITmasaGh2RenamePackageTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmasaGh2RenamePackageTest()
    {
        super( ALL_MAVEN_VERSIONS );
    }

    /**
     * Tests packaging into pozip
     * 
     * @throws Exception
     */
    public void testitA()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/masa-gh2" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "target" );
        verifier.executeGoal( "install" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
        verifier.assertFilePresent( "target/masa-gh2-1.0.apk" );
    }
 
}
