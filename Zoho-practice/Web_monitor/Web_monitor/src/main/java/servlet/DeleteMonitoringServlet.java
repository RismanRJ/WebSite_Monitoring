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

public class DeleteMonitoringServlet extends HttpServlet {

    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Login required");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String urlId = req.getParameter("id");

        if (urlId == null || urlId.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Missing URL ID");
            return;
        }

        try (Connection connection = DBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM urls WHERE id = ? AND user_id = ?");
            preparedStatement.setInt(1, Integer.parseInt(urlId));
            preparedStatement.setInt(2, userId);

            int rowsAffected = preparedStatement.executeUpdate();  // use executeUpdate() for DELETE

            if (rowsAffected > 0) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("Deleted Successfully");
                MonitoringService.getInstance().stopMonitoring(Integer.parseInt(urlId));
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("No matching record found");
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error: " + e.getMessage());
        }
    }


}
