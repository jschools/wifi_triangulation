<?php
try {

	if (!isset($_GET['loc']))
		throw new Exception('no get data');
	

	
	$a = explode(',',$_GET['loc']);
	$mac = (string) $a[0];
	$rssi = (float) $a[1];
	$room = (float) $a[2];
	$time = time();
	
	print "Got: mac = ".$mac." and rssi = ".$rssi . "<br/>";
	
	$db = new PDO("sqlite:./db/data.db");
	
 $db->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);	
	
	$query = " 
		INSERT INTO locations (
			mac,
			rssi,
			room,
			time
		) VALUES ( 
			:mac, 
			:rssi,
			:room, 
			:time
		) 
	";
	
	$stmt = $db->prepare($query);
	$stmt->bindParam(':mac', $mac, SQLITE3_TEXT);
	$stmt->bindParam(':rssi', $rssi, SQLITE3_INTEGER);
	$stmt->bindParam(':room', $room, SQLITE3_INTEGER);
	$stmt->bindParam(':time', $time, SQLITE3_INTEGER);
	$stmt->execute();
	
	print '<br>';
	
	print 'all done <br>';
	
	
	
}
catch(Exception $e) {
	print "<br/>";
	print $e;
}
?>
