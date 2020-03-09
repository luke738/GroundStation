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

    public ArrayList<String[]> grabClass(String classcode) {
        ArrayList <String[]> wholeClass = new ArrayList<>();

        try {
            ps = conn.prepareStatement("SELECT * FROM userinfo WHERE classcode=?;");
            ps.setString(1, classcode);
            rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("username");
                String status = rs.getString("classcode");

                String[] user = {name,email,status};
                wholeClass.add(user);
            }

        } catch (SQLException e) {
            System.out.println("SQLException in function \"grabClass\"");
            e.printStackTrace();
        }
        return wholeClass;
    }

}