<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['friendusername']) && isset($_POST['title']) && isset($_POST['description'])) {
    if ($db->dbConnect()) {
        if ($db->addFriendToReminder(addslashes($_POST['friendusername']), addslashes($_POST['title']), addslashes($_POST['description']))) {
            echo "Successfully invited ". $_POST['friendusername'];
        } else echo "Error inviting " . $_POST['friendusername'];
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>