package connection;

import org.json.simple.JSONObject;

/**
 * Connection is an abstract class that establishes what information connections between Clients and Paxos should have.
 */
public abstract class Connection {

    protected int code;
    protected String status;
    protected String contentType;
    protected int contentLength;
    protected JSONObject content;

    public Connection() {
    }

    /**
     * Set the status based on the code.
     * @param code
     */
    public void setStatus(int code) {
        this.code = code;
        switch (code) {
            case 200:
                this.status = "HTTP/1.0 200 OK";
                break;
            case 400:
                this.status = "HTTP/1.0 400 Bad Request";
                break;
            case 404:
                this.status = "HTTP/1.0 404 Not Found";
                break;
            case 405:
                this.status = "HTTP/1.0 405 Method Not Allow";
                break;
            default:
                this.status = "HTTP/1.0 500 Internal Server Error";
                this.code = 500;
        }
    }

    /**
     * Get the content of the response.
     * @return
     */
    public JSONObject getContent() {
        return this.content;
    }

    /**
     * Returns the response string formatted for HTTP.
     *
     * @return
     */
    @Override
    public String toString() {
        return status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "contentLength: " + contentLength + "\r\n" +
                "\r\n" +
                content.toString() + "\r\n";
    }
}
