package util;

import DAO.CassandraDao;
import Scheduler.MonitoringService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;

@WebListener
public class MonitoringContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("MonitoringContextListener initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        System.out.println("Shutting down MonitoringService...");
        MonitoringService.getInstance().shutdown();
        CassandraDao.session.close();
        try {
            DBUtil.getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
