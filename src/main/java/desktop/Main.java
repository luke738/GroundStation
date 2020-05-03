package desktop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.JavaConnection;
import info.Message;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
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
                        AtomicBoolean innerRun = new AtomicBoolean(true);
                        while(run.get() && innerRun.get())
                        {
                            Message m = jc.receive(Message.class);
                            if(m == null) {
                                System.out.println("Connection closed remotely.");
                                innerRun.set(false);
                                continue;
                            }
                            switch(m.header)
                            {
                                case "motor_control": {
                                    System.out.println("Entering motor control mode on thread " + threads.get(tInd).getId());
                                    motorControl(config, jc, innerRun);
                                }
                                break;
                                case "start_program":
                                {
                                    programControl(config, jc, innerRun, m, true);
                                }
                                break;
                                case "stop_program":
                                {
                                    programControl(config, jc, innerRun, m, false);
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
                                    //TODO: Uncomment this block when UHF toggle interface is working. Remember to update IP and port in config file.
//                                    try
//                                    {
//                                        Socket soc = new Socket(config.get("uhf_transmit_toggle_host").getAsString(), config.get("uhf_transmit_toggle_port").getAsInt());
//                                        BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
//                                        PrintWriter pw = new PrintWriter(soc.getOutputStream());
//
//                                        String toggle_command = "toggle command"; //TODO: Replace with actual toggle command
//                                        pw.println(toggle_command);
//                                        pw.flush();
//
//                                        String response = br.readLine();
//                                        if(response.equals("response that means it works")) { //TODO: Replace with actual response
//                                            jc.send(new Message("success"));
//                                        }
//                                        else {
//                                            jc.send(new Message("failure", response));
//                                        }
//                                    }
//                                    catch(IOException e)
//                                    {
//                                        e.printStackTrace();
//                                        jc.send(new Message("failure", "exception"));
//                                    }
                                    jc.send(new Message("dummy_toggle")); //TODO: Remove when UHF toggle interface is working.
                                }
                                break;
                                case "disconnect": //break a connection explicitly without shutting down
                                {
                                    jc.close();
                                    innerRun.set(false);
                                }
                                break;
                                case "tle_download":
                                {
                                    downloadTLEs(config, jc, innerRun);
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

    private static void downloadTLEs(JsonObject config, JavaConnection jc, AtomicBoolean innerRun)
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
        if(false) //TODO: REMOVE FOR DEPLOYMENT
        {
            Process process = null;
            try
            {
                ProcessBuilder pb = new ProcessBuilder(config.get("tle_filter").getAsString());
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
                System.out.println("Error launching filter.");
                jc.send(new Message("launch_failed"));
            }
            try
            {
                process.waitFor();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Unable to wait for filter to run.");
                jc.send(new Message("launch_failed"));
            }
            process.destroy();
        }
        jc.close();
        innerRun.set(false);
    }

    private static void programControl(JsonObject config, JavaConnection jc, AtomicBoolean innerRun, Message m, boolean isLaunch)
    {
        String supportText;
        String actionText;
        String scriptSuffix;
        String errorText1;
        String failureMessage;
        String errorText2;
        if(isLaunch) {
            supportText = " launch not yet supported.";
            actionText = "Launching ";
            scriptSuffix = "_start.ahk\"";
            errorText1 = "Error launching ";
            failureMessage = "launch_failed";
            errorText2 = " to launch.";
        }
        else {
            supportText = " kill not yet supported.";
            actionText = "Killing ";
            scriptSuffix = "_stop.ahk\"";
            errorText1 = "Error killing ";
            failureMessage = "stop_failed";
            errorText2 = " to die.";
        }
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
                System.out.println(properName + supportText);
                jc.send(new Message("invalid_program_name"));
                return;
            }
            default:
            {
                jc.send(new Message("invalid_program_name"));
                return;
            }
        }
        System.out.println(actionText + properName + "...");
        Process process = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(config.get("autohotkey").getAsString(), "\"" + config.get("script_dir") + "\\" + name + scriptSuffix);
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
            System.out.println(errorText1 + properName + "...");
            jc.send(new Message(failureMessage));
        }
        try
        {
            process.waitFor();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Unable to wait for " + properName + errorText2);
            jc.send(new Message(failureMessage));
        }
        process.destroy();
        jc.send(new Message("success"));
        jc.close();
        innerRun.set(false);
    }

    private static void motorControl(JsonObject config, JavaConnection jc, AtomicBoolean innerRun)
    {
        PstRotatorConnector pstConn = PstRotatorConnector.instance;
        pstConn.initialize(config.get("udp_port").getAsInt());
        //Kick out a polling thread here
        Map<String, Double> wrapStore = new ConcurrentHashMap<>();
        wrapStore.put("prevAz", 0.);
        wrapStore.put("totalDist", 0.);
        final double wrapLevel = config.get("wrap_warning_level").getAsDouble();
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            //Send UDP request to PstRotator for current antenna position
            String queryAz = "<PST>AZ?</PST>";
            String queryEl = "<PST>EL?</PST>";
            pstConn.send(queryAz);
            pstConn.writeLock.lock();
            try
            {
                boolean responded = pstConn.newDataCondition.await(1, TimeUnit.SECONDS);
                if(!responded) System.out.println("Warning: PstRotator did not respond.");
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            pstConn.writeLock.unlock();
            String azResp = pstConn.readData();
            System.out.println(azResp);
            pstConn.send(queryEl);
            pstConn.writeLock.lock();
            try
            {
                boolean responded = pstConn.newDataCondition.await(1, TimeUnit.SECONDS);
                if(!responded) System.out.println("Warning: PstRotator did not respond.");
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            pstConn.writeLock.unlock();
            String elResp = pstConn.readData();
            System.out.println(elResp);

            //Send current position back to socket
            Pattern r = Pattern.compile("\\d+\\.\\d+");
            double az = Double.parseDouble(r.matcher(azResp).group());
            double el = Double.parseDouble(r.matcher(elResp).group());
            jc.send(new Message("currentAzimuth", az));
            jc.send(new Message("currentElevation", el));

            //Check antenna wrap
            //Assumptions: Antenna starts at Az=0 unwrapped, never travels faster than 180deg/s, and is wrapped when it traverses a net wrapLevel in any direction.
            double prevAz = wrapStore.get("prevAz");
            double dist = az - prevAz;
            if(dist > 180) dist -= 360;
            else if(dist < -180) dist += 360;
            double totalDist = wrapStore.get("totalDist");
            totalDist += dist;
            if(totalDist > wrapLevel || -totalDist > wrapLevel) {
                jc.send(new Message("antenna_wrapped", "true"));
            }
            else {
                jc.send(new Message("antenna_wrapped", "false"));
            }
            wrapStore.put("prevAz", az);
            wrapStore.put("totalDist", totalDist);

        }, 0, 1, TimeUnit.SECONDS);
        //Enter tighter motor control loop with new syntax
        while(run.get() && innerRun.get()) {
            Message mc = jc.receive(Message.class);
            if(mc == null) {
                System.out.println("Connection closed remotely.");
                innerRun.set(false);
                continue;
            }
            switch(mc.header) {
                case "updateAzEl": {
                    System.out.println("Updating azimuth and elevation...");
                    double[] vals = (double[]) mc.body;
                    String command = "<PST><AZIMUTH>" + vals[0] + "</AZIMUTH><ELEVATION>" + vals[1] + "</ELEVATION></PST>";
                    pstConn.send(command);
                }
                break;
                case "calibrate_wrap": {
                    wrapStore.put("prevAz", 0.);
                    wrapStore.put("totalDistance", 0.);
                    System.out.println("Wrap calibration reset.");
                }
                break;
                case "stop": {
                    String command = "<PST><STOP>1</STOP></PST>";
                    pstConn.send(command);
                    System.out.println("Antenna stop command sent.");
                }
                break;
                default: {
                    jc.send(new Message("invalid_command"));
                }
            }
        }
    }
}
