package connector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import info.Connection;
import info.Message;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class UHFDataConnector implements DataListener{
    private List<DataListener> listeners = new ArrayList<DataListener>();
    private Connection desktopConn;


    private static UHFDataConnector single = null;
    private UHFDataConnector() {
        //initialize(); TODO: Uncomment when initialize works
    }

    private void initialize() {
        //TODO: Connect to desktop program
        //Initialize desktopConn
        desktopConn.addDataListener(this);
    }

    public static UHFDataConnector getInstance(){
        if(single==null) single = new UHFDataConnector();

        return single;
    }

    public void setTransmitState(Boolean state) {
        //TODO: Construct correct json message
        JsonElement message = new JsonObject();
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
        return true;
    }

    private Boolean validateSend(JsonElement data) {
        return true;
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
