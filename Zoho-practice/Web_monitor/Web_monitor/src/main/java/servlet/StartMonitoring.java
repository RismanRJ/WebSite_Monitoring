package servlet;

import Scheduler.MonitoringService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class StartMonitoring extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Login required");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String url = req.getParameter("url");
        int urlId = Integer.parseInt( req.getParameter("urlId"));

        MonitoringService.getInstance().startMonitoring(urlId,url,userId);

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("Monitoring resumed");



    }
}
