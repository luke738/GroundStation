package connector;

import com.google.gson.JsonElement;
import info.Connection;

import java.util.ArrayList;
import java.util.List;

public class UHFDataConnector implements DataListener{
    private List<DataListener> listeners = new ArrayList<DataListener>();
    Connection desktopConn;

    private static UHFDataConnector single = null;

    private UHFDataConnector() {
        //initialize();
    }

    private void initialize() {
        //Connect to desktop program
        //Initialize desktopConn
        desktopConn.addDataListener(this);
    }

    public UHFDataConnector getInstance(){
        if(single==null) single = new UHFDataConnector();

        return single;
    }

    private Boolean validate(JsonElement data) {
        return true;
    }

    public void dataReceived(JsonElement data) {
        sendEvent(data);
    }

    public void addDataListener(DataListener d) {
        listeners.add(d);
    }

    private void sendEvent(JsonElement data) {
        if(validate(data))
        {
            for (DataListener d : listeners)
            {
                d.dataReceived(data);
            }
        }
        else {

        }
    }
}
