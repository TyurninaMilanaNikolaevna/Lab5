package lab5;

public class Response {

    private final String hostName;
    private final Long responseTime;

    Response(String hostName, Long responseTime) {
        this.hostName = hostName;
        this.responseTime = responseTime;
    }

    public String getHostName() {
        return hostName;
    }
    public Long getResponseTime() {
        return responseTime;
    }
}