package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.listener;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EntityManagerUtil.getEntityManagerFactory();
        System.out.println("EntityManagerFactory init");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EntityManagerUtil.closeEntityManagerFactory();
        System.out.println("EntityManagerFactory close");
    }
}
