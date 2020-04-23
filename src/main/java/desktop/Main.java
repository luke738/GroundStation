package desktop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.JavaConnection;
import info.Message;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Main
{
    private static AtomicBoolean run = new AtomicBoolean(true);

    public static void main(String[] args)
    {
        System.out.println("Starting up...");
        JsonObject _config = new JsonObject();
        try
        {
            _config = new JsonParser().parse(new BufferedReader(new FileReader("config/desktop_config.json"))).getAsJsonObject();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("Config not found, aborting launch...");
            System.exit(-1);
        }
        final JsonObject config = _config;

        try
        {
            ServerSocket ss = new ServerSocket(6789);
            List<Thread> threads = new ArrayList<>();
            while(run.get())
            {
                System.out.println("Waiting to connect...");
                try
                {
                    Socket s = ss.accept();
                    JavaConnection jc = new JavaConnection(s);
                    System.out.println("Connection established!");
                    final int tInd = threads.size();
                    Thread t = new Thread(() ->
                    {
                        boolean innerRun = true;
                        while(run.get() && innerRun)
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
                                        while((line = br.readLine()) != null)
                                        {
                                            System.out.println(line);
                                        }
                                        br.close();
                                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                        while((line = errorReader.readLine()) != null)
                                        {
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
                                    jc.send(new Message("success"));
                                    jc.close();
                                    innerRun = false;
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
                                        while((line = br.readLine()) != null)
                                        {
                                            System.out.println(line);
                                        }
                                        br.close();
                                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                        while((line = errorReader.readLine()) != null)
                                        {
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
                                    jc.send(new Message("success"));
                                    jc.close();
                                    innerRun = false;
                                }
                                break;
                                case "shutdown":
                                {
                                    run.set(false);
                                    System.out.println("Shutting down...");
                                    jc.send(new Message("shutdown_ack"));
                                    jc.close();
                                }
                                break;
                                case "uhf_transmit_toggle":
                                {
                                    jc.send(new Message("dummy_toggle"));
                                }
                                break;
                                case "tle_download":
                                {
                                    System.out.println("Downloading new TLEs...");
                                    try {
                                        String baseURL = "https://www.space-track.org";
                                        String authPath = "/ajaxauth/login";
                                        String userName = config.get("space-trak_username").getAsString();
                                        String password = config.get("space-trak_password").getAsString();
                                        String query = "/basicspacedata/query/class/tle_latest/ORDINAL/1/format/3le";


                                        CookieManager manager = new CookieManager();
                                        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                                        CookieHandler.setDefault(manager);

                                        URL url = new URL(baseURL+authPath);

                                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                                        conn.setDoOutput(true);
                                        conn.setRequestMethod("POST");

                                        String input = "identity="+userName+"&password="+password;

                                        OutputStream os = conn.getOutputStream();
                                        os.write(input.getBytes());
                                        os.flush();

                                        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                                        //conn.disconnect();

                                        url = new URL(baseURL + query);

                                        br = new BufferedReader(new InputStreamReader((url.openStream())));
                                        String TLE_dataString = br.lines().collect(Collectors.joining(System.lineSeparator()));

                                        // System.out.println("Output from Server .... \n");
                                        File TLE_data = new File(config.get("tle_dir").getAsString() + "\\TLE_output.txt");
                                        TLE_data.createNewFile();

                                        BufferedWriter fw;
                                        //print output & store in file
                                        try {
                                            fw = new BufferedWriter(new FileWriter(TLE_data, false));
                                            fw.write(TLE_dataString);
                                            fw.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            jc.send(new Message("write_failure"));
                                            System.out.println("File write failed! Check tle_dir.");
                                        }

                                        //logout
                                        url = new URL(baseURL + "/ajaxauth/logout");
                                        br = new BufferedReader(new InputStreamReader((url.openStream())));
                                        conn.disconnect();

                                        jc.send(new Message("download_success"));
                                        System.out.println("Download successful.");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.out.println("Download failed! Check username/password and connection.");
                                        jc.send(new Message("download_failure"));
                                    }
                                }
                                default:
                                {
                                    jc.send(new Message("invalid_request"));
                                }
                            }
                        }
                        System.out.println("Connection closed.");
                        threads.remove(tInd);
                    });
                    threads.add(t);
                    t.start();
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                    System.out.println("Communications error. Connection lost.");
                }
            }
            for(Thread thread : threads)
            {
                try
                {
                    thread.join();
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                    System.out.println("Error closing thread.");
                }
            }
            ss.close();
            System.out.println("All connections closed.");
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error opening port.");
        }
    }
}
