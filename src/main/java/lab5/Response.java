package lab;

public class Response {

    private String hostName;
    private int responseTime;

    Response(String hostName, int responseTime) {
        this.hostName = hostName;
        this.responseTime = responseTime;
    }

    public String getHostName() {
        return hostName;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }
}