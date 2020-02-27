package servlet;

import info.EmailUtility;
import security.PasswordHashing;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A Java Servlet to handle requests to reset password for customer
 *
 * @author www.codejava.net
 *
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = "/ForgotPassword")

public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String page = "reset_password.jsp";
        request.getRequestDispatcher(page).forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter respWriter  = response.getWriter();
        String recipient = request.getParameter("email");
        Database db = new Database();
        String tempPassword ="";
        String message = "";
        //signal for if the user email exists in db
        Boolean allClear = false;

        ServletContext context = getServletContext();
        String host = context.getInitParameter("host");
        String port = context.getInitParameter("port");
        String email = context.getInitParameter("email");
        String name = context.getInitParameter("name");
        String pass = context.getInitParameter("pass");

        try {
            //check if user
            if(db.checkUser(recipient)) {
                allClear = true;
                System.out.println("HERE");
                tempPassword = PasswordHashing.tempPasswordCreator();
                String salty = PasswordHashing.getRandomSalt();
                String hashedpword = PasswordHashing.hashPassword(tempPassword, salty);
                System.out.println("HERE");
                db.fixUser(recipient, hashedpword, salty);
            }
            else {
                respWriter.println("Not valid email address");
            }

        } catch (Exception ex){
            ex.printStackTrace();
            message = "There was an error: " + ex.getMessage();
        }
        String subject = "Your Password has been reset";
        String content = "Hi, this is your new password: " + tempPassword;
        content += "\nNote: for security reason, "
                + "you must change your password after logging in.";

        //send email
        try {
            if(allClear) {
                EmailUtility.sendEmail(host, port, email, name, pass, recipient, subject, content);
                message = "Your password has been reset. Please check your e-mail.";
                System.out.println("IM HERE");
                //respWriter.println(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            message = "There was an error: " + ex.getMessage();
        } //finally {
            //request.setAttribute("message", message);
           // equest.getRequestDispatcher("message.jsp").forward(request, response);
       // }
        respWriter.println(message);
    }
}
