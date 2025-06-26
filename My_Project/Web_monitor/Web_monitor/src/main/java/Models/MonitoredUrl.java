package Models;

public class MonitoredUrl {
    public int id;
    public int userId;
    public String url;
    public int threshold;
    public String webhook;

    public MonitoredUrl(int id, int userId, String url, int threshold, String webhook) {
        this.id = id;
        this.userId = userId;
        this.url = url;
        this.threshold = threshold;
        this.webhook = webhook;
    }
}
