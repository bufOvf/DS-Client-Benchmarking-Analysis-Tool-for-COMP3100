import java.net.*;
import java.io.*;

// Distributed System Client Class
public class dsClient {

    // Network and I/O variables used for managing communication with the server.
    private Socket clientSocket;
    private DataOutputStream outStream;
    private BufferedReader inStream;

    // State and job related variables are used for handling and tracking jobs.
    private int serverCount = 0;
    private int jobCounter = 0;

    // These string variables store various responses and messages received from the server.
    private String serverMessage = "";
    private String numRecordsString = "";
    private String currentJobType = "";
    private String serverLoopMsg = "";
    private String optimalServerType = "";
    private String optimalServerID = "";
    // These arrays are used to split server messages into separate parts for further analysis.
    private String[] jobTypeArray;
    private String[] recordsArray;
    private String[] optimalServerArray;
    // Constructor establishes connection to the server.
    public dsClient(String address, int port) throws Exception {
        clientSocket = new Socket(address, port);
        outStream = new DataOutputStream(clientSocket.getOutputStream());
        inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    public static void main(String[] args) throws Exception {
        // Instantiate the client and execute its main functionality, then close the client connection.
        dsClient client = new dsClient("localhost", 50000);
        client.executeClient();
        client.close();
    }
    // Method to close the client socket and streams once operations are complete.
    public void close() throws Exception {
        clientSocket.close();
        inStream.close();
        outStream.close();
    }
    // Main function to execute the client operations.
    public void executeClient() throws Exception {
        handshakeAndAuth();  // Initial connection handshake and user authentication.
        jobHandler();  // Handles job scheduling operations.
    }
    // Initial handshake and authentication function.
    private void handshakeAndAuth() throws Exception {
        sendMessage("HELO");  // Send initial handshake message.
        serverMessage = read();  // Receive server response.
        // Get the username from the system properties and send it for authentication.
        String clientUsername = System.getProperty("user.name");
        sendMessage("AUTH " + clientUsername);
        serverMessage = read();  // Receive server response.
    }
    // Job handling function which iterates until no jobs are left.
    private void jobHandler() throws Exception {
        while (!currentJobType.equals("NONE")) {
            handleJob();
        }

        // Once all jobs have been handled, send quit signal to server.
        sendMessage("QUIT");
        read();
    }
    // Function to handle a single job.
    private void handleJob() throws Exception {
        sendMessage("REDY");  // Send ready signal to the server.
        serverLoopMsg = read();
        jobTypeArray = serverLoopMsg.split(" ");
        currentJobType = jobTypeArray[0];

        // If job is not completed or none, manage job availability.
        if (!jobCompleteOrNone()) {
            manageJobAvailability();

            // Process server availability or capability based on the server count.
            if (serverCount != 0) {
                processServerAvailability();
            } else {
                manageJobCapability();
                processServerAvailability();
            }

            // If the current job is new, schedule it.
            if (currentJobType.equals("JOBN")) {
                scheduleJob();
            }
        }
    }
    // Check if the job is complete or none.
    private boolean jobCompleteOrNone() {
        return currentJobType.equals("JCPL") || currentJobType.equals("NONE");
    }
    // Manage job availability by asking the server for available resources.
    private void manageJobAvailability() throws Exception {
        sendMessage("GETS Avail " + jobTypeArray[4] + " " + jobTypeArray[5] + " " + jobTypeArray[6]);
        numRecordsString = read();
        recordsArray = numRecordsString.split(" ");
        serverCount = Integer.parseInt(recordsArray[1]);
        sendMessage("OK");
    }
    // Manage job capability by asking the server for servers capable of handling the job.
    private void manageJobCapability() throws Exception {
        read();
        sendMessage("GETS Capable " + jobTypeArray[4] + " " + jobTypeArray[5] + " " + jobTypeArray[6]);
        numRecordsString = read();
        recordsArray = numRecordsString.split(" ");
        serverCount = Integer.parseInt(recordsArray[1]);
        sendMessage("OK");
    }
    // Process the server's response on availability.
    private void processServerAvailability() throws Exception {
        serverMessage = read();
        optimalServerArray = serverMessage.split(" ");
        optimalServerType = optimalServerArray[0];
        optimalServerID = optimalServerArray[1];

        // Read remaining server messages, if any.
        for (int i = 0; i < serverCount - 1; i++) {
            read();
        }
        sendMessage("OK");
        read();
    }
    // Schedule a job by sending the scheduling message to the server.
    private void scheduleJob() throws Exception {
        sendMessage("SCHD " + jobCounter + " " + optimalServerType + " " + optimalServerID);
        read();
        jobCounter++;
    }
    // Send a message to the server.
    private void sendMessage(String message) throws Exception {
        outStream.write((message + "\n").getBytes("UTF-8"));
    }

    // Read a message from the server.
    private String read() throws Exception {
        return inStream.readLine();
    }
}
