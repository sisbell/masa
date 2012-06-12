package org.jvending.masa.ddmlib;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jvending.masa.ExecutionException;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;

public class AdbConnector
{

    private AndroidDebugBridge androidDebugBridge;

    private IDevice firstAttachedDevice;

    public synchronized IDevice getFirstAttachedDevice()
        throws ShellCommandUnresponsiveException, ExecutionException
    {

        if ( firstAttachedDevice != null )
        {
            return firstAttachedDevice;
        }

        FutureTask<Boolean> future = new FutureTask<Boolean>( new Callable<Boolean>()
        {
            public Boolean call()
            {
                while ( !androidDebugBridge.hasInitialDeviceList() )
                {
                    sleep( 300 );
                }
                firstAttachedDevice = androidDebugBridge.getDevices()[0];
                return true;
            }
        } );
        execute( future );

        try
        {
            if ( !future.get( 5000, TimeUnit.MILLISECONDS ) )
            {
                throw new ShellCommandUnresponsiveException();
            }

        }
        catch ( Exception e )
        {
            throw new ExecutionException( "Failed to attach device", e );
        }

        return firstAttachedDevice;

    }

    public AndroidDebugBridge connectToDevice()
        throws ConnectionException
    {
        AndroidDebugBridge.init( false );
        final AndroidDebugBridge adb = AndroidDebugBridge.createBridge();

        FutureTask<Boolean> future = new FutureTask<Boolean>( new Callable<Boolean>()
        {
            public Boolean call()
            {
                while ( !adb.isConnected() )
                {
                    sleep( 300 );
                }
                return true;
            }
        } );
        execute( future );

        try
        {
            if ( !future.get( 5000, TimeUnit.MILLISECONDS ) )
            {
                throw new ConnectionException( "Failed to connect to device: attempts = "
                    + adb.getConnectionAttemptCount() );
            }

            androidDebugBridge = adb;
            return adb;
        }
        catch ( Exception e )
        {
            throw new ConnectionException(
                                           "Failed to connect to device: attempts = " + adb.getConnectionAttemptCount(),
                                           e );
        }
    }

    private void execute( FutureTask<?> future )
    {
        ThreadPoolExecutor ex = new ThreadPoolExecutor( 1, 1, 10000, TimeUnit.MILLISECONDS,
                                                        new LinkedBlockingQueue<Runnable>() );
        ex.execute( future );
    }

    private void sleep( long millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch ( InterruptedException e )
        {

        }
    }
}
