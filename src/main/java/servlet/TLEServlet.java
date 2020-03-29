package servlet;

import com.google.gson.Gson;
import info.Message;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.nio.file.Paths;

/**
  * A servlet for downloading tle data from space-track
  * @author space-track.org
  *
  */
@WebServlet(name = "TLEServlet", urlPatterns = "/TLEData")
public class TLEServlet extends HttpServlet {
    //zoneID = los angeles
    public static ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
    //zone date time
    public static ZonedDateTime TLE_download_time = ZonedDateTime.now(zoneId);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //when click the button get the info, write to a file, "success"
        //https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/format/3le
        ZonedDateTime zdt = ZonedDateTime.now( zoneId );
        PrintWriter respWriter = response.getWriter();
        Gson gson = new Gson();;
        //if within 60 minutes of last TLE download, the server will not allow TLE download
        if(!(zdt.minusMinutes(60).isAfter(TLE_download_time))){
            respWriter.println(gson.toJson(new Message("Cannot pull TLE data within 1 hr of last request!")));
            respWriter.println(gson.toJson("Last request was at " + TLE_download_time.getHour()+ ":"+ TLE_download_time.getMinute()));
            //break
        }
        else {
            //update TLE download time
            TLE_download_time = ZonedDateTime.now(zoneId);
            try {

                String baseURL = "https://www.space-track.org";
                String authPath = "/ajaxauth/login";
                String userName = "USERNAME";
                String password = "PASSWORD";
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

                String output;

                System.out.println("Output from Server .... \n");
                File TLE_data = new File("TLE_output.txt");

                byte[] encoded = Files.readAllBytes(Paths.get(TLE_data.getPath()));
                String TLE_dataString = new String(encoded, "UTF-8");
                respWriter.println(gson.toJson(new Message("TLE_data",TLE_dataString)));

                BufferedWriter fw;
                //print output & store in file
                try {
                    fw = new BufferedWriter(new FileWriter(TLE_data));
                    while ((output = br.readLine()) != null) {
                        fw.write(output);
                        System.out.println(output);
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                }

                url = new URL(baseURL + query);

                br = new BufferedReader(new InputStreamReader((url.openStream())));


                //logout
                url = new URL(baseURL + "/ajaxauth/logout");
                br = new BufferedReader(new InputStreamReader((url.openStream())));
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
