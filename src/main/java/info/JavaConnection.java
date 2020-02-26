package info;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JavaConnection
{
    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public JavaConnection(){}

    public JavaConnection(Socket s) throws IOException
    {
        initialize(s);
    }

    public JavaConnection(Socket s, ObjectInputStream ois, ObjectOutputStream oos)
    {
        this.s = s;
        this.ois = ois;
        this.oos = oos;
    }

    public void initialize(Socket s) throws IOException
    {
        this.s = s;
        oos = new ObjectOutputStream(this.s.getOutputStream());
        InputStream is = this.s.getInputStream();
        ois = new ObjectInputStream(is);
    }

    public void send(Object obj)
    {
        try
        {
            oos.writeObject(obj);
            oos.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            close();
        }
    }

    public Object receiveObject()
    {
        try
        {
            return ois.readObject();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T receive(Class<T> type)
    {
        try
        {
            Object obj = ois.readObject();
            if(obj.getClass() != type)
            {
                close();
                System.out.println("Incorrect type!");
                System.out.println("Received " + obj.getClass().getTypeName() + " Expected " + type.getTypeName());
                return null;
            }
            return type.cast(obj);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            close();
            return null;
        }
    }

    public void close()
    {
        try
        {
            ois.close();
            oos.close();
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
}
