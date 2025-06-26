package servlet;

import org.mindrot.jbcrypt.BCrypt;
import util.DBUtil;

import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");


        System.out.println("Received login attempt: " + username);

        try (Connection conn = DBUtil.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, password_hash FROM users WHERE username = ?"
            );
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();


            if (rs.next()) {
                String hash = rs.getString("password_hash");

                if (BCrypt.checkpw(password, hash)) {
                    int userId = rs.getInt("id");


                    HttpSession session = req.getSession();
                    session.setAttribute("userId", userId);
                    session.setAttribute("username", username);


                    Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                    sessionCookie.setPath("/");
                    sessionCookie.setHttpOnly(true);
                    sessionCookie.setSecure(false);
                    sessionCookie.setMaxAge(15 * 60);
                    resp.addCookie(sessionCookie);


                    resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                    resp.setHeader("Access-Control-Allow-Credentials", "true");


                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Login successful");
                    return;
                }
            }


            resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid credentials");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
