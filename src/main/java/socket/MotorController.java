package socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import connector.MessageListener;
import connector.MotorConnector;
import info.Message;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint(value = "/MotorControl", configurator = GetHttpSessionConfigurator.class)
public class MotorController implements MessageListener
{
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<>());
    private static Map<MotorConnector.MotorConnectorType, List<Session>> typeClients = Collections.synchronizedMap(new TreeMap<>());
    private Map<MotorConnector.MotorConnectorType, MotorConnector> connectors = Collections.synchronizedMap(new TreeMap<>());
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();
    String uhf_host;
    String sband_host;
    int port;

    @OnOpen
    public void open(Session session, EndpointConfig endpoint){
        if(!endpoint.getUserProperties().get("httpSession").equals(0))
        {
            HttpSession httpSession = (HttpSession) endpoint.getUserProperties().get("httpSession");
            if(httpSession.getAttribute("loggedIn")!=null && (Boolean) httpSession.getAttribute("loggedIn"))
            {
                session.getUserProperties().put("motor_first", true);
                clients.add(session);
                ServletContext context = httpSession.getServletContext();
                uhf_host = context.getInitParameter("uhf_host");
                sband_host = context.getInitParameter("sband_host");
                port = Integer.parseInt(context.getInitParameter("desktop_port"));
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session){
        JsonElement data = parser.parse(message);
        if((boolean) session.getUserProperties().get("motor_first")) {
            if(!data.getAsJsonObject().get("header").getAsString().equals("antenna_name")) {
                try
                {
                    session.getBasicRemote().sendText(gson.toJson(new Message("failure", "must_send_antenna_name_first")));
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return;
            }
            MotorConnector.MotorConnectorType antenna;
            try
            {
                antenna = MotorConnector.MotorConnectorType.valueOf(data.getAsJsonObject().get("body").getAsString().toUpperCase());
            }
            catch(IllegalArgumentException e) {
                e.printStackTrace();
                try
                {
                    session.getBasicRemote().sendText(gson.toJson(new Message("failure", "invalid_antenna_name")));
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return;
            }
            String host = null;
            if(antenna == MotorConnector.MotorConnectorType.UHF) {
                host = uhf_host;
            }
            else if(antenna == MotorConnector.MotorConnectorType.SBAND) {
                host = sband_host;
            }
            session.getUserProperties().put("motor_first", false);
            session.getUserProperties().put("antenna", antenna);
            typeClients.get(antenna).add(session);
            if(!connectors.containsKey(antenna)) {
                synchronized(MotorController.class) {
                    if(!connectors.containsKey(antenna)) {
                        connectors.put(antenna, new MotorConnector(antenna, host, port));
                    }
                }
            }
            connectors.get(antenna).addMessageListener(this);
            return;
        }
        if(data.getAsJsonObject().get("header").getAsString().equals("updateAzimuthElevation")) {
            Message m = new Message("updateAzEl", new int[]{data.getAsJsonObject().get("azimuth").getAsInt(), data.getAsJsonObject().get("elevation").getAsInt()});
            connectors.get((MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna")).sendData(m);
            try
            {
                session.getBasicRemote().sendText(gson.toJson(new Message("success")));
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        else {
            try
            {
                session.getBasicRemote().sendText(gson.toJson(new Message("failure", "invalid_command")));
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @OnClose
    public void close(Session session)
    {
        clients.remove(session);
        typeClients.get((MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna")).remove(session);
        for(MotorConnector connector : connectors.values()) {
            connector.removeMessageListener(this);
        }
    }

    @OnError
    public void error(Throwable error)
    {
        error.printStackTrace();
    }

    @Override
    public void messageReceived(Message m)
    {
        MotorConnector.MotorConnectorType antenna = MotorConnector.MotorConnectorType.valueOf(m.header);
        Message body = (Message) m.body;
        for(Session s : typeClients.get(antenna)) {
            try
            {
                s.getBasicRemote().sendText(gson.toJson(body));
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
