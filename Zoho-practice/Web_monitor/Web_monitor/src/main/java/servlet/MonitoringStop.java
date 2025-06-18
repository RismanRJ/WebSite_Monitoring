package servlet;

import Scheduler.MonitoringService;
import util.DBUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MonitoringStop extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Login required");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String url = req.getParameter("url");

        System.out.println("Stop monitoring: "+userId+" "+url);

        try (Connection conn = DBUtil.getConnection()) {

            String selectQuery = "SELECT id FROM urls WHERE url = ? AND user_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                selectStmt.setString(1, url);
                selectStmt.setInt(2, userId);

                ResultSet rs = selectStmt.executeQuery();
                    if (rs.next()) {
                        int urlId = rs.getInt("id");

                        // Stop monitoring
                        MonitoringService.getInstance().stopMonitoring(urlId);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("Monitoring stopped");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("Failed to stop url");
                    }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Failed to stop url "+e.getMessage());
        }
    }
}
