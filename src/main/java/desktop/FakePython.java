package desktop;

import com.google.gson.JsonElement;
import connector.DataListener;
import info.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FakePython implements DataListener
{
    public Connection c;

    public static void main(String[] args) {
        try
        {
            FakePython fp = new FakePython();
            ServerSocket ss = new ServerSocket(2186);
            while (true)
            {
                Socket s = ss.accept();
                fp.c = new Connection(s);
                fp.c.addDataListener(fp);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void dataReceived(JsonElement data)
    {
        c.send(data);
    }
}
