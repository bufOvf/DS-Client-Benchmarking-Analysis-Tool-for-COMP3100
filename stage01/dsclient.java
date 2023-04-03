import java.io.*;
import java.net.*;

public class dsclient {

  // Function to read messages (Server commands)
  public static String readMessage(BufferedReader reader) throws IOException {
    String message = reader.readLine();
    System.out.println("RCVD " + message);
    return message;
  }

  // Function to send messages (Client commands)
  public static void sendMessage(String messageOut, DataOutputStream out) throws IOException {
    out.write(messageOut.getBytes());
    out.flush();
    System.out.print("SENT " + messageOut);
  }

  // Function to start HANDSHAKE messages (HELO -> OK) and Authentication (AUTH ->
  // OK)
  public static void heloAuth(BufferedReader reader, DataOutputStream out) throws IOException {
    sendMessage("HELO\n", out);
    readMessage(reader);
    sendMessage("AUTH " + System.getProperty("user.name") + "\n", out);
    readMessage(reader);
  }

  // Function to send QUIT message and close input stream, output stream, and
  // socket
  public static void quitAndClose(BufferedReader reader, DataOutputStream out, Socket socket) throws IOException {
    sendMessage("QUIT\n", out);
    readMessage(reader);
    reader.close();
    out.close();
    socket.close();
  }

  public static void main(String args[]) {
    try {
      Socket socket = new Socket("localhost", 50000);

      // Print connection details
      System.out.println("# Target IP:" + socket.getInetAddress() + " Target Port: " + socket.getPort());
      System.out.println("# Local IP:" + socket.getLocalAddress() + " Local Port: " + socket.getLocalPort());
      System.out.println("# Connection Established...");

      // Initialize reader and output stream
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      // Perform handshake and authentication
      heloAuth(reader, out);

      // Initialize variables for message and server details
      String currentServerMessage = "";
      String largestServerType = "";
      int largestServerCores = 0;

      // Loop until server message is NONE
      while (!currentServerMessage.contains("NONE")) {
        System.out.println("# in while not none loop");

        // Send REDY message to the server
        sendMessage("REDY\n", out);
        currentServerMessage = readMessage(reader);
        
        // Send GETS message to the server for capable servers
        if (currentServerMessage.contains("JOBN")) {
          System.out.println("# Received JOBN message: " + currentServerMessage);
          String[] javaArrayJobn = currentServerMessage.split(" ");
          if (javaArrayJobn.length >= 7) {
            sendMessage("GETS Capable " + javaArrayJobn[4] + " " + javaArrayJobn[5] + " " + javaArrayJobn[6] + "\n",
                out);
          } else {
            System.out.println("# Invalid JOBN message: " + currentServerMessage);
          }

          // Receive server message for capable servers
          currentServerMessage = readMessage(reader);
          // System.out.println("GETS Server Comment: " + currentServerMessage);

          // Get DATA n n from server message, then send OK message to server
          if (currentServerMessage.contains("DATA")) {
            sendMessage("OK\n", out);
            String[] javaArrayData = currentServerMessage.split(" ");
            int nRecs = Integer.parseInt(javaArrayData[1]);
            String[] servers = new String[nRecs];
            for (int i = 0; i < nRecs; i++) {
              currentServerMessage = readMessage(reader);
              servers[i] = currentServerMessage;
            }

            // Send OK message to server once all servers have been parsed
            sendMessage("OK\n", out);
            

            // Loop through servers array to find server with most cores
            for (String server : servers) {
              String[] serverIdx = server.split(" ");
              if (serverIdx.length >= 5) { // Ensure that there are enough elements to parse cores
                int serverCores = Integer.parseInt(serverIdx[4]);
                if (serverCores > largestServerCores) {
                  largestServerType = serverIdx[0];
                  largestServerCores = serverCores;
                }
              }
            }

            // Schedule the job with the server with the most cores
            currentServerMessage = readMessage(reader);
            if (currentServerMessage.equals(".")) {
              sendMessage("SCHD " + javaArrayJobn[2] + " " + largestServerType + " 0\n", out);
              currentServerMessage = readMessage(reader);
            }

          }
        }

      }

      // Send QUIT message and close input stream, output stream, and socket
      quitAndClose(reader, out, socket);
    } catch (IOException e) {
      System.out.println("Listen :" + e.getMessage());
    }
  }
}
