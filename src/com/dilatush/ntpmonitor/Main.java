package com.dilatush.ntpmonitor;

import com.dilatush.mop.Mailbox;
import com.dilatush.mop.Message;
import com.dilatush.mop.PostOffice;
import com.dilatush.mop.util.JVMMonitor;
import com.dilatush.mop.util.OSMonitor;
import com.dilatush.util.Config;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dilatush.util.General.isNotNull;
import static java.lang.Thread.sleep;

/**
 * Implements a simple monitor of our NTP server.  Accepts the following arguments on the command line (note that if the log configuration file
 * path is specified, the post office configuration file path <i>must</i> also be specified):
 * <ol>
 *    <li>monitor configuration file path (default is "ntpconfig.json")</li>
 *    <li>logger configuration file path (default is "ntplog.json")</li>
 * </ol>
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Main {

    private static final Logger LOGGER                 = Logger.getLogger( new Object(){}.getClass().getEnclosingClass().getCanonicalName());
    private static Mailbox mailbox;
    private static OSMonitor osMonitor;
    private static JVMMonitor jvmMonitor;
    private static NTPMonitor ntpMonitor;


    public static void main( String[] _args ) {

        // the configuration file...
        String config = "ntpconfig.json";   // the default...
        if( isNotNull( (Object) _args ) && (_args.length > 0) ) config = _args[0];
        if( !new File( config ).exists() ) {
            System.out.println( "NTP configuration file " + config + " does not exist!" );
            return;
        }

        // the logger configuration file...
        String logger = "ntplog.json";
        if( isNotNull( (Object) _args ) && (_args.length > 1) ) logger = _args[1];
        if( !new File( config ).exists() ) {
            System.out.println( "NTP monitor log configuration file " + logger + " does not exist!" );
            return;
        }

        // get our config...
        Config ntpConfig = Config.fromJSONFile( config );
        long monitorIntervalSeconds = ntpConfig.optLongDotted( "monitorInterval", 60 );
        long monitorInterval = 1000 * monitorIntervalSeconds;
        LOGGER.log( Level.INFO, "NTP Monitor is starting, publishing updates at " + monitorIntervalSeconds + " second intervals" );

        // start up our post office...
        PostOffice po = new PostOffice( config );
        mailbox = po.createMailbox( "monitor" );

        // set up our monitors...
        osMonitor = new OSMonitor();
        jvmMonitor = new JVMMonitor();
        ntpMonitor = new NTPMonitor( mailbox );

        // set up our timer...
        Timer timer = new Timer( "NTP Monitor Timer", true );
        timer.scheduleAtFixedRate( new NTPMonitorTask(), 100, monitorInterval );

        // now we just hang about...
        while( true ) {
            try {
                sleep( 1000 );
            }
            catch( InterruptedException _e ) {
                break;
            }
        }
    }


    private static class NTPMonitorTask extends TimerTask {

        @Override
        public void run() {

            // get our empty message...
            Message msg = mailbox.createPublishMessage( "ntp.monitor" );

            // run our monitors and fill in the info...
            osMonitor.fill( msg );
            jvmMonitor.fill( msg );
            ntpMonitor.fill( msg );

            // publish the message...
            mailbox.send( msg );

            LOGGER.log( Level.INFO, "Published monitor information" );

            // if we have an error, log it...
            if( ! msg.getBooleanDotted( "monitor.ntp.valid" ) ) {
                LOGGER.log( Level.SEVERE, msg.getStringDotted( "monitor.ntp.errorMessage" ) );
            }
        }
    }
}
