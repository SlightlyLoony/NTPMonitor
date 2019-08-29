package com.dilatush.ntpmonitor;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {

    public static void main( String[] _args ) {

        int status = 0x4115;
        LeapSecondMode leapSecondMode = LeapSecondMode.fromStatus( status );
        String descr = leapSecondMode.info().description;
        String id    = leapSecondMode.info().id;

        leapSecondMode.hashCode();
    }


}
