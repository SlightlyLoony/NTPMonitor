package com.dilatush.ntpmonitor;

import com.dilatush.util.EnumIndexer;
import com.dilatush.util.EnumInfo;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum SynchronizationSource {


    SYNC_SRC_UNSPEC     ( 0,  "UNSPEC",     "not yet synchronized"                         ),
    SYNC_SRC_PPS        ( 1,  "PPS",        "pulse-per-second signal"                      ),
    SYNC_SRC_LF_RADIO   ( 2,  "LF_RADIO",   "VLF, LF radio (WWVB, DCF77, etc.)"            ),
    SYNC_SRC_HF_RADIO   ( 3,  "HF_RADIO",   "MF/HF radio (WWV, etc.)"                      ),
    SYNC_SRC_UHF_RADIO  ( 4,  "UHF_RADIO",  "VHF/UHF radio/satellite (GPS, Galileo, etc.)" ),
    SYNC_SRC_LOCAL      ( 5,  "LOCAL",      "local timecode (IRIG, LOCAL driver, etc."     ),
    SYNC_SRC_NTP        ( 6,  "NTP",        "peer NTP server(s)"                           ),
    SYNC_SRC_OTHER      ( 7,  "OTHER",      "other (IEEE 1588, openntp, crony, etc."       ),
    SYNC_SRC_WRISTWATCH ( 8,  "WRISTWATCH", "eyeball and wristwatch"                       ),
    SYNC_SRC_TELEPHONE  ( 9,  "TELEPHONE",  "telephone modem"                              ),
    SYNC_SRC_UNKNOWN    ( 63, "UNKNONW",    "unknown"                                      );

    private static EnumIndexer<SynchronizationSource> indexer;


    /**
     * Creates a new instance of {@link SynchronizationSource} with the given index, identifying string, and descriptive string.
     *
     * @param _index the index to associate with this {@link SynchronizationSource}
     * @param _id the identifying string to associate with this {@link SynchronizationSource}
     * @param _description the descriptive string to associate with this {@link SynchronizationSource}
     */
    SynchronizationSource( final int _index, final String _id, final String _description ) {
        set( _index, _id, _description );
    }


    /**
     * Sets the given index, identifying string, and descriptive string to the information for this {@link SynchronizationSource}.
     *
     * @param _index the index to associate with this {@link SynchronizationSource}
     * @param _id the identifying string to associate with this {@link SynchronizationSource}
     * @param _description the descriptive string to associate with this {@link SynchronizationSource}
     */
    private void set( final int _index, final String _id, final String _description ) {
        if( indexer == null )
            indexer = new EnumIndexer<>();
        indexer.add( this, _index, _id, _description );
    }


    /**
     * Returns the {@link SynchronizationSource} that has the given identifying string, or <code>null</code> if there is no such
     * {@link SynchronizationSource}.
     *
     * @param _id the identifying string to look for
     * @return the {@link SynchronizationSource} with the given identifying string
     */
    public static SynchronizationSource fromID( final String _id ) {
        return indexer.fromID( _id );
    }


    /**
     * Returns the {@link SynchronizationSource} that has the given index, or <code>null</code> if there is no such {@link SynchronizationSource}.
     *
     * @param _index the index to look for
     * @return the {@link SynchronizationSource} with the given index
     */
    public static SynchronizationSource fromIndex( final int _index ) {
        return indexer.fromIndex( _index );
    }

    /**
     * Returns information (id, index, and description) associated with the given {@link SynchronizationSource}, or <code>null</code> if there is no
     * information available for the given {@link SynchronizationSource}.
     *
     * @return the information associated with the given enum
     */

    public EnumInfo info() {
        return indexer.info( this );
    }


    /**
     * Returns the decoded synchronization source value from the given system status word.
     *
     * @param _statusWord the system status word to decode
     * @return the decoded {@link SynchronizationSource} value
     */
    public static SynchronizationSource fromStatus( final int _statusWord ) {

        // isolate the source field and make sure it's within range...
        int source = (_statusWord >>> 8) & 0x3F;
        if( source > 9 )
            return SYNC_SRC_UNKNOWN;

        // it's within range, so look it up...
        return indexer.fromIndex( source );
    }
}
