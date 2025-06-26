package Scheduler;

import DAO.CassandraDao;
import util.AlertService;
import util.DBUtil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;

public class MonitoringService {
    private static MonitoringService instance;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    private final Map<Integer, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    private MonitoringService() {}

    public static synchronized MonitoringService getInstance() {
        if (instance == null) instance = new MonitoringService();
        return instance;
    }

    public void startMonitoring(int urlId,String currentUrl, int userId) {
        try {
            if (runningTasks.containsKey(urlId)) return;

            System.out.println("Started monitoring: " + currentUrl);

            Runnable task = () -> {
                HttpURLConnection conn = null;
                long responseTime = -1;
                String webhook="";
                try (Connection conne = DBUtil.getConnection();
                     PreparedStatement ps = conne.prepareStatement("SELECT id, threshold_ms, webhook FROM urls WHERE url = ? AND user_id = ?")) {

                    ps.setString(1, currentUrl);
                    ps.setInt(2, userId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.err.println("URL removed or not found: " + currentUrl);
                            stopMonitoring(urlId);  // Stop task if URL was deleted
                            return;
                        }

                        int dbId = rs.getInt("id");
                        int threshold = rs.getInt("threshold_ms");
                         webhook = rs.getString("webhook");

                        long start = System.currentTimeMillis();
                        conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                        conn.setConnectTimeout(2000);
                        conn.setReadTimeout(2000);
                        conn.connect();

                        int statusCode = conn.getResponseCode();
                        responseTime = System.currentTimeMillis() - start;

                        System.out.println("Current time "+LocalTime.now());

                        System.out.println("Response from " + currentUrl + ": " + statusCode + " in " + responseTime + " ms");
                        CassandraDao.save(userId, currentUrl, responseTime);

                        if (statusCode >= 400 || responseTime > threshold) {
                            AlertService.sendAlert(urlId, responseTime,userId,webhook,currentUrl);

                        }
                    }

                } catch (Exception e) {
                    System.err.println("Monitoring error for: " + currentUrl+" "+e.getMessage());
                    AlertService.sendAlert(urlId, -1, userId, webhook,currentUrl);
                } finally {
                    if (conn != null) conn.disconnect();
                    System.out.println("Checked at: " + LocalTime.now());
                }
            };

            ScheduledFuture<?> future = executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
            runningTasks.put(urlId, future);
        } catch (Exception e) {
            System.out.println("Exception "+ e.getMessage());
        }
    }

    public void stopMonitoring(int urlId) {
        ScheduledFuture<?> task = runningTasks.remove(urlId);
        if (task != null) {
            task.cancel(true);
            System.out.println("Stopped monitoring for URL ID: " + urlId);
        }
    }

    public void shutdown() {

            // Cancel all running scheduled tasks
            for (ScheduledFuture<?> future : runningTasks.values()) {
                future.cancel(true); // true to interrupt if running
            }
            runningTasks.clear();

            // Shutdown the executor service
            executor.shutdown(); // disables new tasks from being submitted

            try {
                // Wait a while for existing tasks to terminate
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // cancel currently executing tasks
                    // Wait again for tasks to respond to being cancelled
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }

            System.out.println("MonitoringService executor shut down.");

    }
}
