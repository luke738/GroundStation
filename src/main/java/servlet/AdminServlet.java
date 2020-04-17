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
import java.util.ArrayList;
import java.util.stream.Collectors;

@WebServlet(name = "AdminServlet", urlPatterns = "/Admin")
public class AdminServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())); //Java 8 magic to collect all lines from a BufferedReader, in this case the request.
        //change password, add admin, add/delete classcode
        String admin_actions = request.getParameter("actions");
        String user_email ="";
        String password = "";
        String classcode = "";
        String salted = "";
        String hashed_pw = "";

        Boolean admin = (Boolean) session.getAttribute("adminStatus");
        //if not an admin send back warning
        if (admin == false){
            //CHECK TO SEE IF THIS IS THE RIGHT REDIRECT
            response.sendRedirect("account_info.html");
        }
        else {
            //getting message for change password
            if (admin_actions.equals("change_password")) {
                //            Message reqMessage = gson.fromJson(reqBody, Message.class);
                //only need new password
                //GET PASSWORD FROM FRONT END
                password = request.getParameter("new_password");
                //generate salt and hashed pw
                salted = PasswordHashing.getRandomSalt();
                hashed_pw = PasswordHashing.hashPassword(password, salted);
                password = "";
            }
            //getting message for if add admin (proposed admin email)
            else if (admin_actions.equals("add_admin")) {
                //            Message reqMessage = gson.fromJson(reqBody, Message.class);
                //only need proposed admin email
                //GET EMAIL FROM FRONT END
                user_email = request.getParameter("user_email");
                ;
            }
            //getting message for delete class code ; need old class code
            else if (admin_actions.equals("delete_class_code")) {
                //            Message reqMessage = gson.fromJson(reqBody, Message.class);
                //only need deleted class_code
                //GET CLASS CODE
                classcode = request.getParameter("old_class_code");
                ;
            }
            //getting message for add class code ; need new class code
            else if (admin_actions.equals("add_class_code")) {
                //            Message reqMessage = gson.fromJson(reqBody, Message.class);
                //only need new class_code
                //GET CLASS CODE
                classcode = request.getParameter("new_class_code");
            } else if (admin_actions.equals("grab_class")) {
                classcode = request.getParameter("class_code");
            }
        }


        try{
            Database db = new Database();
            // check about password & password salt
            switch (admin_actions)
            {
                case "add_admin":
                    //email in db & not already admin
                    if(!db.checkUser(user_email) && db.isAdministrator(user_email)==-1) {
                        db.addAdministrator(user_email);
                        respWriter.println("Administrator, " + user_email + " added!");
                        respWriter.close();
                        return;
                    }
                    //email does not exist
                    else if(db.checkUser(user_email))
                    {
                        respWriter.println(gson.toJson(new Message("User email does not exist in database!")));
                        respWriter.close();
                        return;
                    }
                    //already admin
                    else{
                        respWriter.println(gson.toJson(new Message("User is already an administrator!")));
                        respWriter.close();
                        return;
                    }

                // delete class code
                case "delete_class_code":
                    //classcode exists in db for admin
                    if (db.checkClassCode(classcode))
                    {
                        //SHOULD WE ADD A WARNING LIKE: IF YOU DELETE THIS CLASS, x students will be removed?
                        db.deleteClassCode(db.isAdministrator(session.getAttribute("email").toString()));
                        respWriter.println(gson.toJson(new Message("Deleted Class Code")));
                    }
                    // Class Code does not exist in db
                    else if(! db.checkClassCode(classcode))
                    {
                        respWriter.println(gson.toJson(new Message("Not a valid class code!")));
                        respWriter.close();
                        return;
                    }
                //add class code
                case "add_class_code":
                    db.addClassCode(session.getAttribute("email").toString(), classcode);
                    respWriter.println(gson.toJson(new Message("Added Class Code")));
                //change password
                case "change_password":
                    db.fixUser(session.getAttribute("email").toString(), hashed_pw, salted);
                    respWriter.println(gson.toJson(new Message("Password changed successfully")));

                    //grab entire class
                case "grab_class":
                    ArrayList<String[]> wholeclass = db.grabClass(classcode);
                    respWriter.println(gson.toJson(new Message("class code "+classcode, wholeclass)));

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
