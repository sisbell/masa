package org.jvending.masa.it;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.it.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class MavenITmasa33StringsMergingTest  extends AbstractMavenIntegrationTestCase
{

    public MavenITmasa33StringsMergingTest()
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
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/masa-33" );
/*
        List<String> options = new ArrayList<String>();
        options.add("-DinputFileA=strings-en.xml");
        options.add("-DinputFileB=strings-fr.xml");
        options.add("-DoutputFile=strings.po");
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("inputFileA", "strings-en.xml");
        map.put("inputFileB", "strings-fr.xml");
        map.put("outputFile", "strings.po");
        */
        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.executeGoal( "org.jvending.masa.plugins:maven-po-plugin:merge");
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
        verifier.assertFilePresent( "strings.po" );
    //    verifier.assertFilePresent( "target/processed-resources/values/config.xml" );
        
        //Contents
        //verifier.verify(FileUtils.contentEquals(new File("localized-resources/res-development"), new File("res/values/config.xml")));
    }

}
