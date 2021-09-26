<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['friendname']) && isset($_POST['username'])) {
    if ($db->dbConnect()) {
        if ($db->sendFriendRequest(addslashes($_POST['username']), addslashes($_POST['friendname']))) { 
            echo "Sent Request Successfully";
        } else echo "Error sending request";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>