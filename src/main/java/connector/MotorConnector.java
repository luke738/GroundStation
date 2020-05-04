package connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import info.Connection;
import info.JavaConnection;
import info.Message;

import java.net.Socket;
import java.util.*;

public class MotorConnector{
    public enum MotorConnectorType{
        UHF, SBAND
    }

    private MotorConnectorType type;
    private List<MessageListener> listeners = new ArrayList<>();
    private JavaConnection desktopConn;
    private boolean closed = false;

    public MotorConnector(MotorConnectorType type, String host, int port) {
        this.type = type;
        initialize(host, port);
    }

    private void initialize(String host, int port) {
        //Initialize desktopConn
        while(desktopConn == null)
        {
            try
            {
                desktopConn = new JavaConnection(new Socket(host, port));
                desktopConn.send(new Message("motor_control"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Thread t = new Thread(() -> {
            Message m = desktopConn.receive(Message.class);
            Message m2 = new Message(type.toString(), m);
            sendEvent(m2);
        });
        t.start();
    }

    public synchronized void sendData(Message m) {
        desktopConn.send(m);
    }

    public void addMessageListener(MessageListener m) {
        listeners.add(m);
    }

    public void removeMessageListener(MessageListener m) {
        listeners.remove(m);
    }

    private void sendEvent(Message m) {
        for (MessageListener ml : listeners)
        {
            ml.messageReceived(m);
        }
    }
}
