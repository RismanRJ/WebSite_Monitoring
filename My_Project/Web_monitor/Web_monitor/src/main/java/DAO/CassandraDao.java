package DAO;

import Models.AlertResponseRecord;
import Models.MonitoredUrl;
import Models.ResponseRecord;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CassandraDao {

    public static final CqlSession session = CqlSession.builder()
            .addContactPoint(new InetSocketAddress("localhost", 9042)) // adjust if containerized
            .withKeyspace("monitoring")
            .withLocalDatacenter("datacenter1")
            .build();

    private static final PreparedStatement insertStmt = session.prepare(
            "INSERT INTO response_times (user_id, url, timestamp, response_time_ms) VALUES (?, ?, ?, ?);"
    );

    public static void save(int userId, String url, long responseTimeMs) {
        try {
            session.execute(insertStmt.bind(userId, url, Instant.now(), (int) responseTimeMs));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ResponseRecord> getRecentResponseTimes(int userId, String url) {
        List<ResponseRecord> records = new ArrayList<>();
        ResultSet rs = session.execute(
                SimpleStatement.builder("SELECT timestamp, response_time_ms FROM response_times WHERE user_id=? AND url=?  ORDER BY timestamp DESC LIMIT 10")
                        .addPositionalValues(userId, url)
                        .build()
        );

        for (Row row : rs) {
            Instant timestamp = row.getInstant("timestamp");
            int responseTime = row.getInt("response_time_ms");
            if (timestamp != null) {
                records.add(new ResponseRecord(timestamp.toString(), responseTime));
            }
        }
        return records;
    }

    public static void saveAlertResponse(int userId, long responseTime, int urlId,boolean flag) {
        try {
            PreparedStatement preparedStatement = session.prepare(
                    "INSERT INTO alert_response (user_id, url_id, alert_time, response_time_ms, send_status) VALUES (?, ?, ?, ?, ?);"
            );

            Instant now = Instant.now();
            session.execute(preparedStatement.bind(userId, urlId,now , responseTime,flag));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<AlertResponseRecord> getAlertResponse(int userId, int urlId){
        List<AlertResponseRecord> alertResponseRecords = new ArrayList<>();

        PreparedStatement preparedStatement = session.prepare("select alert_time,response_time_ms, send_status from alert_response where user_id=? and url_id=? order by alert_time desc limit 15");
        ResultSet resultSet = session.execute( preparedStatement.bind(userId,urlId));

        for (Row row : resultSet) {
            Instant timestamp = row.getInstant("alert_time");
            boolean sendStatus = row.getBoolean("send_status");
            long responseTime = 0;
            if (!row.isNull("response_time_ms")) {
                responseTime = row.getLong("response_time_ms");
            }
            if (timestamp != null) {
                alertResponseRecords.add(new AlertResponseRecord(timestamp.toString(), (int) responseTime,sendStatus));
            }
        }


        return alertResponseRecords;
    }


}
