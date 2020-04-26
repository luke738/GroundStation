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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.Collectors;

/**
  * A servlet for downloading tle data from space-track
  * @author space-track.org
  *
  */
@WebServlet(name = "TLEServlet", urlPatterns = "/TLEData")
public class TLEServlet extends HttpServlet {
    //zoneID = los angeles
    //public static ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
    //zone date time
    //public static ZonedDateTime TLE_download_time = ZonedDateTime.now(zoneId).minusMinutes(90);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //when click the button get the info, write to a file, "success"
        //https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/format/3le
        //ZonedDateTime zdt = ZonedDateTime.now( zoneId );
        Database db = new Database();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Message reqMessage = gson.fromJson(reqBody, Message.class);
        //if within 60 minutes of last TLE download, the server will not allow TLE download
        if (db.TLE_status() == false){
            String last_req = db.getRecentTLETime();
            respWriter.println(gson.toJson(new Message("Cannot pull TLE data within 1 hr of last request!", "Last request was at ") + last_req));
        }
        else {
            JavaConnection desktopConn;
            ServletContext context = getServletContext();
            if(reqMessage.header.equals("sband")) {
                desktopConn = new JavaConnection(new Socket(context.getInitParameter("sband_host"), Integer.parseInt(context.getInitParameter("desktop_port"))));
            }
            else if(reqMessage.header.equals("uhf")) {
                desktopConn = new JavaConnection(new Socket(context.getInitParameter("uhf_host"), Integer.parseInt(context.getInitParameter("desktop_port"))));
            }
            else {
                respWriter.println(gson.toJson(new Message("invalid_computer")));
                return;
            }
            desktopConn.send(new Message("tle_download"));
            Message desktopResp = desktopConn.receive(Message.class);
            if(desktopResp.header.equals("download_success")) {
                //adding current time to the TLE db
                db.addCurrentTLETime();
                respWriter.println(gson.toJson(new Message("tle_success")));
            }
            else {
                respWriter.println(gson.toJson(new Message("tle_failure", desktopResp.body)));
            }
        }

    }

}
