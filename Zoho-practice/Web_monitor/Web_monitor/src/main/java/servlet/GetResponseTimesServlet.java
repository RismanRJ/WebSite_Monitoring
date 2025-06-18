package servlet;

import DAO.CassandraDao;
import Models.ResponseRecord;
import com.google.gson.Gson;
import javax.servlet.http.*;

import java.io.IOException;
import java.util.List;


public class GetResponseTimesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String url = req.getParameter("url");

        if (url == null || url.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing URL");
            return;
        }

        try {
            List<ResponseRecord> records = CassandraDao.getRecentResponseTimes(userId, url);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(records));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
