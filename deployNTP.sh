#! /bin/bash

# copy all the deployment files over to the NTP server...
scp out/artifacts/NTPMonitor.jar tom@ntp:/apps/ntpmon
scp NTP.service tom@ntp:/apps/ntpmon
scp ntpconfig.json tom@ntp:/apps/ntpmon
scp ntplog.json tom@ntp:/apps/ntpmon
scp runNTPMon.sh tom@ntp:/apps/ntpmon

# execute commands on the NTP server
# get to the app directory
# set mode and owner on files that stay in that directory
# copy the service wrapper (if it has changed) to the systemd/system directory, change its mode and owner
# bounce the CPO service
ssh tom@ntp << RUN_ON_BEAST
cd /apps/ntpmon
sudo chown ntpmon:ntpmon NTPMonitor.jar
sudo chmod ug+xrw NTPMonitor.jar
sudo chown ntpmon:ntpmon ntpconfig.json
sudo chmod ug+xrw ntpconfig.json
sudo chown ntpmon:ntpmon ntplog.json
sudo chmod ug+xrw ntplog.json
sudo chown ntpmon:ntpmon runNTPMon.sh
sudo chmod ug+xrw runNTPMon.sh
sudo cp -u NTP.service /etc/systemd/system
sudo chown ntpmon:ntpmon /etc/systemd/system/NTP.service
sudo chmod ug+xrw /etc/systemd/system/NTP.service
sudo systemctl stop NTP.service
sudo systemctl daemon-reload
sudo systemctl start NTP.service
RUN_ON_BEAST