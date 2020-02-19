package socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import connector.DataListener;
import connector.UHFDataConnector;
import info.Message;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ServerEndpoint(value = "/MotorControl", configurator = GetHttpSessionConfigurator.class)
public class MotorController implements DataListener
{
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<Session>());
    private UHFDataConnector connector = UHFDataConnector.getInstance();
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    public MotorController()
    {
        connector.addDataListener(this);
    }

    @OnOpen
    public void open(Session session, EndpointConfig endpoint){
        if(!endpoint.getUserProperties().get("httpSession").equals(0))
        {
            HttpSession httpSession = (HttpSession) endpoint.getUserProperties().get("httpSession");
            if(httpSession.getAttribute("loggedIn")!=null && (Boolean) httpSession.getAttribute("loggedIn"))
            {
                clients.add(session);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session){
        JsonElement data = parser.parse(message);
        //TODO: check HttpSession to see if user has edit access
        Boolean success = connector.sendData(data);
        try
        {
            session.getBasicRemote().sendText(gson.toJson(new Message(success ? "success" : "failure")));
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
        //Send data to their sql?
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
