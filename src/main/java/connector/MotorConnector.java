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
    private static Map<MotorConnectorType, MotorConnector> instances = Collections.synchronizedMap(new TreeMap<>());

    private List<MessageListener> listeners = new ArrayList<>();
    private JavaConnection desktopConn;

    private MotorConnector(MotorConnectorType type) {
        initialize();
    }

    private void initialize() {
        //Initialize desktopConn
        while(desktopConn == null)
        {
            try
            {
                //desktopConn = new JavaConnection(new Socket("10.182.13.99", 6789)); //TODO: Uncomment for deployment
                desktopConn = new JavaConnection(new Socket("127.0.0.1",6789));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Thread t = new Thread(() -> {
            Message m = desktopConn.receive(Message.class);
            sendEvent(m);
        });
        t.start();
    }

    public static MotorConnector getInstance(MotorConnectorType type) {
        if(!instances.containsKey(type))
        {
            synchronized (MotorConnector.class)
            {
                if(!instances.containsKey(type))
                {
                    instances.put(type, new MotorConnector(type));
                }
            }
        }
        return instances.get(type);
    }

    public static Map<MotorConnectorType, MotorConnector> getInstances() {
        if(instances.size() != MotorConnectorType.values().length) {
            for(MotorConnectorType type : MotorConnectorType.values()) {
                if(!instances.containsKey(type))
                {
                    synchronized (MotorConnector.class)
                    {
                        if(!instances.containsKey(type))
                        {
                            instances.put(type, new MotorConnector(type));
                        }
                    }
                }
            }
        }
        return instances;
    }

    public Boolean sendData(Message m) {
        if(validateSend(m))
        {
            desktopConn.send(m);
            return true;
        }
        else
            return false;
    }

    private Boolean validateRecieve(Message m) {
        return true;
    }

    private Boolean validateSend(Message m) {
        return true;
    }

    public void addMessageListener(MessageListener m) {
        listeners.add(m);
    }

    public void removeMessageListener(MessageListener m) {
        listeners.remove(m);
    }

    private void sendEvent(Message m) {
        if(validateRecieve(m))
        {
            for (MessageListener ml : listeners)
            {
                ml.messageReceived(m);
            }
        }
        else {
            for (MessageListener ml : listeners)
            {
                ml.messageReceived(new Message("invalid"));
            }
        }
    }
}
