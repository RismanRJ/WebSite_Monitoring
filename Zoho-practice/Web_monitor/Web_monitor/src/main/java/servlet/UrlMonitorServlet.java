package servlet;


import Models.MonitoredUrl;
import Scheduler.MonitoringService;
import javax.servlet.http.*;

import util.DBUtil;

import java.io.IOException;
import java.sql.*;

public class UrlMonitorServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Login required");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String url = req.getParameter("url");
        int threshold = Integer.parseInt(req.getParameter("threshold"));
        String webhook = req.getParameter("webhook");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO urls (user_id, url, threshold_ms, webhook) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setInt(1, userId);
            stmt.setString(2, url);
            stmt.setInt(3, threshold);
            stmt.setString(4, webhook);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int urlId = rs.getInt(1);

                MonitoringService.getInstance().startMonitoring(
                        urlId,url,userId
                );

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Monitoring started");
            } else {
                throw new SQLException("URL insert failed");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }


}
