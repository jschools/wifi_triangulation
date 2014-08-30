import subprocess as s
from websocket import create_connection
import thread
import os
import socket
from sys import exit , stdout , stdin
import serial
import datetime
import socket
import sys
from time import time,strftime,localtime,sleep
from sqlite3 import connect
import cPickle as pickle
import math
from operator import itemgetter
import numpy

base_directory = "/var/www/where3/"
ip_address = '127.0.0.1:9003'

def get_lock(process_name):
    global lock_socket
    lock_socket = socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)
    try:
        lock_socket.bind('\0' + process_name)
        print 'I got the lock'
    except socket.error:
        print 'lock exists'
        sys.exit()		

def getMessages():
	while 1:
		result = ws.recv()
		print result
			
						
def sendState(msg):
	try:
		ws.send(msg)
	except:
		pass


get_lock('python_server_com_where3')

# Load main data
start_time = time()
start_time2 = time()
data=pickle.load(open(base_directory+'P.p','rb'))
P=pickle.loads(data)
data=pickle.load(open(base_directory+'nP.p','rb'))
nP=pickle.loads(data)
data=pickle.load(open(base_directory+'W.p','rb'))
Wdefault=pickle.loads(data)
print "<i><small>Loaded data (%2.1f seconds)</small></i><br>" % (time() - start_time)
start_time=time() 



# setup server connections
ws = create_connection("ws://"+ip_address+"/ws")
sendState('hello')
try:
	thread.start_new_thread(getMessages,())
except:
	sys.exit(0)
while 1:
	sleep(1)
	conn = connect(base_directory+'db/data.db')
	c = conn.cursor()
	for row in c.execute("select time  from locations order by id desc limit 1"):
		lastTime =  row[0]
	conn.close()


	conn = connect(base_directory+'db/data.db')
	c = conn.cursor()
	W=Wdefault
	#for row in c.execute("SELECT mac,rssi FROM locations WHERE time>STRFTIME('%s',DATETIME('now','-30 hours'))GROUP BY mac"):
	#for row in c.execute("select mac,rssi from (select id,mac,rssi from locations order by id desc limit 30) group by mac"):
	for row in c.execute("select mac,Avg(rssi) as rssi from (select * from locations order by id desc limit 100) group by mac"):
		W[str(row[0]).decode('utf-8')]=row[1]

	conn.close()
	print "<i><small>Pulled database (%2.1f seconds)</small></i><br>" % (time() - start_time)
	start_time=time()
	locations = P[P.keys()[0]].keys() 
	macs = P.keys()
	numberLocations =  len(locations)
	numberMACs = len(macs)

	P_bayes = {}
	for loc in locations:
		P_bayes[loc]=0

	P_A = 1.0/numberLocations;
	P_notA = (numberLocations-1.0)/numberLocations;
	for loc in locations:
		P_bayes[loc] = 0
		P_B_notA = 0
		P_B_A = math.log(P_A)
		for mac in macs:
			pInd = int(W[mac]+100)
			if (W[mac]<0):
				pFoo = P[mac][loc][pInd]
				if (pFoo > 0):
					P_B_A = P_B_A + math.log(pFoo)
				pFoo = nP[mac][loc][pInd]
				if (pFoo > 0):
					P_B_notA = P_B_notA + math.log(pFoo) 
		P_bayes[loc] =   (P_B_notA) - (P_B_A)

		
	print "<i><small>Calculated Bayes (%2.1f seconds)</small></i><br>" % (time() - start_time)
	start_time=time()

	sorted_P_bayes = {}
	sorted_P_bayes = sorted(P_bayes.iteritems(), key=itemgetter(1),reverse=True)
	first = True
	print "<p style='font-family:Courier;color:red;font-size:24px;'>"
	toSend = "Log-likelihood:       Room\n"
	for s in sorted_P_bayes:
		if first:
			toSend = toSend +  "%2.3f: %s\n" % (s[1],s[0])
			first = False
		else:
			toSend = toSend + "%2.3f: %s \n" % (s[1],s[0])

	print  strftime('Last recorded at %Y-%m-%d %H:%M:%S\n', localtime(lastTime))
	print "</p>"
	print "<i><small>Finished (%2.1f seconds)</small></i><br>" % (time() - start_time2)
	sendState(toSend)

ws.close()
