package server;

import servlets.Servlet;
import config.Config;

public interface HTTPServer extends Runnable {
    public void addServlet(String httpCommanmd, String uri, Servlet s);

    public void removeServlet(String httpCommanmd, String uri);

    public void start();

    public void close();

    public void setConfig(Config config);

    public Config getConfig();
}
