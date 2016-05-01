<?php
	require_once 'config.php';
	require_once 'library.php';

	$filter = $_GET['filter'];

	switch ($filter) {
		case "total":
			$query = "SELECT id FROM games";
			break;
		case "registered":
			$query = "SELECT id FROM games WHERE username!=''";
			break;
		case "anonymous":
			$query = "SELECT id FROM games WHERE username=''";
			break;
	}

	$games = db_query($query);

	echo mysqli_num_rows($games);
?>