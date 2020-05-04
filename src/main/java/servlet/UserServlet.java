package servlet;

import com.google.gson.Gson;
import info.Message;
import security.PasswordHashing;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "UserServlet", urlPatterns = "/User")
public class UserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())); //Java 8 magic to collect all lines from a BufferedReader, in this case the request.

        //if not logged in send back!
        boolean logged_in = session.getAttribute("loggedIn").equals("true");
        if (!logged_in){
            response.sendRedirect("login.html");
        }

        String change_password = request.getParameter("change_pw");
        String password = "";

        String salted = "";
        String hashed_pw = "";

        //getting message for change password
        if(change_password.equals("True")) {

            Message reqMessage = gson.fromJson(reqBody, Message.class);
            String action = reqMessage.header; //"new_password"
            password = (String) reqMessage.body;
//            System.out.println("password: " + password);

            //only need new password
            //GET PASSWORD FROM FRONT END
            //password = request.getParameter("new_password");

            //generate salt and hashed pw
            salted = PasswordHashing.getRandomSalt();
            hashed_pw = PasswordHashing.hashPassword(password, salted);
            password ="";
        }

        try{
            Database db = new Database();
            db.fixUser(session.getAttribute("email").toString(), hashed_pw, salted);
            respWriter.println(gson.toJson(new Message("Password changed successfully")));

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
