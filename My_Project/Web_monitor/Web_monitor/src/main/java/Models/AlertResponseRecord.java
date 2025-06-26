package Models;

public class AlertResponseRecord {
    public String alertTime;
    public int responseTime;
    public boolean sendStatus;

    public AlertResponseRecord(String alertTime, int responseTime,boolean sendStatus) {
        this.alertTime = alertTime;
        this.responseTime = responseTime;
        this.sendStatus = sendStatus;
    }
}
