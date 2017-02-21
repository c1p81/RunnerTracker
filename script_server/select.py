#!/usr/bin/python
# -*- coding: utf-8 -*-

import sqlite3 as lite
import sys


con = lite.connect('mmssms.db')

# Creates or opens a file called mydb with a SQLite3 DB
db = lite.connect('posizioni.db')
cursor = db.cursor()
cursor.execute('''DROP TABLE IF EXISTS posizioni''')
db.commit()
cursor.execute('''CREATE TABLE posizioni(rowid INTEGER PRIMARY KEY AUTOINCREMENT, squadra TEXT,ora TEXT, lat TEXT , lng TEXT , quota TEXT, precisione TEXT )''')
db.commit()

with con:    
    
    cur = con.cursor()    
    cur.execute("SELECT body FROM SMS")

    rows = cur.fetchall()

    for row in rows:
        stringa = row[0]
        # print stringa
        squadra = stringa.split(',')[0]
        ora = stringa.split(',')[1]
        lat = stringa.split(',')[2]
        lng = stringa.split(',')[3]
        quota =  stringa.split(',')[4]
        precisione = stringa.split(',')[-1]
        #print squadra + " " + ora +" "+ lat +" "+ lng +" "+quota +" "+precisione

        cursor = db.cursor()
        cursor.execute('''INSERT INTO posizioni(squadra, ora, lat, lng, quota, precisione)VALUES(?,?,?,?,?,?)''', (squadra,ora, lat, lng, quota, precisione))
        db.commit()
db.close()

print "Squadra;Ora;lat;lng;icon;Quota(m);Prec.(m)"
# questa prende l'ultimo punto per squadra
for x in range(1,15):
	con = lite.connect('posizioni.db')
	with con:    
		cur = con.cursor()    
    	cur.execute("SELECT * FROM posizioni WHERE squadra = "+ str(x) +" ORDER BY rowid DESC LIMIT 1")
    	rows = cur.fetchall()
    	for row in rows:
            squadra = row[1]
            ora = row[2]
            lat = row[3]
            lng = row[4]
            quota = row[5]
            precisione = row[6]
            icona = "./ico/number_%s.png" % (squadra)
            print "%s;%s;%s;%s;%s;%s;%s" % (squadra,ora,lat,lng,icona,quota,precisione)
    


