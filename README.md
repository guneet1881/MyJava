# Java Bare-Bones Local Web Server

A fully functional, multithreaded web server built from scratch using pure Java (`ServerSocket` and `Socket`). This project intentionally bypasses modern web frameworks like Spring Boot to demonstrate a raw understanding of networking, HTTP protocols, and core Java concepts.

## Features

- **Core Java Networking**: Uses pure `java.net.ServerSocket` and `java.net.Socket`.
- **Multithreaded**: Utilizes an `ExecutorService` thread pool to handle concurrent client requests efficiently.
- **HTTP Parsing**: Reads and parses raw HTTP GET requests from incoming streams.
- **Dynamic File Serving**: Maps requested paths to the local `./www` directory and serves static files.
- **MIME Type Detection**: Automatically determines and sends correct `Content-Type` headers for HTML, CSS, JS, and image files.
- **Security Check**: Prevents basic directory traversal attacks (e.g., trying to access files outside the `www` directory).

## How to Run

1. **Compile the code:**
   ```bash
   javac src/com/webserver/*.java -d out/
   ```

2. **Start the server:**
   Ensure you run the server from the root directory of the project so it can find the `www` folder.
   ```bash
   java -cp out com.webserver.WebServer
   ```

3. **View the website:**
   Open your favorite web browser and navigate to:
   [http://localhost:8080](http://localhost:8080)

## Project Structure

- `src/com/webserver/WebServer.java`: Sets up the ServerSocket and accepts connections.
- `src/com/webserver/ClientHandler.java`: Parses individual HTTP requests and serves files.
- `src/com/webserver/MimeTypeDetector.java`: Utility to detect MIME types from file extensions.
- `www/`: Directory serving as the web root. Contains sample `index.html` and `style.css`.
