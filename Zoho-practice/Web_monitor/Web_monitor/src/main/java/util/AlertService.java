package util;


import DAO.CassandraDao;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlertService {
    private static final Map<Integer, Instant> lastAlertTime = new ConcurrentHashMap<>();



    public static void sendAlert(int urlId, long responseTime, int userId,String webhook,String currentUrl) {
        Instant now = Instant.now();
        Instant lastSent = lastAlertTime.getOrDefault(urlId, Instant.EPOCH);

        if (lastSent.plusSeconds(300).isAfter(now)) {
            CassandraDao.saveAlertResponse(userId, responseTime, urlId,false);
            return;
        }

        String payload = "{ \"url\": \"" + currentUrl + "\", " +
                "\"responseTime\": " + responseTime + ", " +
                "\"message\": \"" + (responseTime == -1 ? "Unreachable" : "Threshold breached") + "\" }";

        try {
            URL url = new URL(webhook);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            boolean flag = true;
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Alert sent to webhook for user " + userId);
                lastAlertTime.put(urlId, now);
            } else {
                System.err.println("Webhook call failed with response code: " + responseCode);
                flag= false;
            }
            CassandraDao.saveAlertResponse(userId, responseTime, urlId,flag);
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Failed to send Webhook alert: " + e.getMessage());

        }
    }
}
