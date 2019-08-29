package com.dilatush.ntpmonitor;

import com.dilatush.util.EnumIndexer;
import com.dilatush.util.EnumInfo;

/**
 * Enumerates the possible states of the LEAP code within a system status word.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum LeapSecondMode {

    NONE         ( 0, "NONE",          "normal value for a synchronized clock"                  ),
    ADD_SECOND   ( 1, "ADD_SECOND",    "insert a leap second after 23:59:59 of the current day" ),
    DELETE_SECOND( 2, "DELETE_SECOND", "delete the second 23:59:59 of the current day"          ),
    ALARM        ( 3, "ALARM",         "the clock has never been synchronized"                  );

    private static EnumIndexer<LeapSecondMode> indexer;


    /**
     * Creates a new instance of {@link LeapSecondMode} with the given index, identifying string, and descriptive string.
     *
     * @param _index the index to associate with this {@link LeapSecondMode}
     * @param _id the identifying string to associate with this {@link LeapSecondMode}
     * @param _description the descriptive string to associate with this {@link LeapSecondMode}
     */
    LeapSecondMode( final int _index, final String _id, final String _description ) {
        set( _index, _id, _description );
    }


    /**
     * Sets the given index, identifying string, and descriptive string to the information for this {@link LeapSecondMode}.
     *
     * @param _index the index to associate with this {@link LeapSecondMode}
     * @param _id the identifying string to associate with this {@link LeapSecondMode}
     * @param _description the descriptive string to associate with this {@link LeapSecondMode}
     */
    private void set( final int _index, final String _id, final String _description ) {
        if( indexer == null )
            indexer = new EnumIndexer<>();
        indexer.add( this, _index, _id, _description );
    }

    /**
     * Returns the {@link LeapSecondMode} that has the given identifying string, or <code>null</code> if there is no such {@link LeapSecondMode}.
     *
     * @param _id the identifying string to look for
     * @return the {@link LeapSecondMode} with the given identifying string
     */

    public static LeapSecondMode fromID( final String _id ) {
        return indexer.fromID( _id );
    }


    /**
     * Returns the {@link LeapSecondMode} that has the given index, or <code>null</code> if there is no such {@link LeapSecondMode}.
     *
     * @param _index the index to look for
     * @return the {@link LeapSecondMode} with the given index
     */
    public static LeapSecondMode fromIndex( final int _index ) {
        return indexer.fromIndex( _index );
    }


    /**
     * Returns information (id, index, and description) associated with the given {@link LeapSecondMode}, or <code>null</code> if there is no
     * information available for the given {@link LeapSecondMode}.
     *
     * @return the information associated with the given {@link LeapSecondMode}
     */
    public EnumInfo info() {
        return indexer.info( this );
    }


    /**
     * Returns the decoded {@link LeapSecondMode} from the given system status word.
     *
     * @param _statusWord the system status word to decode
     * @return the decoded {@link LeapSecondMode} value
     */
    public static LeapSecondMode fromStatus( final int _statusWord ) {
        return indexer.fromIndex( (_statusWord >>> 14) & 0x3 );
    }
}
