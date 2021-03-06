package info;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import connector.DataListener;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection
{
    private List<DataListener> listeners = new ArrayList<DataListener>();
    private Socket s;
    private BufferedReader br;
    private PrintWriter pw;
    private JsonParser parser;

    public Connection(){}

    public Connection(Socket _s) throws IOException
    {
        parser = new JsonParser();
        initialize(_s);
    }

    public Connection(Socket _s, InputStream _is, OutputStream _os)
    {
        s = _s;
        br = new BufferedReader(new InputStreamReader(_is));
        pw = new PrintWriter(_os);
        parser = new JsonParser();
    }

    public synchronized void initialize(Socket _s) throws IOException
    {
        s = _s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        pw = new PrintWriter(s.getOutputStream());

        Thread t = new Thread(() -> {
            try
            {
                String datastr = br.readLine();
                System.out.println(datastr);
                JsonElement data = parser.parse(datastr);
                sendEvent(data);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });
        t.start();
    }

    public synchronized void send(JsonElement json)
    {
        pw.println(json.toString());
        pw.flush();
    }

    public synchronized void close()
    {
        try
        {
            br.close();
            pw.close();
            s.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            try
            {
                s.close();
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
            }
        }
    }

    public void addDataListener(DataListener d) {
        listeners.add(d);
    }

    public void removeDataListener(DataListener d) {
        listeners.remove(d);
    }

    private void sendEvent(JsonElement data) {
        for (DataListener d : listeners)
        {
            d.dataReceived(data);
        }
    }
}
