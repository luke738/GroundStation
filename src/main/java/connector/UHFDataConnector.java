package connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.Connection;
import info.JavaConnection;
import info.Message;

import javax.servlet.ServletContext;
import java.net.Socket;
import java.util.*;

public class UHFDataConnector implements DataListener{
    private List<DataListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private Connection pythonConn;
    private JavaConnection desktopConn;

    public UHFDataConnector(String desktop_host, int desktop_port, int python_port) {
        initialize(desktop_host, desktop_port, python_port);
    }

    private void initialize(String host, int port, int python_port) {
        //Initialize desktopConn
        while(desktopConn == null)
        {
            try
            {
                desktopConn = new JavaConnection(new Socket(host, port));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        //Initialize pythonConn
        while(pythonConn == null)
        {
            try
            {
                pythonConn = new Connection(new Socket(host, python_port));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        pythonConn.addDataListener(this);
    }

    public void setTransmitState(Boolean state) {
//        JsonElement message = new JsonObject();
//        message.getAsJsonObject().add("header", new JsonPrimitive("set_transmit"));
//        message.getAsJsonObject().add("body", new JsonPrimitive(state));
//        pythonConn.send(message);
        synchronized(UHFDataConnector.class)
        {
            desktopConn.send(new Message("set_transmit", state));
        }
        Message resp = desktopConn.receive(Message.class); //TODO: Remove these debug lines
        System.out.println(resp.header); //TODO: Above
    }

    public Boolean sendData(JsonElement data) {
        if(validateSend(data))
        {
            synchronized(UHFDataConnector.class)
            {
                pythonConn.send(data);
            }
            return true;
        }
        else
            return false;
    }

    private Boolean validateRecieve(JsonElement data) {
        Set<String> types = new HashSet<>(Arrays.asList("status", "data_rx", "info", "data_tx_ack", "shutdown_delay"));
        return data.getAsJsonObject().has("header") && types.contains(data.getAsJsonObject().get("header").getAsString());
    }

    private Boolean validateSend(JsonElement data) {
        Set<String> types = new HashSet<>(Arrays.asList("data_tx", "shutdown"));
        return data.getAsJsonObject().has("header") && types.contains(data.getAsJsonObject().get("header").getAsString());
    }

    public void dataReceived(JsonElement data) {
        sendEvent(data);
    }

    public void addDataListener(DataListener d) {
        listeners.add(d);
    }

    public void removeDataListener(DataListener d) {
        listeners.remove(d);
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
