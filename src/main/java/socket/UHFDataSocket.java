package socket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ServerEndpoint(value = "/UHFData", configurator = GetHttpSessionConfigurator.class)
public class UHFDataSocket implements ActionListener {
    private static List<Session> clients = Collections.synchronizedList(new ArrayList<Session>());

    @OnOpen
    public void open(Session session, EndpointConfig endpoint)
    {

    }

    @OnMessage
    public void onMessage(String message, Session session)
    {

    }

    @OnClose
    public void close(Session session)
    {

    }

    @OnError
    public void error(Throwable error)
    {
        error.printStackTrace();
    }

    @Override
    public void actionPerformed(ActionEvent e) //Got an event from the gs desktop
    {
        for(Session s : clients) {
            try
            {
                s.getBasicRemote().sendText(e.getActionCommand());
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
