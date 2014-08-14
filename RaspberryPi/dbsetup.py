import sqlite3
import hashlib

exec(open('databasecommands.py').read())

DB = DataBase('db/data.db')

#
#SET UP INITIAL TABLES
#

#locations TABLE

if not DB.tableExists('locations'):
	print('no table "locations"... making one now...')
	#lat: latitude
	#long: longitude
	#time: unix time stamp, unixepoch
	DB.createTable('locations ('\
		+'id INTEGER PRIMARY KEY AUTOINCREMENT, '\
		+'mac TEXT, '\
		+'rssi INTEGER, '\
		+'room INTEGER, '\
		+'time INTEGER'\
		+')')
else:
	print('already "locations" table')

print('adding test data...')
if DB.getData(1):
	print('already data..')
else:
	DB.addData('none',0,0)
	
def _resetDB():
	DB.dropTable('locations')
	DB.close()
