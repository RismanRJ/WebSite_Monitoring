package servlet;

import javax.servlet.http.*;

import org.mindrot.jbcrypt.BCrypt;
import util.DBUtil;

import java.io.IOException;
import java.sql.*;


public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Username and password required");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("Username already exists");
                return;
            }

            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password_hash) VALUES (?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, hash);
            insertStmt.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Registration successful");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
