import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer {

    public static void main(String[] args) throws Exception {
        // Set the port number.
        int port = 6789;
        
        // Establish the listen socket.
        ServerSocket listenSocket = new ServerSocket(port);

        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request to the port.
            Socket connectionSocket = listenSocket.accept(); 

            // Construct an object to process the HTTP request message.
            HttpRequest request = new HttpRequest(connectionSocket);  

            // Create a new thread to process the request
            Thread thread = new Thread(request);

            // Start the thread
            thread.start();
        }
    }
}
final class HttpRequest implements Runnable {
    
    final static String CRLF = "\r\n"; // used to terminate line of response msg
    Socket socket;
    
    // Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }
    
    // Implement the run() method of the Runnable interface
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    // Does the actual processing of the request
    private void processRequest() throws Exception {
        // Get a reference to the socket's input and output streams
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            
        //To read the input stream we use InputStreamReader and BufferedReader
        BufferedReader br =  new BufferedReader(new InputStreamReader(is));
        
        // Get the request line of the HTTP request message
        String requestLine = br.readLine(); 
        
        // Display the request line
        System.out.println();
        System.out.println(requestLine);
        
        // Get and display the header lines
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        
        //close streams and socket.
        is.close();
        os.close();
        br.close();
        socket.close();
    }
}