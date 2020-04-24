package socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import connector.MessageListener;
import connector.MotorConnector;
import info.Message;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ServerEndpoint(value = "/MotorControl", configurator = GetHttpSessionConfigurator.class)
public class MotorController implements MessageListener
{
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<>());
    private Map<MotorConnector.MotorConnectorType, MotorConnector> connectors = MotorConnector.getInstances();
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    public MotorController()
    {
        for(MotorConnector connector : connectors.values()) {
            connector.addMessageListener(this);
        }
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
        //TODO: check if this is first message, add to correct computer list
        Boolean success = false; //connector.sendData(data);
        //TODO: check who sent this message, then relay to correct computer
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
        //TODO: modify this to not send the message to all clients, but only to clients connected to the antenna that sent the message
        for(Session s : clients) {
            try
            {
                s.getBasicRemote().sendText(gson.toJson(m));
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
