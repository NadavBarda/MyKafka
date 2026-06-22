package servlets;

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        System.out.println(ri);
    }

    @Override
    public void close() throws IOException {
        System.out.println("close");
    }

}
