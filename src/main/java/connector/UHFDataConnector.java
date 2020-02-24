package connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.Connection;
import info.Message;

import java.util.*;

public class UHFDataConnector implements DataListener{
    private List<DataListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private Connection desktopConn;


    private static final UHFDataConnector single = new UHFDataConnector();
    private UHFDataConnector() {
        //initialize(); TODO: Uncomment when initialize works
    }

    private void initialize() {
        //TODO: Connect to desktop program
        //Initialize desktopConn
        desktopConn.addDataListener(this);
    }

    public static UHFDataConnector getInstance(){
        return single;
    }

    public void setTransmitState(Boolean state) {
        JsonElement message = new JsonObject();
        message.getAsJsonObject().add("header", new JsonPrimitive("set_transmit"));
        message.getAsJsonObject().add("body", new JsonPrimitive(state));
        desktopConn.send(message);
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
