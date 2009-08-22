package org.jvending.masa.it;

import java.io.File;

import org.apache.maven.it.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.util.FileUtils;

public class MavenITmasa30LocalizedResourcesTest extends AbstractMavenIntegrationTestCase
{

    public MavenITmasa30LocalizedResourcesTest()
    {
        super( ALL_MAVEN_VERSIONS );
    }

    /**
     * Tests packaging into pozip
     * 
     * @throws Exception
     */
    public void testit()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/masa-30" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "target" );
        verifier.executeGoal( "compile" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
        verifier.assertFilePresent( "res/values/config.xml" );
        verifier.assertFilePresent( "target/processed-resources/values/config.xml" );
        
        //Contents
        verifier.verify(FileUtils.contentEquals(new File("localized-resources/res-development"), new File("res/values/config.xml")));
    }
    

}
