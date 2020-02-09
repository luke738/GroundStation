package socket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/MotorControl", configurator = GetHttpSessionConfigurator.class)
public class MotorController {
    @OnOpen
    public void open(Session session, EndpointConfig endpoint){

    }

    @OnMessage
    public void onMessage(String message, Session session){

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
}
