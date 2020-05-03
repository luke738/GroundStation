package desktop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PstRotatorConnector
{
    public static PstRotatorConnector instance = new PstRotatorConnector();

    private String data = "";
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    public final Lock writeLock = rwLock.writeLock();
    public final Condition newDataCondition = writeLock.newCondition();

    private DatagramSocket sender;
    private DatagramSocket receiver;
    private int port;
    private boolean initialized = false;
    private AtomicBoolean receiving = new AtomicBoolean(true);

    private PstRotatorConnector() { }

    public synchronized boolean initialize(int port) {
        if(initialized) return true;
        this.port = port;
        try
        {
            sender = new DatagramSocket();
            receiver = new DatagramSocket(port+1);
            initialized = true;

            startReceiving();
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }
        return initialized;
    }

    public void startReceiving() {
        receiving.set(true);
        Thread t = new Thread(() -> {
            while(receiving.get()) {
                receive();
            }
        });
        t.start();
    }

    public void stopReceiving() {
        receiving.set(false);
    }

    public synchronized boolean send(String data) {
        try
        {
            sender.send(new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getLoopbackAddress(), port));
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void receive() {
        byte[] receive = new byte[1024];
        DatagramPacket dataPacket = new DatagramPacket(receive, receive.length);
        try
        {
            receiver.receive(dataPacket);
            String dataCopy = new String(receive).trim().replace("\0", "");
            rwLock.writeLock().lock();
            try {
                data = dataCopy;
                newDataCondition.signalAll();
            }
            finally
            {
                rwLock.writeLock().unlock();
            }

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String readData() {
        String dataCopy;
        rwLock.readLock().lock();
        try
        {
            dataCopy = data;
        }
        finally
        {
            rwLock.readLock().unlock();
        }
        return dataCopy;
    }
}
