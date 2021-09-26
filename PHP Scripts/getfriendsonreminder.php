<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['title']) && isset($_POST['description']) && isset($_POST['username'])) {
    if ($db->dbConnect()) {
        $res = $db->getFriendsOnReminder(addslashes($_POST['username']), addslashes($_POST['title']), addslashes($_POST['description'])); 
        if ($res != null) {
            echo "Success " . $res;
        } else echo "Error getting friends on reminder";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>