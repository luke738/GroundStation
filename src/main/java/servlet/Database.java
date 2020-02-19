package servlet;

import java.sql.*;

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

    public Boolean createUser(String username, String passwordHash, String salt, String name, String classcode) {
        if (checkUser(username)) {
            return false;
        }
        if(!checkClassCode(classcode)){
            return false;
        }
        try {
            ps = conn.prepareStatement("INSERT INTO UserInfo(username, pw, salt, name) VALUES(?,?,?,?)");
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setString(4, name);
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
            ps = conn.prepareStatement("UPDATE UserInfo SET passwordHash = ? AND salt = ? WHERE userID = ? AND username = ?");
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, userID);
            ps.setString(4, username);
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException in function \"validate\" fixuser");
            e.printStackTrace();
        }
        return false;
    }

 /*   public Boolean checkPasswordCode(String pcode, String username){
       try {
           int userID = getUserID(username);
           ps = conn.prepareStatement("SELECT u.userID FROM UserInfo u WHERE pcode=? and username=?");
           ps.setString(1, pcode);
           ps.setString(2, username);
           rs = ps.executeQuery();
           //no password code with this user
           if (!rs.next()) {
               return false;
           }

       }
       catch (SQLException e) {
           System.out.println("SQLException in function \"validate\" in checkpasswodcode");
           e.printStackTrace();
       }
        return true;
    }
    */


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
}