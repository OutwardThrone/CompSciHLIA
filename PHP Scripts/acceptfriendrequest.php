<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['senderid']) && isset($_POST['friendname']) && isset($_POST['wasaccepted'])) {
    if ($db->dbConnect()) {
        if ($db->acceptFriendRequest(addslashes($_POST['senderid']), addslashes($_POST['friendname']), addslashes($_POST['wasaccepted']))) {
            echo "Successfully updated friend request";
        } else echo "Error updating friend request";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>