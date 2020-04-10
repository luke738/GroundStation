package desktop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.JavaConnection;
import info.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Starting up...");
        JsonObject config = new JsonObject();
        try
        {
            config = new JsonParser().parse(new BufferedReader(new FileReader("config/desktop_config.json"))).getAsJsonObject();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("Config not found, aborting launch...");
            System.exit(-1);
        }

        try
        {
            ServerSocket ss = new ServerSocket(6789);
            Boolean run = true;
            while(run)
            {
                System.out.println("Waiting to connect...");
                try
                {
                    Socket s = ss.accept();
                    JavaConnection jc = new JavaConnection(s);
                    System.out.println("Connection established!");
                    while(run)
                    {
                        Message m = jc.receive(Message.class);
                        switch(m.header)
                        {
                            case "start_program":
                            {
                                String name = (String) m.body;
                                String properName = "";
                                switch(name)
                                {
                                    case "orbitron":
                                    {
                                        properName = "Orbitron";
                                    }
                                    break;
                                    case "spidmd01":
                                    {
                                        properName = "SPID MD-01 Driver";
                                    }
                                    break;
                                    case "sdrsharp":
                                    {
                                        properName = "SDRSharp";
                                    }
                                    break;
                                    case "pstrotator":
                                    {
                                        properName = "PSTRotator";
                                    }
                                    break;
                                    case "python":
                                    {
                                        properName = "GNU Radio Python Script";
                                        System.out.println(properName + " launch not yet supported.");
                                        jc.send(new Message("invalid_program_name"));
                                        continue;
                                    }
                                    default:
                                    {
                                        jc.send(new Message("invalid_program_name"));
                                        continue;
                                    }
                                }
                                System.out.println("Launching " + properName + "...");
                                Process process = null;
                                try
                                {
                                    ProcessBuilder pb = new ProcessBuilder(config.get("autohotkey").getAsString(), "\"" + config.get("script_dir") + "\\" + name + "_start.ahk\"");
                                    process = pb.start();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    br.close();
                                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                    while ((line = errorReader.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    errorReader.close();
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                    System.out.println("Error launching " + properName + "...");
                                    jc.send(new Message("launch_failed"));
                                }
                                try
                                {
                                    process.waitFor();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                    System.out.println("Unable to wait for " + properName + " to launch.");
                                    jc.send(new Message("launch_failed"));
                                }
                                process.destroy();
                            }
                            break;
                            case "stop_program":
                            {
                                String name = (String) m.body;
                                String properName = "";
                                switch(name)
                                {
                                    case "orbitron":
                                    {
                                        properName = "Orbitron";
                                    }
                                    break;
                                    case "spidmd01":
                                    {
                                        properName = "SPID MD-01 Driver";
                                    }
                                    break;
                                    case "sdrsharp":
                                    {
                                        properName = "SDRSharp";
                                    }
                                    break;
                                    case "pstrotator":
                                    {
                                        properName = "PSTRotator";
                                    }
                                    break;
                                    case "python":
                                    {
                                        properName = "GNU Radio Python Script";
                                        System.out.println(properName + " kill not yet supported.");
                                        jc.send(new Message("invalid_program_name"));
                                        continue;
                                    }
                                    default:
                                    {
                                        jc.send(new Message("invalid_program_name"));
                                        continue;
                                    }
                                }
                                System.out.println("Killing " + properName + "...");
                                Process process = null;
                                try
                                {
                                    ProcessBuilder pb = new ProcessBuilder(config.get("autohotkey").getAsString(), "\"" + config.get("script_dir") + "\\" + name + "_stop.ahk\"");
                                    process = pb.start();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    br.close();
                                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                    while ((line = errorReader.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    errorReader.close();
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                    System.out.println("Error killing " + properName + "...");
                                    jc.send(new Message("stop_failed"));
                                }
                                try
                                {
                                    process.waitFor();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                    System.out.println("Unable to wait for " + properName + " to die.");
                                    jc.send(new Message("stop_failed"));
                                }
                                process.destroy();
                            }
                            break;
                            case "shutdown":
                            {
                                run = false;
                                System.out.println("Shutting down...");
                            }
                            break;
                            case "uhf_transmit_toggle":
                            {
                                jc.send(new Message("dummy_toggle"));
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
                    System.out.println("Connection closed.");
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                    System.out.println("Communications error. Connection lost.");
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error opening port.");
        }
    }
}
