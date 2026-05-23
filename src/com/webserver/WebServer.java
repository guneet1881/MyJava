package com.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bare-bones, multithreaded HTTP web server using core Java Sockets.
 */
public class WebServer {
    private static final Logger logger = Logger.getLogger(WebServer.class.getName());
    
    // The port the server will listen on
    private static final int PORT = 8080;
    
    // Thread pool to handle multiple concurrent clients
    // A fixed thread pool of 10 is usually enough for a simple local server
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("WebServer started successfully.");
            logger.info("Listening for connections on port " + PORT + "...");
            logger.info("Point your browser to http://localhost:" + PORT);
            
            // Infinite loop to accept incoming client connections
            while (true) {
                // This blocks until a client connects
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                
                // Hand off the client socket to a separate thread for processing
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server encountered an error and must shut down", e);
        } finally {
            threadPool.shutdown();
        }
    }
}
