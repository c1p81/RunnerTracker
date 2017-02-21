#!/bin/bash
rm -f mmssms.db
rm -f posizioni.db
rm -f posizioni.txt
while true
do
	adb shell "su -c 'rm /data/local/tmp/mmssms.db'"
	adb shell "su -c 'cp /data/data/com.android.providers.telephony/databases/mmssms.db /data/local/tmp'"
	adb shell "su -c 'chmod 755 /data/local/tmp/mmssms.db'" 
	adb pull /data/local/tmp/mmssms.db
	./select.py > posizioni.txt
	./altervista.sh
	sleep 300
done
