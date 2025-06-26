package Models;

public class ResponseRecord {
    public String timestamp;
    public int responseTime;

    public ResponseRecord(String timestamp, int responseTime) {
        this.timestamp = timestamp;
        this.responseTime = responseTime;
    }
}
