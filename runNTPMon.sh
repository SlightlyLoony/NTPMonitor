#! /bin/bash
# run by systemd to start up the NTP monitor program...

# Exits the systemd service with the given return code, after a short delay to ensure that journal entries are tagged with the unit...
systemd_exit () {
    sleep 0.1
    exit ${1}
}

# first we make sure that the serial port and GPS are properly initialized...
#! /bin/bash

# first we try to synchronize the host baud rate to whatever the GPS happens to use, with NMEA data...
RESULT=$(/home/tom/gpsctl/gpsctl -a nmea)

# if that fails, we try again using ASCII data...
if [ -n "${RESULT}" ]; then RESULT=$(/home/tom/gpsctl/gpsctl -a ascii); fi

# if that fails, we try again using UBX data...
if [ -n "${RESULT}" ]; then RESULT=$(/home/tom/gpsctl/gpsctl -a ubx); fi

# if that fails, we exit with an error message...
if [ -n "${RESULT}" ]
then
    echo "Failed to synchronize host serial port /dev/serial0 with GPS baud rate!"
    systemd_exit 1
fi

# then we try to set the baud rate for both to 115,200 baud...
/home/tom/gpsctl/gpsctl -B 115200 | grep -q 'Successfully '
if [ '0' -eq $? ]
then
    echo "Host serial port /dev/serial0 is synchronized with the GPS at 115,200 baud."
else
    echo "Could not set serial port /dev/serial0 and GPS to 115,200 baud!"
    systemd_exit 1
fi

# then if that worked, we start up the actual monitor...
echo "Starting NTPMonitor program..."
/usr/bin/java -jar /apps/ntpmon/NTPMonitor.jar /apps/ntpmon/ntpconfig.json /apps/ntpmon/ntplog.json
systemd_exit 1
