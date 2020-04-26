package servlet;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private Connection conn;
    private Statement st;
    private PreparedStatement ps;
    private ResultSet rs;

    public Database() {
        conn = null;
        ps = null;
        rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/groundstation", "root", "root1234!");
        } catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
        } catch (ClassNotFoundException cnfe) {
            System.out.println("ClassNotFoundException: " + cnfe.getMessage());
        }

    }

    public Boolean checkUser(String username) {
        try {
            ps = conn.prepareStatement("SELECT u.userID FROM UserInfo u WHERE username=?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return true;

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }

        return false;
    }


    public String[] getPasswordInfo(String username) {
        try {
            if (checkUser(username)) {
                int userID = getUserID(username);
                ps = conn.prepareStatement("SELECT u.pw, u.salt FROM UserInfo u WHERE username=? AND userID =?");
                ps.setString(1, username);
                ps.setInt(2, userID);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String password = rs.getString("pw");
                    String salt = rs.getString("salt");
                    String[] pINfo = new String[2];
                    pINfo[0] = salt;
                    pINfo[1] = password;
                    return pINfo;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return new String[]{"", ""};
    }

    public int getUserID(String username) {
        try {
            ps = conn.prepareStatement("SELECT u.userID FROM UserInfo u WHERE username=?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("userID");
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return -1;
    }
    //change the classcode of UserInfo table when user is made an admin
    //the new admin's UserInfo table classcode will be the admins Administrator table AdminID
    public void changeClassCode(String username){
        try{
            int userID = getUserID(username);
            int adminCode = isAdministrator(username);
            if (isAdministrator(username)==-1){
                return;
            }
            ps = conn.prepareStatement("UPDATE UserInfo SET classcode = ? WHERE userID=? AND username=?");
            ps.setInt(1,adminCode);
            ps.setInt(2,userID);
            ps.setString(3,username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException in function \"VALIDATE\"");
            e.printStackTrace();
        }

    }

    //returns AdminID if exists; returns -1 if not an admin
    public int isAdministrator(String username){
        try{
            int userID = getUserID(username);
            int AdminID = -1;
            ps = conn.prepareStatement("SELECT a.AdminID FROM Administrators a WHERE userID=? ");
            ps.setInt(1,userID);
            rs = ps.executeQuery();
            if(rs.next()){
                AdminID = rs.getInt("AdminID");
            }
            return AdminID;
        } catch (SQLException e) {
            System.out.println("SQLException in function \"VALIDATE\"");
            e.printStackTrace();
        }
        return -1;
    }

    public Boolean addAdministrator(String username){
        try {
            int userID = getUserID(username);
            if(isAdministrator(username)!=-1) {
                ps = conn.prepareStatement("INSERT INTO Administrators(userID) VALUES(?)");
                ps.setInt(1, userID);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return false;
    }

        public Boolean createUser(String username, String passwordHash, String salt, String name, String classcode) {
        if (checkUser(username)) {
            return false;
        }
        if(!checkClassCode(classcode)){
            return false;
        }
        try {
            ps = conn.prepareStatement("INSERT INTO UserInfo(username, pw, salt, name, classcode) VALUES(?,?,?,?,?)");
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setString(4, name);
            ps.setString(5, classcode);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return false;
    }

    public Boolean fixUser(String username, String passwordHash, String salt){
        try {
            int userID = getUserID(username);
            ps = conn.prepareStatement("UPDATE UserInfo SET pw = ?, salt = ? WHERE userID = ? AND username = ?");
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, userID);
            ps.setString(4, username);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\" fixuser");
            e.printStackTrace();
        }
        return false;
    }



    public Boolean checkClassCode(String classcode) {
        try {
            ps = conn.prepareStatement("SELECT a.userID FROM Administrators a WHERE classcode=?");
            ps.setString(1, classcode);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return true;

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }

        return false;
    }
    //adds a classcode into the the admin's section of classcodes
    public Boolean addClassCode(String username, String classcode){
        try {
            int userID = getUserID(username);
            int adminID = isAdministrator(username);
            if(isAdministrator(username) != -1) {
                ps = conn.prepareStatement("SELECT a.classcode, a.AdminID FROM Administrators a WHERE userID=?");
                ps.setInt(1, userID);
                rs = ps.executeQuery();
                if(rs.next()){
                    deleteClassCode(adminID);
                }
                ps = conn.prepareStatement("UPDATE Administrators SET classcode = ? WHERE AdminID=?");
                ps.setString(1,classcode);
                ps.setInt(2,adminID);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return false;
    }

    public void deleteClassCode(int adminID) {
        try{
            ps = conn.prepareStatement("UPDATE Administrators SET classcode = ? WHERE AdminID=? ");
            ps.setString(1, "");
            ps.setInt(2,adminID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
    }
    //given the classcode, this returns an arraylist of strings of usernames
    public ArrayList<String[]> grabClass(String classcode) {
        ArrayList <String[]> wholeClass = new ArrayList<>();

        PreparedStatement ps1;
        ResultSet rs1;

        try {
            ps1 = conn.prepareStatement("SELECT * FROM UserInfo WHERE classcode=?;");
            ps1.setString(1, classcode);
            rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String name = rs1.getString("name");
                String email = rs1.getString("username");
                String status = "" + (isAdministrator(email));

                String[] user = {name,email,status};
                wholeClass.add(user);
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return wholeClass;
    }
    //Method: returns class code of user given the username (aka email)
    public String getUserClassCode(String username) {
        try {
            ps = conn.prepareStatement("SELECT u.classcode FROM UserInfo u WHERE username=?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("classcode");
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return "";
    }
    //Method: check if TLE has been downloaded in the last hour
    public boolean TLE_status() {
        try {
            ps = conn.prepareStatement("SELECT TIMEDIFF(CURRENT_TIMESTAMP, (SELECT TLE_dt from TLE_times ORDER BY tle_id DESC LIMIT 0, 1));");
            rs = ps.executeQuery();
            //hrs: mins: seconds
            if (rs.next()) {
               String time_diff = rs.getString("td");
               int ind = time_diff.indexOf(":");
               int hours = Integer.parseInt((time_diff.substring(0, ind)));
               if (hours < 1){
                   return true;
               }
               return false;
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return false;
    }
    //add recent TLE download time to TLE db
    public void addCurrentTLETime(){
        try {
            ps = conn.prepareStatement("INSERT INTO GroundStation.TLE_times(TLE_dt) VALUES(CURRENT_TIMESTAMP);");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
    }
    public String getRecentTLETime(){
        try {
            ps = conn.prepareStatement("SELECT TLE_dt from TLE_times ORDER BY tle_id DESC LIMIT 0, 1));");
            rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("TLE_dt");
            }
            return "";
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return "";
    }

    //grab all of the TLE download times :
    public ArrayList<String> grabTLEs() {
        ArrayList <String> tles = new ArrayList<>();
        PreparedStatement ps1;
        ResultSet rs1;

        try {
            ps1 = conn.prepareStatement("SELECT TLE_dt FROM TLE_times;");
            rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String dt = rs1.getString("TLE_dt");
                tles.add(dt);
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\"");
            e.printStackTrace();
        }
        return tles;
    }

}