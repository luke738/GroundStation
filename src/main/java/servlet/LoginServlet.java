package servlet;

import com.google.gson.Gson;
import security.PasswordHashing;
import info.Message;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;


@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet  {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())); //Java 8 magic to collect all lines from a BufferedReadder, in this case the request.

        Message reqMessage = gson.fromJson(reqBody, Message.class);
        String username = reqMessage.header;
        String password = (String) reqMessage.body;
        //telling whether it is a login or a signup
        String logOrsign = request.getParameter("signOrLog");
        try{
            Database db = new Database();
            // check about password & password salt with the thing david did
            switch (logOrsign)
            {
                case "login":
                    if (db.checkUser(username))
                    {
                        String[] pInfo = db.getPasswordInfo(username);
                        System.out.println(PasswordHashing.hashPassword(password, pInfo[0]));
                        // Log In Successful
                        if ((PasswordHashing.hashPassword(password, pInfo[0])).equals(pInfo[1]))
                        {
                            int userIDstore = db.getUserID(username);
                            //set attributes
                            session.setAttribute("hello", "Hello " + username);
                            session.setAttribute("userID", userIDstore);
                            respWriter.println(gson.toJson(new Message("LoggedIn", userIDstore)));
                        }
                        // Wrong password
                        else
                        {
                            respWriter.println(gson.toJson(new Message("Invalid Password!")));
                            respWriter.close();
                            return;
                        }
                    }
                    // User does not exist
                    else
                    {
                        respWriter.println(gson.toJson(new Message("Invalid Username!")));
                        respWriter.close();
                        return;
                    }
                    break;
                // New User
                case "signup":
                    // Valid username
                    if (!db.checkUser(username))
                    {
                        String salt = PasswordHashing.getRandomSalt();
                        db.createUser(username, PasswordHashing.hashPassword(password, salt), salt);

                        int userIDstore = db.getUserID(username);
                        session.setAttribute("hello", "Hello " + username);
                        session.setAttribute("userID", userIDstore);
                        //NEED TO CHANGE FOR VERIFICATION VIA ADMINISTRATOR EVENTUALLY
                        respWriter.println(gson.toJson(new Message("Created", userIDstore)));
                    }
                    // Invalid Username
                    else
                    {
                        respWriter.println(gson.toJson(new Message("Username already exists!")));
                        respWriter.close();
                        return;
                    }
                    break;
                case "verify":
                    if (session.getAttribute("userID") != null && Integer.parseInt(password) == (int)session.getAttribute("userID")) {
                        respWriter.println(gson.toJson(new Message("Verified")));
                        return;
                    }
                    else {
                        respWriter.println(gson.toJson(new Message("Unverified")));
                        return;
                    }
                    //break;
            }
        } catch(Exception e) { //Handle exceptions
            e.printStackTrace();
            respWriter.println(gson.toJson(new Message("Invalid Request!")));
            respWriter.close();
        }
        respWriter.close();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
