sqlite3 mmssms.db << EOF
.mode csv
.headers on
.separator ","
.output sms2.csv
select body,datetime(date_sent/1000, 'unixepoch','localtime') from sms;
.exit
EOF
