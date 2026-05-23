package com.webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles individual client connections.
 * Parses HTTP requests, retrieves requested files, and sends HTTP responses.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    // Directory where the web server looks for files
    private static final String WEB_ROOT = "./www";
    private static final String DEFAULT_FILE = "index.html";

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = clientSocket.getOutputStream();
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(output)
        ) {
            // 1. Parse the HTTP Request
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            logger.info("Request: " + requestLine);
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendErrorResponse(bufferedOutput, 400, "Bad Request");
                return;
            }

            String method = requestParts[0];
            String requestedPath = requestParts[1];

            // We only support GET requests for this simple server
            if (!method.equals("GET")) {
                sendErrorResponse(bufferedOutput, 405, "Method Not Allowed");
                return;
            }

            // 2. Determine File Path
            if (requestedPath.equals("/")) {
                requestedPath = "/" + DEFAULT_FILE;
            }

            // Remove leading slash to make it a relative path
            String relativePath = requestedPath.substring(1);
            Path filePath = Paths.get(WEB_ROOT, relativePath).normalize();
            Path webRootPath = Paths.get(WEB_ROOT).normalize();

            // 3. Security Check: Prevent Path Traversal attacks
            if (!filePath.startsWith(webRootPath)) {
                logger.warning("Attempted path traversal to: " + filePath);
                sendErrorResponse(bufferedOutput, 403, "Forbidden");
                return;
            }

            File file = filePath.toFile();

            // 4. Serve the File or 404
            if (file.exists() && !file.isDirectory()) {
                serveFile(bufferedOutput, file);
            } else {
                logger.warning("File not found: " + file.getAbsolutePath());
                sendErrorResponse(bufferedOutput, 404, "Not Found");
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Communication error with client", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }

    private void serveFile(BufferedOutputStream out, File file) throws IOException {
        byte[] fileData = Files.readAllBytes(file.toPath());
        String contentType = MimeTypeDetector.getContentType(file.getName());

        // Construct HTTP Response Headers
        String responseHeaders = 
            "HTTP/1.1 200 OK\r\n" +
            "Server: JavaBareBonesServer/1.0\r\n" +
            "Date: " + new Date() + "\r\n" +
            "Content-type: " + contentType + "\r\n" +
            "Content-length: " + fileData.length + "\r\n" +
            "Connection: close\r\n" +
            "\r\n"; // End of headers

        // Write headers
        out.write(responseHeaders.getBytes());
        // Write body
        out.write(fileData);
        out.flush();
    }

    private void sendErrorResponse(BufferedOutputStream out, int statusCode, String statusMessage) throws IOException {
        String htmlBody = "<html><body><h1>" + statusCode + " " + statusMessage + "</h1></body></html>";
        
        String responseHeaders = 
            "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
            "Server: JavaBareBonesServer/1.0\r\n" +
            "Date: " + new Date() + "\r\n" +
            "Content-type: text/html\r\n" +
            "Content-length: " + htmlBody.length() + "\r\n" +
            "Connection: close\r\n" +
            "\r\n"; // End of headers

        out.write(responseHeaders.getBytes());
        out.write(htmlBody.getBytes());
        out.flush();
    }
}
