package com.dilatush.ntpmonitor;

import com.dilatush.mop.Mailbox;
import com.dilatush.mop.Message;
import com.dilatush.util.Executor;
import com.dilatush.util.HJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dilatush.util.Strings.isEmpty;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class NTPMonitor {

    private static final Logger LOGGER                 = Logger.getLogger( new Object(){}.getClass().getEnclosingClass().getCanonicalName());

    private static final Executor ntpqpEx = new Executor( "ntpq -p" );
    private static final Executor ntpqcEx = new Executor( "ntpq -c kerninfo" );
    private static final Executor fixEx = new Executor( "/home/tom/gpsctl/gpsctl --query fix --json" );
    private static final Executor satEx = new Executor( "/home/tom/gpsctl/gpsctl --query satellites --json" );

    private static final Pattern ntpqpPat = Pattern.compile( "^(\\S)(\\S+)\\s+(\\S+)\\s+(\\d)\\s+([ul])\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([\\d.]+)\\s+([\\d.-]+)\\s+([\\d.]+)$", Pattern.MULTILINE );
    private static final Pattern ntpqcPat = Pattern.compile( "[^,]+,\\s*([^,]+).*?pll offset:\\s+([0-9Ee\\-.]+).*pll frequency:\\s+([0-9eE\\-.]+).*maximum error:\\s+([0-9Ee\\-.]+).*", Pattern.DOTALL );

    private final Mailbox box;

    private boolean valid;
    private String errorMessage;
    private List<Peer> peers;
    private boolean validPPS;
    private double pllOffsetMs;
    private double pllFrequencyOffsetPpm;
    private double maxErrMs;
    private boolean validTime;
    private double timeAccuracy;
    private int satellitesUsed;
    private boolean validFix;
    private boolean fixIs3D;
    private double latitude;
    private double longitude;
    private double altitudeFt;
    private double fixAccuracyFt;
    private List<Sat> satellites;
    private Boolean previousValidPPS;


    public NTPMonitor( final Mailbox _box ) {
        box = _box;
        previousValidPPS = null;  // to indicate that we haven't yet recorded the PPS state...
    }


    /**
     * Runs this monitor and fills the specified message with the results.
     *
     * @param _message the message to be filled.
     */
    /* package-private */ void fill( final Message _message ) {

        // first run the monitor...
        run();  // collect our data...
        fillMessage( _message );    // fill in the message...
        post();                     // post event for our important readings...
        postValidPPSChange();       // post an event if validPPS has changed...
    }


    /**
     * Post an event if validPPS has changed...
     */
    private void postValidPPSChange() {

        // if we haven't recorded one yet, record it leave...
        if( previousValidPPS == null ) {
            previousValidPPS = validPPS;
            return;
        }

        // otherwise, see if things have changed...
        if( previousValidPPS != validPPS ) {

            // yup, they've changed - so send the event...
            String message = "As reported by 'ntpq -c kerninfo', and noted at " + new Date().toString();
            String subject = validPPS ? "NTP now locked to PPS" : "NTP now NOT locked to PPS";
            Message msg = box.createDirectMessage( "events.post", "event.post", false );
            msg.putDotted( "tag",          "pps.change"               );
            msg.putDotted( "timestamp",    System.currentTimeMillis() );
            msg.putDotted( "event.source", "ntp.monitor"              );
            msg.putDotted( "event.type",    "valid.pps.changed"       );
            msg.putDotted( "event.message", message                   );
            msg.putDotted( "event.level",   5                         );
            msg.putDotted( "event.subject", subject                   );
            box.send( msg );
        }

        // record the latest...
        previousValidPPS = validPPS;
    }


    /*
     * Post event...
     */
    private void post() {

        // if we don't have a valid capture, skip all this...
        if( !valid ) return;

        // build our event message...
        Message msg = box.createDirectMessage( "events.post", "event.post", false );
        msg.putDotted( "tag",                          "ntpstats" );
        msg.putDotted( "timestamp",                    System.currentTimeMillis() );
        msg.putDotted( "fields.validPPS",              validPPS              );
        msg.putDotted( "fields.pllOffsetMs",           pllOffsetMs           );
        msg.putDotted( "fields.pllFrequencyOffsetPpm", pllFrequencyOffsetPpm );
        msg.putDotted( "fields.maxErrMs",              maxErrMs              );
        msg.putDotted( "fields.validTime",             validTime             );
        msg.putDotted( "fields.timeAccuracy",          timeAccuracy          );
        msg.putDotted( "fields.satellitesUsed",        satellitesUsed        );

        // send it!
        box.send( msg );
    }


    /*
     * Fills in the given message, from the data we've collected...
     */
    private void fillMessage( final Message _message ) {
        // no matter what, fill in the validity...
        _message.putDotted( "monitor.ntp.valid", valid );

        // if the results were not valid, fill in the error message and leave...
        if( !valid ) {
            _message.putDotted( "monitor.ntp.errorMessage", errorMessage );
            return;
        }

        // otherwise, fill in our collected data...
        _message.putDotted( "monitor.ntp.pllOffsetMs",      pllOffsetMs           );
        _message.putDotted( "monitor.ntp.pllFreqOffsetPpm", pllFrequencyOffsetPpm );
        _message.putDotted( "monitor.ntp.maxErrMs",         maxErrMs              );
        _message.putDotted( "monitor.ntp.validPPS",         validPPS              );
        _message.putDotted( "monitor.ntp.validTime",        validTime             );
        _message.putDotted( "monitor.ntp.timeAccuracy",     timeAccuracy          );
        _message.putDotted( "monitor.ntp.satellitesUsed",   satellitesUsed        );
        _message.putDotted( "monitor.ntp.validFix",         validFix              );
        _message.putDotted( "monitor.ntp.fixIs3D",          fixIs3D               );
        _message.putDotted( "monitor.ntp.latitude",         latitude              );
        _message.putDotted( "monitor.ntp.longitude",        longitude             );
        _message.putDotted( "monitor.ntp.altitudeFt",       altitudeFt            );
        _message.putDotted( "monitor.ntp.fixAccuracyFt",    fixAccuracyFt         );
        JSONArray peersJSON = new JSONArray();
        _message.putDotted( "monitor.ntp.peers",          peersJSON      );
        for( Peer peer : peers ) {
            JSONObject peerJSON = new JSONObject();
            peerJSON.put( "state",               peer.state               );
            peerJSON.put( "remote",              peer.remote              );
            peerJSON.put( "refid",               peer.refid               );
            peerJSON.put( "stratum",             peer.stratum             );
            peerJSON.put( "local",               peer.local               );
            peerJSON.put( "lastPolledSeconds",   peer.lastPolledSeconds   );
            peerJSON.put( "pollIntervalSeconds", peer.pollIntervalSeconds );
            peerJSON.put( "reached",             peer.reached             );
            peerJSON.put( "delayMs",             peer.delayMs             );
            peerJSON.put( "offsetMs",            peer.offsetMs            );
            peerJSON.put( "jitterRmsMs",         peer.jitterRmsMs         );
            peersJSON.put( peerJSON );
        }
        JSONArray satellitesJSON = new JSONArray();
        _message.putDotted( "monitor.ntp.satellites",     satellitesJSON  );
        for( Sat sat : satellites ) {
            JSONObject satJSON = new JSONObject();
            satJSON.put( "type",      sat.type      );
            satJSON.put( "id",        sat.id        );
            satJSON.put( "azimuth",   sat.azimuth   );
            satJSON.put( "elevation", sat.elevation );
            satJSON.put( "cno",       sat.cno       );
            satellitesJSON.put( satJSON );
        }
    }


    /**
     * Runs this monitor, executing operating system commands to find its current state.
     */
    private void run() {

        valid = false;

        // first we run ntpq -p and analyze it...
        String ntpq = ntpqpEx.run();
        if( isEmpty( ntpq ) ) {
            errorMessage = "Command ntpq -p failed";
            return;
        }
        peers = new ArrayList<>();
        Matcher mat = ntpqpPat.matcher( ntpq );
        while( mat.find() ) {
            Peer peer = new Peer();
            char state = mat.group( 1 ).charAt( 0 );
            switch( state ) {
                case ' ': peer.state = "(none)";             break;
                case 'x':
                case '-': peer.state = "Out of tolerance";   break;
                case '#': peer.state = "Good, not used";     break;
                case '+': peer.state = "Good, preferred";    break;
                case '*': peer.state = "Primary reference";  break;
                case 'o': peer.state = "PPS peer";           break;
                default:  peer.state = "(unknown):" + state; break;
            }
            peer.remote              = mat.group( 2 );
            peer.refid               = mat.group( 3 );
            peer.stratum             = Integer.parseInt( mat.group( 4 ) );
            peer.local               = "l".equals(       mat.group( 5 ) );
            peer.lastPolledSeconds   = Integer.parseInt( mat.group( 6 ) );
            peer.pollIntervalSeconds = Integer.parseInt( mat.group( 7 ) );
            peer.reached             = Integer.toBinaryString( 256 + Integer.parseInt( mat.group( 8 ), 8 ) ).substring( 1 );
            peer.delayMs             = Float.parseFloat( mat.group( 9 ) );
            peer.offsetMs            = Float.parseFloat( mat.group( 10 ) );
            peer.jitterRmsMs         = Float.parseFloat( mat.group( 11 ) );
            peers.add( peer );
        }

        // then we run ntpq -c kernelinfo and analyze it...
        validPPS = false;
        ntpq = ntpqcEx.run();
        if( isEmpty( ntpq ) ) {
            errorMessage = "Command ntpq -c failed";
            return;
        }
        mat = ntpqcPat.matcher( ntpq );
        if( mat.matches() ) {
            validPPS = "sync_pps".equals( mat.group( 1 ) );
            pllOffsetMs = Double.parseDouble( mat.group( 2 ) );
            pllFrequencyOffsetPpm = Double.parseDouble( mat.group( 3 ) );
            maxErrMs = Double.parseDouble( mat.group( 4 ) );
        }

        // now we get our fix, in JSON, and analyze it...
        String fixJSON = fixEx.run();
        if( isEmpty( fixJSON ) ) {
            errorMessage = "Command gpsctl query fix failed";
            return;
        }
        HJSONObject fix;
        try {
            fix = new HJSONObject( fixJSON );
        }
        catch( JSONException _e ) {
            errorMessage = "Query fix invalid JSON";
            LOGGER.log( Level.SEVERE, "Query fix invalid JSON: " + fixJSON, _e );
            return;
        }
        validTime = fix.getBooleanDotted( "time.valid" );
        timeAccuracy = 0.000000001 * fix.getIntDotted( "time.accuracy_ns" );
        satellitesUsed = fix.getIntDotted( "number_of_satellites_used" );
        validFix = fix.getBooleanDotted( "fix.valid" );
        fixIs3D = fix.getBooleanDotted( "fix.3d" );
        latitude = fix.getDoubleDotted( "fix.latitude_deg" );
        longitude = fix.getDoubleDotted( "fix.longitude_deg" );
        altitudeFt = fix.getDoubleDotted( "fix.height_above_mean_sea_level_mm" ) / (12.0 * 25.4);
        fixAccuracyFt = fix.getDoubleDotted( "fix.horizontal_accuracy_mm" ) / (12.0 * 25.4);

        // now we get our satellites...
        String satJSON = satEx.run();
        if( isEmpty( satJSON ) ) {
            errorMessage = "Command gpsctl query satellites failed";
            return;
        }
        HJSONObject satData;
        try {
            satData = new HJSONObject( satJSON );
        }
        catch( JSONException _e ) {
            errorMessage = "Query satellites invalid JSON";
            LOGGER.log( Level.SEVERE, "Query satellites invalid JSON: " + fixJSON, _e );
            return;
        }
        satellites = new ArrayList<>();
        JSONArray sats = satData.getJSONArray( "satellites" );
        for( int i = 0; i < sats.length(); i++ ) {

            JSONObject sat = sats.getJSONObject( i );
            if( !sat.getBoolean( "used" ) ) continue;

            Sat usedSat = new Sat();
            usedSat.type = sat.getString( "gnssID" );
            usedSat.id = sat.getInt( "satelliteID" );
            usedSat.cno = sat.getInt( "CNo" );
            usedSat.azimuth = sat.getInt( "azimuth" );
            usedSat.elevation = sat.getInt( "elevation" );
            satellites.add( usedSat );
        }

        valid = true;
    }


    private static class Sat {
        private String type;
        private int id;
        private int azimuth;
        private int elevation;
        private int cno;
    }


    private static class Peer {
        private String state;
        private String remote;
        private String refid;
        private int stratum;
        private boolean local;
        private int lastPolledSeconds;
        private int pollIntervalSeconds;
        private String reached;
        private float delayMs;
        private float offsetMs;
        private float jitterRmsMs;


    }
}
