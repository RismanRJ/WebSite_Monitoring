package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.*;


import java.io.IOException;

public class TestServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("Test servlet loaded");
    }

    protected void doGet(HttpServletRequest Req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write("Hello from servlet!");
    }
}
