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

/**
 * A Java Servlet to handle requests to reset password for customer
 *
 * @author www.codejava.net
 *
 */
@WebServlet(name = "ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String host;
    private String port;
    private String email;
    private String name;
    private String pass;

    public void init() {
        // reads SMTP server setting from web.xml file
        ServletContext context = getServletContext();
        host = context.getInitParameter("host");
        port = context.getInitParameter("port");
        email = context.getInitParameter("email");
        name = context.getInitParameter("name");
        pass = context.getInitParameter("pass");
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String page = "reset_password.jsp";
        request.getRequestDispatcher(page).forward(request, response);

    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recipient = request.getParameter("email");
        Database db = new Database();
        String tempPassword ="";
        String message = "";
        Boolean allClear = false;
        try {
            if(!db.checkUser(recipient)) {
                allClear = true;
                tempPassword = PasswordHashing.tempPasswordCreator();
                String salty = PasswordHashing.getRandomSalt();
                String hashedpword = PasswordHashing.hashPassword(tempPassword, salty);
                db.fixUser(recipient, hashedpword, salty);
            }

        } catch (Exception ex){
            ex.printStackTrace();
            message = "There was an error: " + ex.getMessage();
        }
        String subject = "Your Password has been reset";
        String content = "Hi, this is your new password: " + tempPassword;
        content += "\nNote: for security reason, "
                + "you must change your password after logging in.";


        try {
            if(allClear) {
                EmailUtility.sendEmail(host, port, email, name, pass, recipient, subject, content);
                message = "Your password has been reset. Please check your e-mail.";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            message = "There was an error: " + ex.getMessage();
        } finally {
            request.setAttribute("message", message);
            request.getRequestDispatcher("message.jsp").forward(request, response);
        }
    }


}
