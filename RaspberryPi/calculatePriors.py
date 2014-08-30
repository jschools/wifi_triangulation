import numpy
import cPickle as pickle
import math
from sqlite3 import connect

conn = connect('/var/www/where3/db/data.db') # use your own location here

# Determine all unique rooms and mac addressed automatically
c = conn.cursor()
macs = []
for row in c.execute("select distinct mac from locations where room>0"):
	macs.append(str(row[0]).decode('utf-8'))

rooms = []
for row in c.execute("select distinct room from locations where room>0"):
	rooms.append(str(row[0]).decode('utf-8'))

# Initialize probabilities	
P={}
nP={}
Wdefault = {}
for mac in macs:
	Wdefault[mac]=0
	P[mac]={}
	nP[mac]={}
	for room in rooms:
		P[mac][room] = numpy.zeros(100)
		nP[mac][room] = numpy.zeros(100)

# Add a Gaussian centered around RSSI with STD of 1 RSSI - this takes awhile
for mac in macs:
	for room in rooms:
		for row in c.execute("select rssi from (select rssi from locations where mac like '" + mac + "' and room=" + str(room) + ")"):
			try:
				P[mac][room][row[0]+100]=P[mac][room][row[0]+100]+0.4
				P[mac][room][row[0]+99]=P[mac][room][row[0]+99]+0.24
				P[mac][room][row[0]+101]=P[mac][room][row[0]+101]+0.24
				P[mac][room][row[0]+98]=P[mac][room][row[0]+98]+0.06
				P[mac][room][row[0]+102]=P[mac][room][row[0]+102]+0.06
			except Exception,e:
				print str(e)
		for row in c.execute("select rssi from locations where mac like '" + mac + "' and room>0"):
			try:
				nP[mac][room][row[0]+100]=nP[mac][room][row[0]+100]+0.4
				nP[mac][room][row[0]+99]=nP[mac][room][row[0]+99]+0.24
				nP[mac][room][row[0]+101]=nP[mac][room][row[0]+101]+0.24
				nP[mac][room][row[0]+98]=nP[mac][room][row[0]+98]+0.06
				nP[mac][room][row[0]+102]=nP[mac][room][row[0]+102]+0.06
			except Exception,e:
				print str(e)

# close the database
conn.close()

# Normalize the distributions
for mac in macs:
	for room in rooms:
		pTotal = sum(P[mac][room])
		npTotal = sum(nP[mac][room])
		if pTotal>0:
			for i in range(100):
				P[mac][room][i] = P[mac][room][i]/pTotal
		if npTotal>0:
			for i in range(100):
				nP[mac][room][i] = nP[mac][room][i]/npTotal

# Dumpe them to a pickle
data = pickle.dumps(P,2)
pickle.dump(data,open('P.p','wb'))
data = pickle.dumps(nP,2)
pickle.dump(data,open('nP.p','wb'))
data = pickle.dumps(Wdefault,2)
pickle.dump(data,open('W.p','wb'))