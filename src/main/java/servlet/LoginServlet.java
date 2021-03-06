package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.Message;
import security.PasswordHashing;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())); //Java 8 magic to collect all lines from a BufferedReader, in this case the request.
        //telling whether it is a login or a signup
        String logOrsign = request.getParameter("signOrLog");
        String username;
        String password = "";
        //only for sign up
        String name ="";
        String classcode = "";

        //getting message for if login (only username & password
        if(logOrsign.equals("login")) {
            Message reqMessage = gson.fromJson(reqBody, Message.class);
            username = reqMessage.header;
            password = (String) reqMessage.body;
        }
        //getting message for if signup(name, email, password, class code)
        else {
            Message reqMessage = gson.fromJson(reqBody, Message.class);
            username = reqMessage.header;
            //Interact with the raw JSON to determine the type of the object via unique fields
            JsonObject signupinfo = new JsonParser().parse((String) reqMessage.body).getAsJsonObject();
            password = signupinfo.get("password").getAsString();
            classcode = signupinfo.get("classcode").getAsString();
            name = signupinfo.get("name").getAsString();
        }


        try{
            Database db = new Database();
            // check about password & password salt
            switch (logOrsign)
            {
                case "login":
                    if (db.checkUser(username))
                    {
                        String[] pInfo = db.getPasswordInfo(username);
                        // System.out.println(PasswordHashing.hashPassword(password, pInfo[0]));
                        // Log In Successful
                        if ((PasswordHashing.hashPassword(password, pInfo[0])).equals(pInfo[1]))
                        {
                            int userIDstore = db.getUserID(username);
                            boolean isAdmin = false;
                            if (db.isAdministrator(username)!=-1){
                                isAdmin = true;
                            }

                            classcode = db.getUserClassCode(username);

                            Map<String,String> userToFrontend = new HashMap<>();

                            //set attributes
                            session.setAttribute("hello", "Hello " + username);
                            session.setAttribute("email", username);
                            session.setAttribute("userID", userIDstore);
                            session.setAttribute("loggedIn",true);
                            session.setAttribute("isAdmin",Boolean.toString(isAdmin));

                            userToFrontend.put("email", username);
                            userToFrontend.put("userID", userIDstore+"");
                            userToFrontend.put("loggedIn", "true");
                            userToFrontend.put("classcode", classcode);
                            userToFrontend.put("isAdmin", Boolean.toString(isAdmin));

                            respWriter.println(gson.toJson(new Message("LoggedIn", userToFrontend)));
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
                        respWriter.println(gson.toJson(new Message("Invalid Email!")));
                        respWriter.close();
                        return;
                    }
                    break;
                // New User
                case "signup":
                    //correct class code & valid email
                    if (db.checkClassCode(classcode) && !db.checkUser(username))
                    {
                        String salt = PasswordHashing.getRandomSalt();
                        db.createUser(username, PasswordHashing.hashPassword(password, salt), salt, name, classcode);

                        int userIDstore = db.getUserID(username);
                        boolean isAdmin = false;

                        Map<String,String> userToFrontend = new HashMap<>();
                        session.setAttribute("hello", "Hello " + username);
                        session.setAttribute("email", username);
                        session.setAttribute("userID", userIDstore);
                        session.setAttribute("loggedIn",true);
                        session.setAttribute("isAdmin",Boolean.toString(isAdmin));

                        userToFrontend.put("email", username);
                        userToFrontend.put("userID", userIDstore+"");
                        userToFrontend.put("loggedIn", "true");
                        userToFrontend.put("classcode", classcode);
                        userToFrontend.put("isAdmin", Boolean.toString(isAdmin));

                        //NEED TO CHANGE FOR VERIFICATION VIA ADMINISTRATOR EVENTUALLY
                        respWriter.println(gson.toJson(new Message("Created", userToFrontend)));
                    }

                    // Invalid Username But Correct Class Code
                    else if(db.checkUser(username) && db.checkClassCode(classcode))
                    {
                        respWriter.println(gson.toJson(new Message("Email already exists!")));
                        respWriter.close();
                        return;
                    }

                    // Invalid Class Code
                    else if(!db.checkClassCode(classcode) )
                    {
                        respWriter.println(gson.toJson(new Message("Invalid class code!")));
                        respWriter.close();
                        return;
                    }
                    break;

                case "verify":
                    if(session.getAttribute("userID") != null && Integer.parseInt(password) == (int)session.getAttribute("userID")) {
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
