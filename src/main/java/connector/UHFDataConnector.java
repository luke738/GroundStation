package connector;

import javax.websocket.Session;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UHFDataConnector {
    private List<DataListener> listeners = new ArrayList<DataListener>();

    public void addDataListener(DataListener d) {
        listeners.add(d);
    }

    private void sendEvent(String data) {
        for(DataListener d : listeners) {
            d.dataReceived(data);
        }
    }
}
