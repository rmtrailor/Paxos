package connection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;

/**
 * Response is a class that allows for easy set up and sending of a response.
 */
public class Response extends Connection {

    /**
     * Creates a response object with the information we already have.
     * @param code
     * @param content
     */
    public Response(int code, JSONObject content) {
        this.setStatus(code);
        this.content = content;
        this.contentType = "application/json";
        this.contentLength = content.size();
    }

    /**
     * Creates a Response Object by getting the response from another server.
     * @param url
     * @throws IOException
     */
    public Response(URL url) throws ProtocolException {
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

    /**
     * Send the formatted response back to the requester.
     * @param sock
     * @throws IOException
     */
    public void sendResponse(Socket sock) throws IOException {
        OutputStream out = sock.getOutputStream();
        out.write(this.toString().getBytes());
        out.flush();
        out.close();
    }
}
