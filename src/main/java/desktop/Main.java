package desktop;

import info.JavaConnection;
import info.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            ServerSocket ss = new ServerSocket(6789);
            Socket s = ss.accept();
            JavaConnection jc = new JavaConnection(s);
            Boolean run = true;
            while(run) {
                Message m = jc.receive(Message.class);
                switch (m.header) {
                    case "start_program":
                    {
                        String name = (String) m.body;
                        switch (name)
                        {
                            case "orbitron": {

                            }
                            break;
                            case "spidmd01": {

                            }
                            break;
                            case "sdrsharp": {

                            }
                            break;
                            case "pstrotator": {

                            }
                            break;
                            case "python": {

                            }
                            default: {
                                jc.send(new Message("invalid_program_name"));
                            }
                        }
                    }
                    break;
                    case "stop_program":
                    {
                        String name = (String) m.body;
                        switch (name)
                        {

                        }
                    }
                    break;
                    case "shutdown":
                    {
                        run = false;
                    }
                    break;
                    case "uhf_transmit_toggle":
                    {
                    }
                    break;
                    default:
                    {
                        jc.send(new Message("invalid_request"));
                    }
                }
            }
            jc.send(new Message("shutdown_ack"));
            jc.close();
            ss.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
