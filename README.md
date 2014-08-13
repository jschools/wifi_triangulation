wifi_triangulation
==================

Uses Android phone and Raspberry Pi for an adhoc Internal Positioning System with ~10ft resolution

###### Please note that this setup as described is supposed to be a proof of principle. It is far from polished. What I attempt here serves as a springboard for further development. My goals here were determine 1) Does WiFi triangulation work at all? 2) What is the best application if Bayes Theorem (prior and posteriors)? 3) What sort of resolution and accuracy can be determined? I think I've answered these questions as you read. 




# Method Overview

Basically this relies on you walking to a designated location and waiting for 10minutes while devices collect information about the WiFi networks and strengths at that spot. Once all the locations have been "learned" then it simply calculates the Bayesian probability of location X given a WiFi signal from router Y with signal Z:

  ![BayesTheorem](https://rpiai.files.wordpress.com/2014/08/bayes_theorem.png)

A normalized Bayesian probability posterior can then be used for evaluation of the probability that a location is entered.

# Implementation

I'll go over how I implemented this code using my apartment as an example

## Learning locations

My apartment is almost exactly 1,000 sq ft. I divided my apartment into 8 frequented locations (shown by yellow circles). The first task is to aquire several hundred scans of all the WiFi networks and save them to a database. I have a roundabout way of doing this, hopefully to be improved in the future.  

I've used an SQLite database on the Raspberry Pi to store all the variables ```db/data.db```. The database was created using Python scripts ```dbsetup.py``` written by Travis. Records are inserted one at a time using a PHP script, ```update.php```. This PHP script has three inputs: MAC address, signal strength, location number (0 if not known) which are presented comma-delimited into the loc variable (i.e. ```http://blahblahblah/update.php?loc=3d:ma:c3:ad:d3,-54,1```.

The WiFi information is gathered from my Android device - a Droid DNA phone. I wrote a *really* simple App (```My First App```) which simply writes to a file all of the MAC addresses and signal strengths, pipe-delimited. I wish I was smart enough to write the app to do this for a few minutes and goto the webaddress above to insert the records, but I'm not. So instead I used [Tasker](Tasker url) which does the following loop: 1) Run my stupid App, 2) Read file with MAC address and signal strength, 3) Open URL to update the Raspbery Pi database with each MAC address in file, 4) Go back to 1) a 100 times. That's it. I just go to every location in my apartment, tell the Tasker handler which room I'm in and let it run for awhile. After doing this for each location, the database is populated and ready for determining Bayesion probabilties

### Determining Bayesian probabilities

These distributions depend on how the WiFi strength signals. After looking at a bunch of these I've noticed that there not quite uniform and not quite normal. Some are, but some are bimodal or otherwise complicated. Here is a typical:

For a catch all solution I opted to use a multivariate normal with six degrees of freedom (pair of means, variances weights). This allows for pretty precise control and only requires six mumbers

## Code


## Future development

- Use Python instead of Matlab to determine the fixed Posterior distributions and off load almost everything to the Raspberry Pi
- Make the Android app more friendly and not reliant on Tasker
- Eventually make entire process self contained in an Android app
