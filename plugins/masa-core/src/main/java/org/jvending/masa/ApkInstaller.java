package org.jvending.masa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public class ApkInstaller
{
    public static void install( File apk, String adbPath, Log log )
        throws ExecutionException
    {
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( log );

        List<String> commands = new ArrayList<String>();
        commands.add( "install" );
        commands.add( "-r" );
        commands.add( apk.getAbsolutePath() );

        log.info( adbPath + ":" + commands.toString() );
        executor.executeCommand( adbPath, commands );
    }
}
