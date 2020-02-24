package servlet;

import com.google.gson.Gson;
import info.Message;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "SchedulingServlet")
public class SchedulingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String username = "";
        String dated = "";
        String time = "";
        String schedOrUnsched = "schedule";
        try{
            Database db = new Database();

            switch (schedOrUnsched)
            {
                case "sched":
                    if (db.TimeSlotAvail(time, dated))
                    {
                        db.scheduleTimeSlot(username, time, dated);
                        //update session attributes for the calendar
                        respWriter.println(gson.toJson(new Message("Time Slot scheduled!")));
                    }

                    else
                    {
                        respWriter.println(gson.toJson(new Message("Time slot taken!")));
                        respWriter.close();
                        return;
                    }
                    break;

                case "unschedule":
                    //if not an admin
                    db.unscheduleTimeSlot(username, time, dated);
                    respWriter.println(gson.toJson(new Message("Time Slot unscheduled!")));
                    //if an admin
                    break;


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
