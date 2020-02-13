package socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import connector.DataListener;
import connector.UHFDataConnector;
import info.Message;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ServerEndpoint(value = "/UHFData", configurator = GetHttpSessionConfigurator.class)
public class UHFDataSocket implements DataListener
{
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<Session>());
    private UHFDataConnector connector = UHFDataConnector.getInstance();
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    public UHFDataSocket()
    {
        connector.addDataListener(this);
    }

    @OnOpen
    public void open(Session session, EndpointConfig endpoint)
    {
        clients.add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        JsonElement data = parser.parse(message);
        //TODO: check session to see if user has edit access
        if(data.getAsJsonObject().get("type").getAsString().equalsIgnoreCase("transmitState")) {
            connector.setTransmitState(data.getAsJsonObject().getAsString().equalsIgnoreCase("true"));
        }
        else {
            Boolean success = connector.sendData(data);
            try
            {
                session.getBasicRemote().sendText(gson.toJson(new Message(success ? "success" : "failure")));
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
    }

    @OnError
    public void error(Throwable error)
    {
        error.printStackTrace();
    }

    @Override
    public void dataReceived(JsonElement data)
    {
        //Send data to their sql
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
