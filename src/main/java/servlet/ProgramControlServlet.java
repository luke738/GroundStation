package servlet;

import com.google.gson.Gson;
import info.JavaConnection;
import info.Message;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.Collectors;

@WebServlet(name = "ProgramControlServlet", urlPatterns = "/ProgramControl")
public class ProgramControlServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Message reqSuperMessage = gson.fromJson(reqBody, Message.class);
        Message reqMessage = gson.fromJson((String) reqSuperMessage.body, Message.class);


        //if not logged in redirect
        boolean logged_in = (boolean) session.getAttribute("loggedIn");
        if (!logged_in){
            response.sendRedirect("login.html");
            return;
        }

        JavaConnection desktopConn;
        ServletContext context = getServletContext();
        if(reqSuperMessage.header.equals("sband")) {
            desktopConn = new JavaConnection(new Socket(context.getInitParameter("sband_host"), Integer.parseInt(context.getInitParameter("desktop_port")))); //TODO: Set to SBAND IP
        }
        else if(reqSuperMessage.header.equals("uhf")) {
            desktopConn = new JavaConnection(new Socket(context.getInitParameter("uhf_host"), Integer.parseInt(context.getInitParameter("desktop_port")))); //TODO: Set to UHF IP
        }
        else {
            respWriter.println(gson.toJson(new Message("invalid_computer")));
            return;
        }


        switch(reqMessage.header) {
            case "start_program": {
                desktopConn.send(reqMessage);
                Message desktopResp = desktopConn.receive(Message.class);
                if(desktopResp.header.equals("success")) {
                    respWriter.println(gson.toJson(new Message("launch_success")));
                }
                else {
                    respWriter.println(gson.toJson(new Message("launch_failure",desktopResp.body)));
                }
            }
            break;
            case "stop_program": {
                desktopConn.send(reqMessage);
                Message desktopResp = desktopConn.receive(Message.class);
                if(desktopResp.header.equals("success")) {
                    respWriter.println(gson.toJson(new Message("kill_success")));
                }
                else {
                    respWriter.println(gson.toJson(new Message("kill_failure",desktopResp.body)));
                }
            }
            break;
            case "shutdown": {
                if(session.getAttribute("isAdmin").equals("true")) {
                    desktopConn.send(reqMessage);
                    Message desktopResp = desktopConn.receive(Message.class);
                    if(desktopResp.header.equals("shutdown_ack")) {
                        respWriter.println(gson.toJson(new Message("shutdown_success")));
                    }
                    else {
                        respWriter.println(gson.toJson(new Message("shutdown_failure",desktopResp.body)));
                    }
                }
                else {
                    respWriter.println(gson.toJson(new Message("shutdown_failure","must_be_admin")));
                }
            }
            break;
            default: {
                respWriter.println(gson.toJson(new Message("invalid_request")));
            }
        }

        desktopConn.close();
    }
}
