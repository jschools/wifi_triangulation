wifi_triangulation
==================

Uses Android phone and Raspberry Pi for an adhoc Internal Positioning System with ~10ft resolution

###### Please note that this setup as described is supposed to be a proof of principle. It is far from polished. What I attempt here serves as a springboard for further development. My goals here were determine 1) Does WiFi triangulation work at all? 2) What is the best application if Bayes Theorem (prior and posteriors)? 3) What sort of resolution and accuracy can be determined? I think I've answered these questions as you read. 

# Requirements

- ~~Matlab with [mqsqlite](http://sourceforge.net/projects/mksqlite/)~~ (08/30/14: Python does all this now!)
- Python+SQLite (I hope to remove this dependency eventually)
- Android SDK
- ~~[Tasker for Android](http://tasker.dinglisch.net/)~~ (08/30/14: Android App can do this now!)


# Future development

Lots and lots to do! This was my first Android app every, which is why it sucks, and I've only spent ~9 hours on this project. Please contribute ideas/code or fork it and continue yourself!

- ~~Use Python instead of Matlab to determine the fixed Posterior distributions and off load almost everything to the Raspberry Pi~~ *Made possible using simpler (and just as effective) prior calculation scheme*
- ~~Make the Android app more friendly and not reliant on Tasker~~ **Thank you [jschools](https://github.com/jschools)**
- Allow Android to collect data in the background
- Ability to change URL in AndroidApp
- Eventually make entire process self contained in an Android app

# Acknowledgements

Travis provided all the database code for Python and helped me get all of that started. [jschools](https://github.com/jschools) got the android app and php uploading working!


# Background Information 

## Implementation

Basically this relies on you walking to a designated location and waiting for 10minutes while devices collect information about the WiFi networks and strengths at that spot. Once all the locations have been "learned" then it simply calculates the Bayesian probability of location X given a WiFi signal from router Y with signal Z. It does this using Bayes' theorem:

  ![BayesTheorem](https://upload.wikimedia.org/math/d/9/2/d92e290c66d423e4798a22a3690cbd31.png)
  
In this case, since there are Y routers and X locations, we use version of Bayes' theorem with multiple observations:

  ![BayesTheorem2](https://rpiai.files.wordpress.com/2014/08/tex2png-10.png?w=600)
<!--http://frog.isima.fr/bruno/share/tex2png/
P(\text{Loc}_X | \text{WiFi}_Y = Z_Y) = \frac{ P( \text{Loc}_X  ) \prod_Y P(\text{WiFi}_Y = Z_Y |\text{Loc}_X )}{P(\text{WiFi}_1 = Z_1,\ldots,\text{WiFi}_Y = Z_Y)}
-->

which can be simplifed (for computational reasons) using the Log-likelihood:

  ![BayesTheorem2](https://rpiai.files.wordpress.com/2014/08/tex2png-10-1.png?w=900)

<!--
\log\left(P(\text{Loc}_X | \text{WiFi}_Y = Z_Y)\right) = \log\left( P( \text{Loc}_X  ) \right) + \sum_Y \log \left( P(\text{WiFi}_Y = Z_Y |\text{Loc}_X ) \right) -  \sum_Y \log\left( P(\text{WiFi}_Y = Z_Y) \right)
-->


I'll go over how I implemented this code using my apartment as an example

## Learning locations

My apartment is almost exactly 1,000 sq ft. I divided my apartment into 8 frequented locations (shown by yellow circles):

  ![Layout](https://rpiai.files.wordpress.com/2014/08/apartment_layout_wifi-01.png?w=243)
  
The first task is to aquire several hundred scans of all the WiFi networks and save them to a database. I have a roundabout way of doing this, hopefully to be improved in the future.  

I've used an SQLite database on the Raspberry Pi to store all the variables ```db/data.db```. The database was created using Python scripts ```dbsetup.py``` written by Travis. Records are inserted one at a time using a PHP script, ```update.php```. This PHP script has three inputs: MAC address, signal strength, location number (0 if not known) which are presented comma-delimited into the loc variable (i.e. ```http://blahblahblah/update.php?loc=3d:ma:c3:ad:d3,-54,1```.

The WiFi information is gathered from my Android device - a Droid DNA phone. I wrote a *really* simple App (```My First App```) which simply writes to a file all of the MAC addresses and signal strengths, pipe-delimited. I wish I was smart enough to write the app to do this for a few minutes and goto the webaddress above to insert the records, but I'm not. So instead I used [Tasker](Tasker url) which does the following loop: 1) Run my stupid App, 2) Read file with MAC address and signal strength, 3) Open URL to update the Raspbery Pi database with each MAC address in file, 4) Go back to 1) a 100 times. That's it. I just go to every location in my apartment, tell the Tasker handler which room I'm in and let it run for awhile. After doing this for each location, the database is populated and ready for determining Bayesion probabilties

### Determining Bayesian probabilities

These distributions depend on the WiFi strength signals. I initialliy tried using Gaussian mixture models, but found a much simpler and effective way is to just estimate the probabilities by the number of events at a given RSSI divided by the total number of events. This costs more overhead, but its not much more and its insignificant as long as your not polling thousands of locations.

### Simulations

Here are some simulations from real data. This code essential picks a room and then picks random signals from that room and tests how often it is correct. In general, this method is accurate 75-90% of the time.  However this can be supplemented to further improve a little bit.

To improve further I introduced some metrics for minimum passing calls. The obvious metric is how low the maximum Bayesian probability can be, and another could be the ratio between the maximum Bayesian probability and the next highest. The plot of these two metrics is shown here:

  ![Metrics](http://rpiai.files.wordpress.com/2014/08/two-metrics.png?w=300)

As you can see there is a localization of "bad" points which can be cutoff with these metrics. For example, taking this data and setting the metrics to be ~1.2 (First/Second) and ~25% (minimum first result) you can greatly improve the correct calls/room as shown here for before/after at the cost of dropping about 37% of the polls:

  ![Improvmenets](http://rpiai.files.wordpress.com/2014/08/metric-improvements.png?w=500)

# Step-by-step guide to implementation

**In progress**

0. This project really works well with a Raspbery Pi. Get one and install python and sqlite3. I believe its fine if you use the local network, but make sure to forward Port 9003 so the websockets will work. Put the ```RaspberryPi``` files in your public html folder, ```/var/www/```.

1. First run ```dbsetup.py``` to set up the table.
 
2. Use the Android app now, pointing the app towards your URL of ```update.php```. Walk around to each room and collect some data points and upload them to the server. Be sure to register which room your in! This step is worth repeating every once and awhile.

3. Once you have data, run ```calculatePriors.py``` which will save a Pickle of the parameters for all the mac addresses and locations in your database. This takes a few minutes so thats why its a separate file.

4. To start up the server now, first make sure your IP addresses in ```index.html``` and ```server_com.py``` are correct. Then run ```nohup python server.py &``` to start the main listener and then ```nohup python server_com.py &``` to start the calculation of Bayesian probabilities. It will calculate about once per second. The calculations will automatically update on ```index.html```.
