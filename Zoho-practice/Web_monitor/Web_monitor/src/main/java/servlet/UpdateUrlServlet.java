package servlet;

import Models.UpdateUrlRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import util.DBUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UpdateUrlServlet extends HttpServlet {

//    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
//        HttpSession session = req.getSession(false);
//        if (session == null || session.getAttribute("userId") == null) {
//            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            res.getWriter().write("Login required");
//            return;
//        }
//
//        int userId = (int) session.getAttribute("userId");
//        String urlId = req.getParameter("id");
//        String threshold = req.getParameter("threshold");
//        String webhook = req.getParameter("webhook");
//
//        try (Connection conn = DBUtil.getConnection()) {
//            String sql = "UPDATE urls SET threshold_ms = ?, webhook = ? WHERE user_id = ? AND id = ?";
//            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                stmt.setInt(1, Integer.parseInt(threshold));
//                stmt.setString(2, webhook);
//                stmt.setInt(3, userId);
//                stmt.setInt(4, Integer.parseInt(urlId));
//
//                int rowsUpdated = stmt.executeUpdate();
//
//                if (rowsUpdated > 0) {
//                    res.setStatus(HttpServletResponse.SC_OK);
//                    res.getWriter().write("Data updated successfully");
//                } else {
//                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                    res.getWriter().write("No matching record found");
//                }
//            }
//        } catch (Exception e) {
//            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            res.getWriter().write("Error: " + e.getMessage());
//        }
//    }
protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("Login required");
        return;
    }

    int userId = (int) session.getAttribute("userId");


    StringBuilder sb = new StringBuilder();
    String line;
    try (BufferedReader reader = req.getReader()) {
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
    }

    Gson gson = new Gson();
    try {
        UpdateUrlRequest updateRequest = gson.fromJson(sb.toString(), UpdateUrlRequest.class);

        String urlId = updateRequest.getId();
        String threshold = updateRequest.getThreshold();
        String webhook = updateRequest.getWebhook();

        if (urlId == null || threshold == null || webhook == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Missing required fields.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE urls SET threshold_ms = ?, webhook = ? WHERE user_id = ? AND id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(threshold));
                stmt.setString(2, webhook);
                stmt.setInt(3, userId);
                stmt.setInt(4, Integer.parseInt(urlId));

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().write("Data updated successfully");
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("No matching record found");
                }
            }
        }
    } catch (JsonSyntaxException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid JSON format: " + e.getMessage());
    } catch (NumberFormatException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid number format: " + e.getMessage());
    } catch (Exception e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.getWriter().write("Error: " + e.getMessage());
    }
}


}
