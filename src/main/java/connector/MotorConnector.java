package connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import info.Connection;
import info.Message;

import java.util.*;

public class MotorConnector implements DataListener{
    public enum MotorConnectorType{
        UHF, SBAND
    }
    private static Map<MotorConnectorType, MotorConnector> instances = Collections.synchronizedMap(new TreeMap<>());

    private List<DataListener> listeners = new ArrayList<DataListener>();
    private Connection desktopConn;

    private MotorConnector(MotorConnectorType type) {
        //initialize() TODO: Uncomment
    }

    private void initialize() {
        //TODO: Connect to desktop program
        //Initialize desktopConn
        desktopConn.addDataListener(this);
    }

    public MotorConnector getInstance(MotorConnectorType type) {
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

    public Boolean sendData(JsonElement data) {
        if(validateSend(data))
        {
            desktopConn.send(data);
            return true;
        }
        else
            return false;
    }

    private Boolean validateRecieve(JsonElement data) {
        return true;
    }

    private Boolean validateSend(JsonElement data) {
        return true;
    }

    public void addDataListener(DataListener d) {
        listeners.add(d);
    }

    public void removeDataListener(DataListener d) {
        listeners.remove(d);
    }

    @Override
    public void dataReceived(JsonElement data) {
        sendEvent(data);
    }

    private void sendEvent(JsonElement data) {
        if(validateRecieve(data))
        {
            for (DataListener d : listeners)
            {
                d.dataReceived(data);
            }
        }
        else {
            for (DataListener d : listeners)
            {
                d.dataReceived(new Gson().toJsonTree(new Message("invalid")));
            }
        }
    }
}
