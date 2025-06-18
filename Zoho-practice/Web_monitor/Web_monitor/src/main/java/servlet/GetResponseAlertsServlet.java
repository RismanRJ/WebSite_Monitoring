package servlet;

import DAO.CassandraDao;
import Models.AlertResponseRecord;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class GetResponseAlertsServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
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


        try{

            List<AlertResponseRecord> alertResponseRecords = CassandraDao.getAlertResponse(userId,Integer.parseInt(urlId));
            res.setContentType("application/json");
            res.getWriter().write(new Gson().toJson(alertResponseRecords));

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error: " + e.getMessage());
        }

    }

}
