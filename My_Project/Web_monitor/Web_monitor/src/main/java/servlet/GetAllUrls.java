package servlet;

import util.DBUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetAllUrls extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Login required");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id, url, threshold_ms, webhook FROM urls WHERE user_id = ?");
            preparedStatement.setInt(1, userId);

            ResultSet rs = preparedStatement.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                int id = rs.getInt("id");
                String url = rs.getString("url");
                int threshold = rs.getInt("threshold_ms");
                String webhook = rs.getString("webhook");

                json.append("{")
                        .append("\"id\":").append(id).append(",")
                        .append("\"url\":\"").append(url).append("\",")
                        .append("\"threshold\":").append(threshold).append(",")
                        .append("\"webhook\":\"").append(webhook).append("\"")
                        .append("}");
            }

            json.append("]");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
