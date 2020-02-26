package servlet;

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
/**
  * A servlet for downloading tle data from space-track
  * @author space-track.org
  *
  */
@WebServlet(name = "TLEServlet")
public class TLEServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //when click the button get the info, write to a file, "success"
        //https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/format/3le
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
