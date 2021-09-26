<?php
require "DataBaseConfig.php";

class DataBase
{
    public $connect;
    public $data;
    private $sql;
    protected $servername;
    protected $username;
    protected $password;
    protected $databasename;

    public function __construct()
    {
        $this->connect = null;
        $this->data = null;
        $this->sql = null;
        $dbc = new DataBaseConfig();
        $this->servername = $dbc->servername;
        $this->username = $dbc->username;
        $this->password = $dbc->password;
        $this->databasename = $dbc->databasename;
    }

    function dbConnect()
    {
        $this->connect = mysqli_connect($this->servername, $this->username, $this->password, $this->databasename);
        return $this->connect;
    }

    function prepareData($data)
    {
        return mysqli_real_escape_string($this->connect, stripslashes(htmlspecialchars($data)));
    }

    function logIn($table, $username, $password)
    {
        $username = $this->prepareData($username);
        $password = $this->prepareData($password);
        $this->sql = "select * from " . $table . " where username = '" . $username . "'";
        $result = mysqli_query($this->connect, $this->sql);
        $row = mysqli_fetch_assoc($result);
        if (mysqli_num_rows($result) != 0) {
            $dbusername = $row['username'];
            $dbpassword = $row['password'];
            if ($dbusername == $username && password_verify($password, $dbpassword)) {
                $otherInfo = $this->getUserData($table, $username, $password);
                $login = true;
            } else $login = false;
        } else $login = false;

        if ($login) return $otherInfo;
        else return null;
    }

    function getUserData($table, $username, $password) {
        $this->sql = "select * from " . $table . " where username = '" . $username . "'";
        $result = mysqli_query($this->connect, $this->sql);
        $row = mysqli_fetch_assoc($result);
        $dbusername = $row['username'];
        $dbpassword = $row['password'];
        if ($dbusername == $username && password_verify($password, $dbpassword)) {
            $dbfullname = $row['fullname'];
            $dbemail = $row['email'];
            $obj = json_encode(array('fullname' => $dbfullname, 'email' => $dbemail), JSON_FORCE_OBJECT);
            return $obj;
        }
        return null;
    }

    function sendFriendRequest($uname, $fname) {
        $this->sql = "select `id` from `users` where `username` = '" . $fname . "'";
        $res = mysqli_query($this->connect, $this->sql);
        if ($res->num_rows < 1) {
            echo "Username invalid. ";
            return false;
        } else {

            //check if they're already friends
            $uID = $this->getUserID($uname);
            $fID = $this->getUserID($fname);

            if ($uID == $fID) {
                echo "Cannot sent friend request to yourself. ";
                return false;
            }

            $this->sql = "select * from `friends` where `senderid` = " . $uID . " and `friendid` = " . $fID;
            $res1 = mysqli_query($this->connect, $this->sql);
    
            $this->sql = "select * from `friends` where senderid` = " . $uID . " and `friendid` = " . $fID;
            $res2 = mysqli_query($this->connect, $this->sql);

            if ($res) { 
                if (mysqli_num_rows($res1) > 0) {
                    echo "Already Friends. ";
                    return false;
                }
            }
            if ($res2) { 
                if (mysqli_num_rows($res2) > 0) {
                    echo "Already Friends. ";
                    return false;
                }
            }

            $friendID = mysqli_fetch_assoc($res)['id'];
            $userID = $this->getUserID($uname);

            $this->sql = "insert into `friends` (senderid, friendid, requestaccepted) values ('" . $userID . "', '" . $friendID . "', 0)"; //requestaccepted defaults to false
            if (mysqli_query($this->connect, $this->sql)) {
                return true;
            } else return false;
        }
    }

    function updateCompleted($username, $title, $description, $completed) {
        $id = $this->getReminderID($title, $description);
        $userID = $this->getUserID($username);
        $this->sql = "update `users_reminders` set `completed` = " . $completed . " where `reminderid` = " . $id . " and `userid` = " . $userID;
        if (mysqli_query($this->connect, $this->sql)) {
            return true;
        } else return false;
    }

    function getReminders($username) {
        $userID = $this->getUserID($username);
        $this->sql = "select * from users_reminders where userid = '" . $userID . "'";
        $res = mysqli_query($this->connect, $this->sql);
        $reminderIDs = array();
        $idToCompleted = array();
        if ($res->num_rows > 0) {
            while ($row = $res->fetch_assoc()) {
                array_push($reminderIDs, $row['reminderid']);
                array_push($idToCompleted, $row['completed']);
            }
        }
        $reminders = '{'; 
        $id = current($reminderIDs);
        $isComplete = current($idToCompleted);
        $c = 0;
        while ($id != null) {
            //do something with id
            $this->sql = "select * from reminders where id = '" . $id . "'";
            $res = mysqli_query($this->connect, $this->sql);
            if ($res->num_rows > 0) {
                while ($row = $res->fetch_assoc()) {
                    $singleReminder = json_encode(array('title' => $row['title'], 'description' => $row['description'], 'date' => $row['date'], 'weekly' => $row['weekly'], 'daily' => $row['daily'], 'completed' => $isComplete), JSON_FORCE_OBJECT);
                    if ($c != 0) {
                        $reminders = $reminders . ',"' . $c . '":' . $singleReminder;
                    } else $reminders = $reminders . '"' . $c . '":' . $singleReminder;

                                        
                    $c += 1;
                }
            }
            $id = next($reminderIDs);
            $isComplete = next($idToCompleted);
        }
        $reminders = $reminders . "}";

        return $reminders;
    }

    function getFriendsOnReminder($username, $title, $description) {
        $reminderID = $this->getReminderID($title, $description);
        $userID = $this->getUserID($username);

        $this->sql = "select * from `users_reminders` where `reminderid` = " . $reminderID . " and `userid` != " . $userID;
        $res = mysqli_query($this->connect, $this->sql);
        $string = "{";
        $c = 1;
        if ($res) {
            while ($row = $res->fetch_assoc()) {
                $friendID = $row['userid'];
                $isOwner = $row['isOwner'];
                $friendName = $this->getUsername($friendID);
                $friendFullname = $this->getFullname($friendID);
                $completed = $row['completed'];

                $string = $string . "'" . $friendName . "': {'isOwner': '" . $isOwner . "', 'fullname': '" . $friendFullname . "', 'completed': " . $completed . "}"; 
                
                if ($c != $res->num_rows) {
                    $string = $string . ", ";
                }

                $c++;
            }
        } else {
            return null;
        }
        $string = $string . "}";
        return $string;
    }

    function deleteReminder($username, $title, $description) {
        $userID = $this->getUserID($username);
        $id = $this->getReminderID($title, $description);
        
        $this->sql = "DELETE FROM `users_reminders` WHERE `reminderid` = " . $id . " and `userid` = " . $userID;
        $res1 = mysqli_query($this->connect, $this->sql);

        $this->sql = "select * from `users_reminders` where reminderid = " . $id;
        $res2 = mysqli_query($this->connect, $this->sql);
        if ($res2) { 
            if (mysqli_num_rows($res2) > 0) {
                return true;
            }
        }
        $this->sql = "DELETE FROM `reminders` WHERE `id` = " . $id;
        return mysqli_query($this->connect, $this->sql);
    }

    function acceptFriendRequest($senderID, $friendName, $wasAccepted) {
        $friendID = $this->getUserID($friendName);
        if ($wasAccepted == "1") {
            $this->sql = "update `friends` set `requestaccepted` = 1 where `friendid` = '" . $friendID . "' and `senderid` = '" . $senderID . "'";
            return mysqli_query($this->connect, $this->sql); 
        } else {
            $this->sql = "delete from `friends` where `friendid` = '" . $friendID . "' and `senderid` = '" . $senderID . "'";
            return mysqli_query($this->connect, $this->sql); 
        }
    }

    function getFriends($username) {
        //request accepted must be 1

        $friendIDs = array();

        $id = $this->getUserID($username);
        //get senderid where friendid matches
        $this->sql = "select `senderid` from `friends` where `friendid` = '" . $id . "' and `requestaccepted` = 1";
        $res = mysqli_query($this->connect, $this->sql);
        if ($res->num_rows > 0) {
            while (($row = $res->fetch_assoc()) != null) {
                array_push($friendIDs, $row['senderid']);
            }
        }

        //get friendid where senderid matches
        $this->sql = "select `friendid` from `friends` where `senderid` = '" . $id . "' and `requestaccepted` = 1";
        $res = mysqli_query($this->connect, $this->sql);
        if ($res->num_rows > 0) {
            while (($row = $res->fetch_assoc()) != null) {
                array_push($friendIDs, $row['friendid']);
            }
        }

        $friends = "{";
        $fID = current($friendIDs);
        $c = 1;

        //get the friends' data corresponding to the friendIDs in the array
        while ($fID != null) {
            $this->sql = "select * from `users` where `id` = " . $fID;
            $row = mysqli_fetch_assoc(mysqli_query($this->connect, $this->sql));
            $friendJSON = json_encode(array('username' => $row['username'], 'email' => $row['email'], 'fullname' => $row['fullname']), JSON_FORCE_OBJECT);

            $friends = $friends . "'" . $fID . "': " . $friendJSON;

            if ($c != count($friendIDs)) {
                $friends = $friends . ", ";
            }
            $c++;
            $fID = next($friendIDs);
        }

        $friends = $friends . "}";
        return $friends;
    }

    function removeFriend($username, $friendID) {
        $userID = $this->getUserID($username);

        $this->sql = "delete from `friends` where `senderid` = " . $userID . " and `friendid` = " . $friendID;
        $res1 = mysqli_query($this->connect, $this->sql);

        $this->sql = "delete from `friends` where `senderid` = " . $friendID . " and `friendid` = " . $userID;
        $res2 = mysqli_query($this->connect, $this->sql);
        
        return $res1 || $res2;
    }

    function getFriendRequests($username) {
        $userID = $this->getUserID($username);
        $this->sql = "select * from `friends` where `friendid` = " . $userID . " and `requestaccepted` = 0"; 
        $res = mysqli_query($this->connect, $this->sql);
        
        $senderIDs = array();
        $senders = '{';
        $c = 1;
        if ($res->num_rows > 0) {
            while ($row = $res->fetch_assoc()) {
                array_push($senderIDs, $row['senderid']);
            }
            $currentID = current($senderIDs);
            while ($currentID != null) {
                $this->sql = "select * from `users` where `id` = " . $currentID;
                $row = mysqli_fetch_assoc(mysqli_query($this->connect, $this->sql));

                $senders = $senders . "'" . $currentID . "': '" . $row['username'] . "'";

                if ($c != count($senderIDs)) {
                    $senders = $senders . ", ";
                }

                $currentID = next($senderIDs);
                $c+=1;
            }
            $senders = $senders . "}";
            return $senders;
                    
        } else {
            echo "No friend requests. ";
        }
    }

    function addFriendToReminder($friendName, $title, $description) {
        $reminderID = $this->getReminderID($title, $description);
        $friendID = $this->getUserID($friendName);
        $this->sql = "INSERT INTO `users_reminders` (userid, reminderid, isOwner, completed) VALUES ('" . $friendID . "', '" . $reminderID . "', 0, 0)";
        return mysqli_query($this->connect, $this->sql);
    }

    function getReminderID($title, $description) {
        $this->sql = "select id from reminders where title = '" . $title . "' and description = '" . $description . "'";
        $res = mysqli_query($this->connect, $this->sql);
        $userID = mysqli_fetch_assoc($res)['id'];
        return $userID;
    }

    function getUserID($username) {
        $this->sql = "select id from users where username = '" . $username . "'";
        $res = mysqli_query($this->connect, $this->sql);
        $userID = mysqli_fetch_assoc($res)['id'];
        return $userID;
    }

    function getUsername($userID) {
        $this->sql = "select `username` from users where `id` = " . $userID;
        $res = mysqli_query($this->connect, $this->sql);
        $username = mysqli_fetch_assoc($res)['username'];
        return $username;
    }

    function getFullname($userID) {
        $this->sql = "select `fullname` from users where `id` = " . $userID;
        $res = mysqli_query($this->connect, $this->sql);
        $username = mysqli_fetch_assoc($res)['fullname'];
        return $username;
    }

    function reminderUpload($table, $username, $title, $des, $date, $weekly, $daily) {
        $title = $this->prepareData($title);
        $des = $this->prepareData($des);
        $weekly = $this->prepareData($weekly);
        $daily = $this->prepareData($daily);
        $date = $this->prepareData($date);
        $this->sql = "INSERT INTO " . $table . " (title, description, date, weekly, daily) VALUES ('" . $title . "','" . $des . "','" . $date . "','" . $weekly . "','" . $daily . "')"; //completed defaults at false
        if (mysqli_query($this->connect, $this->sql)) {
            $reminderID = $this->connect->insert_id;
            $userID = $this->getUserID($username);

            $this->sql = "INSERT INTO users_reminders (userid, reminderid, isOwner, completed) VALUES ('". $userID . "','" . $reminderID . "', 1, 0)";
            if (mysqli_query($this->connect, $this->sql)) {
                return true;
            } else return false;
        } else return false;
    }

    function signUp($table, $fullname, $email, $username, $password)
    {
        $fullname = $this->prepareData($fullname);
        $username = $this->prepareData($username);
        $password = $this->prepareData($password);
        $email = $this->prepareData($email);
        $password = password_hash($password, PASSWORD_DEFAULT);
        $this->sql =
            "INSERT INTO " . $table . " (fullname, username, password, email) VALUES ('" . $fullname . "','" . $username . "','" . $password . "','" . $email . "')";
        if (mysqli_query($this->connect, $this->sql)) {
            return true;
        } else return false;
    }

}

?>
