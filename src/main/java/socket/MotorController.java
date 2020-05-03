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
                session.getUserProperties().put("isAdmin", httpSession.getAttribute("isAdmin"));
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
                wrappedSend(session, gson.toJson(new Message("failure", "must_send_antenna_name_first")));
                return;
            }
            MotorConnector.MotorConnectorType antenna;
            try
            {
                antenna = MotorConnector.MotorConnectorType.valueOf(data.getAsJsonObject().get("body").getAsString().toUpperCase());
            }
            catch(IllegalArgumentException e) {
                e.printStackTrace();
                wrappedSend(session, gson.toJson(new Message("failure", "invalid_antenna_name")));
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
            if(!typeClients.containsKey(antenna)) {
                synchronized(MotorController.class) {
                    if(!typeClients.containsKey(antenna)) {
                        typeClients.put(antenna, new ArrayList<>());
                    }
                }
            }
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
        switch(data.getAsJsonObject().get("header").getAsString()) {
            case "updateAzimuthElevation": {
                Message m = new Message("updateAzEl", new double[]{data.getAsJsonObject().get("azimuth").getAsDouble(), data.getAsJsonObject().get("elevation").getAsDouble()});
                connectors.get((MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna")).sendData(m);
                wrappedSend(session, gson.toJson(new Message("success")));
            }
            break;
            case "stop": {
                Message m = new Message("stop");
                connectors.get((MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna")).sendData(m);
                wrappedSend(session, gson.toJson(new Message("success")));
            }
            case "calibrate_wrap": {
                if(Boolean.parseBoolean((String) session.getUserProperties().get("isAdmin"))) {
                    Message m = new Message("calibrate_wrap");
                    connectors.get((MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna")).sendData(m);
                    wrappedSend(session, gson.toJson(new Message("success")));
                }
                else {
                    wrappedSend(session, gson.toJson(new Message("failure", "inadequate_permissions")));
                }
            }
            break;
            default: {
                wrappedSend(session, gson.toJson(new Message("failure", "invalid_command")));
            }
        }
    }

    private void wrappedSend(Session session, String text) {
        try
        {
            session.getBasicRemote().sendText(text);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @OnClose
    public void close(Session session)
    {
        clients.remove(session);
        if(!(boolean)session.getUserProperties().get("motor_first"))
        {
            MotorConnector.MotorConnectorType antenna = (MotorConnector.MotorConnectorType) session.getUserProperties().get("antenna");
            typeClients.get(antenna).remove(session);
            connectors.get(antenna).removeMessageListener(this);
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
            wrappedSend(s, gson.toJson(body));
        }
    }
}
