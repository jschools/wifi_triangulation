import sqlite3
import hashlib
import re

class DataBase:
	
	def __init__(self,name):
		self.name = name
		self.conn = sqlite3.connect(self.name)
		self.c = self.conn.cursor()
		
	
	def close(self):
		self.conn.close()
		self.conn = None
		self.c = None	
	
		
	'''DATA'''
	
	
	def getAllData(self):
		return [row for row in self.c.execute('SELECT * FROM locations')]
	
	#NOT USER INPUT#id: auto generated just add null
	#lat: latitude
	#long: longitude
	#NOT USER INPUT#date: YYYY-MM-DD HH:MM:SS
	def addData(self,mac,rssi,room):
		self.c.execute('SELECT strftime("%s","now")')
		date = self.c.fetchone()[0]
		self.c.execute('INSERT INTO locations VALUES (?,?,?,?,?)',(None,mac,rssi,room,date))
		id = self.c.lastrowid
		#remember to commit changes so we don't lock the db!
		self.conn.commit()
		return True
		
	def removeData(self,id):
		self.c.execute('DELETE FROM locations WHERE id=?',(id,))
		self.conn.commit()
		
	def getData(self,id):
		return [row for row in self.c.execute('SELECT * FROM locations WHERE id=(?)',(id,))]
	
	
	'''
	
	GENERAL TABLE COMMANDS
	
	'''
	
	
	#returns true if table exists
	def tableExists(self,table_name):
		self.c.execute('SELECT count(*) FROM sqlite_master WHERE type="table" AND name=?;',(table_name,))
		return not self.c.fetchone()[0] is 0
	
	
	#builds table if doesn't already exist
	#-a little sketchy
	def createTable(self,table_data):
		if self.tableExists(table_data):
			return False
		else:
			self.c.execute('CREATE TABLE %s;' % table_data)
			self.conn.commit()
			return True

	
	#drops table
	#-a little sketchy
	def dropTable(self,table_name):
		if self.tableExists(table_name):
			self.c.execute('DROP TABLE %s' % table_name)
			self.conn.commit()
			return True
		else:
			return False
