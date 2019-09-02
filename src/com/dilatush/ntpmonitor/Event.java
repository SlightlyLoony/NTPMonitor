package com.dilatush.ntpmonitor;

import com.dilatush.util.EnumIndexer;
import com.dilatush.util.EnumInfo;

/**
 * Enumerates the possible states of the EVENT code within a system status word.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum Event {

    UNSPECIFIED       ( 0,  "UNSPECIFIED",       "unspecified event"                      ),
    FREQ_NOT_SET      ( 1,  "FREQ_NOT_SET",      "frequence file not available"           ),
    FREQ_SET          ( 2,  "FREQ_SET",          "frequency set from frequency file"      ),
    SPIKE_DETECT      ( 3,  "SPIKE_DETECT",      "spike detected"                         ),
    FREQ_MODE         ( 4,  "FREQ_MODE",         "initial frequency training mode"        ),
    CLOCK_SYNC        ( 5,  "CLOCK_SYNC",        "clock synchronized"                     ),
    RESTART           ( 6,  "RESTART",           "program restart"                        ),
    PANIC_STOP        ( 7,  "PANIC_STOP",        "clock error more than 600 seconds"      ),
    NO_SYSTEM_PEER    ( 8,  "NO_SYSTEM_PEER",    "no system peer"                         ),
    LEAP_ARMED        ( 9,  "LEAP_ARMED",        "leap second armed from file or Autokey" ),
    LEAP_DISARMED     ( 10, "LEAP_DISARMED",     "leap second disarmed"                   ),
    LEAP_EVENT        ( 11, "LEAP_EVENT",        "leap second event"                      ),
    CLOCK_STEP        ( 12, "CLOCK_STEP",        "clock stepped"                          ),
    KERN              ( 13, "KERN",              "kernel information message"             ),
    TAI               ( 14, "TAI",               "leap second values updated from file"   ),
    STALE_LEAP_VALUES ( 15, "STALE_LEAP_VALUES", "new NIST leap seconds file needed"      );

    private static EnumIndexer<Event> indexer;


    /**
     * Creates a new instance of {@link Event} with the given index, identifying string, and descriptive string.
     *
     * @param _index the index to associate with this {@link Event}
     * @param _id the identifying string to associate with this {@link Event}
     * @param _description the descriptive string to associate with this {@link Event}
     */
    Event( final int _index, final String _id, final String _description ) {
        set( _index, _id, _description );
    }


    /**
     * Sets the given index, identifying string, and descriptive string to the information for this {@link Event}.
     *
     * @param _index the index to associate with this {@link Event}
     * @param _id the identifying string to associate with this {@link Event}
     * @param _description the descriptive string to associate with this {@link Event}
     */
    private void set( final int _index, final String _id, final String _description ) {
        if( indexer == null )
            indexer = new EnumIndexer<>();
        indexer.add( this, _index, _id, _description );
    }


    /**
     * Returns the {@link Event} that has the given identifying string, or <code>null</code> if there is no such {@link Event}.
     *
     * @param _id the identifying string to look for
     * @return the {@link Event} with the given identifying string
     */

    public static Event fromID( final String _id ) {
        return indexer.fromID( _id );
    }


    /**
     * Returns the {@link Event} that has the given index, or <code>null</code> if there is no such {@link Event}.
     *
     * @param _index the index to look for
     * @return the {@link Event} with the given index
     */
    public static Event fromIndex( final int _index ) {
        return indexer.fromIndex( _index );
    }


    /**
     * Returns information (id, index, and description) associated with the given {@link Event}, or <code>null</code> if there is no
     * information available for the given {@link Event}.
     *
     * @return the information associated with the given {@link Event}
     */
    public EnumInfo info() {
        return indexer.info( this );
    }


    /**
     * Returns the decoded {@link Event} from the given system status word.
     *
     * @param _statusWord the system status word to decode
     * @return the decoded {@link Event} value
     */
    public static Event fromStatus( final int _statusWord ) {
        return indexer.fromIndex( _statusWord & 0xFF );
    }
}
