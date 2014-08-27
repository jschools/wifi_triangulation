<?php

// define the query string first
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

/*
The POST body has this JSON format:
[
	{
		"mac": "48:2C:6A:1E:59:3D",
		"rssi": -45,
		"room": 3,
		"time": 1409108787
	},
	{
		"mac": "48:2C:6A:1E:59:3D",
		"rssi": -44,
		"room": 3,
		"time": 1409108788
	},
	...
	{
		"mac": "48:2C:6A:1E:59:3D",
		"rssi": -55,
		"room": 3,
		"time": 1409108804
	}
]
*/
function insert_data_point($data_point) {
	$mac = strtolower($data_point['mac']);
	$rssi = $data_point['rssi'];
	$room = $data_point['room'];
	$time = $data_point['time'];

	$stmt = $db->prepare($query);
	$stmt->bindParam(':mac', $mac, SQLITE3_TEXT);
	$stmt->bindParam(':rssi', $rssi, SQLITE3_INTEGER);
	$stmt->bindParam(':room', $room, SQLITE3_INTEGER);
	$stmt->bindParam(':time', $time, SQLITE3_INTEGER);
	$stmt->execute();
}

// set up the database
$db = new PDO("sqlite:./db/data.db");
$db->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);

// read the POST body
$request_body = file_get_contents('php://input');

// decode the JSON
$data_points = json_decode($request_body, true);

// loop over the elements of the array, inserting each one into the db
foreach ($data_points as $data_point) {
	insert_data_point($data_point);
}

// respond with 204 No Content if everything goes well
http_response_code(204);

// TODO: add gzip compression support
// TODO: send http_response_code(400) for bad requests

?>
