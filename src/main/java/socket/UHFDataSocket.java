package socket;

import com.google.gson.*;
import connector.DataListener;
import connector.UHFDataConnector;
import info.Message;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ServerEndpoint(value = "/UHFData", configurator = GetHttpSessionConfigurator.class)
public class UHFDataSocket implements DataListener
{
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<Session>());
    private UHFDataConnector connector;
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    private static Pattern pattern = Pattern.compile("^(?:0x)?[0-9a-fA-F]+$");

    @OnOpen
    public void open(Session session, EndpointConfig endpoint)
    {
        if(!endpoint.getUserProperties().get("httpSession").equals(0))
        {
            HttpSession httpSession = (HttpSession) endpoint.getUserProperties().get("httpSession");
            session.getUserProperties().put("httpSession", httpSession);
            if(httpSession.getAttribute("loggedIn")!=null && (Boolean) httpSession.getAttribute("loggedIn"))
            {
                clients.add(session);
                ServletContext context = httpSession.getServletContext();
                String desktop_host = context.getInitParameter("uhf_host");
                int desktop_port = Integer.parseInt(context.getInitParameter("desktop_port"));
                int python_port = Integer.parseInt(context.getInitParameter("python_port"));
                connector = new UHFDataConnector(desktop_host, desktop_port, python_port);
                connector.addDataListener(this);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        System.out.println(message);
        JsonElement data = parser.parse(message);
        String body = "send_failure";
        boolean valid = false;
        switch (data.getAsJsonObject().get("header").getAsString()) {
            case "set_transmit": {
                if(!data.getAsJsonObject().has("body")) {
                    valid = false;
                    body = "invalid_transmit";
                    break;
                }
                connector.setTransmitState(data.getAsJsonObject().get("body").getAsString().equalsIgnoreCase("true"));
                try
                {
                    session.getBasicRemote().sendText(gson.toJson(new Message("success")));
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return;
            }
            case "data_tx": {
                if(!data.getAsJsonObject().has("body")) {
                    valid = false;
                    body = "no_data";
                }
                else {
                    String dataBody = data.getAsJsonObject().get("body").getAsString();
                    Matcher matcher = pattern.matcher(dataBody);
                    if(matcher.matches()) {
                        data.getAsJsonObject().add("userID", new JsonPrimitive(session.getId()));
                        valid = true;
                        body = "";
                    }
                    else {
                        valid = false;
                        body = "invalid_data";
                    }
                }
            }
            break;
            case "shutdown": {
                if(data.getAsJsonObject().has("body")) {
                    valid = false;
                    body = "shutdown_body";
                }
                else {
                    data.getAsJsonObject().add("userID", new JsonPrimitive(session.getId()));
                    valid = true;
                    body = "";
                }
            }
            break;
            default: {
                valid = false;
                body = "unknown_type";
            }
        }
        boolean success =  valid ? connector.sendData(data) : false;
        try
        {
            session.getBasicRemote().sendText(gson.toJson(new Message(success ? "success" : "failure", body)));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @OnClose
    public void close(Session session)
    {
        clients.remove(session);
        connector.removeDataListener(this);
    }

    @OnError
    public void error(Throwable error)
    {
        error.printStackTrace();
    }

    @Override
    public void dataReceived(JsonElement data)
    {
        if(data.getAsJsonObject().get("header").getAsString().equalsIgnoreCase("data_rx"))
        {
            //TODO Send data to uhf storage database, if so desired
        }
        else if(data.getAsJsonObject().get("header").getAsString().equalsIgnoreCase("shutdown_delay")) {
            TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    JsonObject shutdown = new JsonObject();
                    shutdown.add("header", new JsonPrimitive("shutdown"));
                    connector.sendData(shutdown);
                }
            };
            Timer t = new Timer();
            long wait = 60;
            if(data.getAsJsonObject().has("body"))
                wait = data.getAsJsonObject().get("body").getAsLong();
            t.schedule(task, wait);
            Session user = null;
            for(Session s : clients) {
                if(s.getId().equals(data.getAsJsonObject().get("userID").getAsString())) {
                    user = s;
                    break;
                }
            }
            try
            {
                if(user != null) user.getBasicRemote().sendText(data.toString());
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            return;
        }
        else if(data.getAsJsonObject().get("header").getAsString().equalsIgnoreCase("data_tx_ack")) {
            Session user = null;
            for(Session s : clients) {
                if(s.getId().equals(data.getAsJsonObject().get("userID").getAsString())) {
                    user = s;
                    break;
                }
            }
            try
            {
                if(user != null) user.getBasicRemote().sendText(data.toString());
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            return;
        }
        for(Session s : clients) {
            try
            {
                s.getBasicRemote().sendText(data.toString());
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
