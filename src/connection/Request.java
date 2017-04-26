package connection;

import connection.Connection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Request is a class that allows for easy set up and sending of a request.
 */
public class Request extends Connection {

    public Request(URL url) {
        super();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader buf = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();
            String line;

            while ((line = buf.readLine()) != null) {
                response.append(line);
            }

            buf.close();

            JSONParser parser = new JSONParser();

            try {
                this.content = (JSONObject) parser.parse(response.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (this.content.get("success").equals("true")) {
                this.setStatus(200);
            } else {
                this.setStatus(404);
            }

            this.contentLength = content.size();
            this.contentType = "application/json";
        } catch (IOException e) {
            this.content = new JSONObject();
            this.content.put("success", "false");
            this.content.put("err", "No response");
            this.content.put("stacktrace", e.getMessage()); // for debugging
//            e.printStackTrace(); // for debugging, remove later
        }
    }
}
