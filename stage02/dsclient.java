import java.net.*;
import java.io.*;

// Distributed System Client Class
public class dsClient {
    // Network and I/O variables
    private Socket clientSocket;
    private DataOutputStream outStream;
    private BufferedReader inStream;

    // State and job related variables
    private int serverCount = 0;
    private int jobCounter = 0;

    private String serverMessage = "";
    private String numRecordsString = "";
    private String currentJobType = "";
    private String serverLoopMsg = "";
    private String optimalServerType = "";
    private String optimalServerID = "";

    private String[] jobTypeArray;
    private String[] recordsArray;
    private String[] optimalServerArray;

    // Constructor establishes connection to server
    public dsClient(String address, int port) throws Exception {
        clientSocket = new Socket(address, port);
        outStream = new DataOutputStream(clientSocket.getOutputStream());
        inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        dsClient client = new dsClient("localhost", 50000);
        client.executeClient();
        client.close();
    }

    public void close() throws Exception {
        clientSocket.close();
        inStream.close();
        outStream.close();
    }

    public void executeClient() throws Exception {
        handshakeAndAuth();
        jobHandler();
    }

    private void handshakeAndAuth() throws Exception {
        sendMessage("HELO");  // initial handshake
        serverMessage = read();  // server response

        // authentication
        String clientUsername = System.getProperty("user.name");
        sendMessage("AUTH " + clientUsername);
        serverMessage = read();  // server response
    }

    private void jobHandler() throws Exception {
        // Job handling loop
        while (!currentJobType.equals("NONE")) {
            handleJob();
        }

        // All jobs handled, send quit signal to server
        sendMessage("QUIT");
        read();
    }

    private void handleJob() throws Exception {
        sendMessage("REDY");  // ready for new job
        serverLoopMsg = read();
        jobTypeArray = serverLoopMsg.split(" ");
        currentJobType = jobTypeArray[0];

        if (!jobCompleteOrNone()) {
            manageJobAvailability();
            if (serverCount != 0) {
                processServerAvailability();
            } else {
                manageJobCapability();
                processServerAvailability();
            }

            if (currentJobType.equals("JOBN")) {
                scheduleJob();
            }
        }
    }

    private boolean jobCompleteOrNone() {
        return currentJobType.equals("JCPL") || currentJobType.equals("NONE");
    }

    private void manageJobAvailability() throws Exception {
        sendMessage("GETS Avail " + jobTypeArray[4] + " " + jobTypeArray[5] + " " + jobTypeArray[6]);
        numRecordsString = read();
        recordsArray = numRecordsString.split(" ");
        serverCount = Integer.parseInt(recordsArray[1]);
        sendMessage("OK");
    }

    private void manageJobCapability() throws Exception {
        read();
        sendMessage("GETS Capable " + jobTypeArray[4] + " " + jobTypeArray[5] + " " + jobTypeArray[6]);
        numRecordsString = read();
        recordsArray = numRecordsString.split(" ");
        serverCount = Integer.parseInt(recordsArray[1]);
        sendMessage("OK");
    }

    private void processServerAvailability() throws Exception {
        serverMessage = read();
        optimalServerArray = serverMessage.split(" ");
        optimalServerType = optimalServerArray[0];
        optimalServerID = optimalServerArray[1];

        for (int i = 0; i < serverCount - 1; i++) {
            read();
        }
        sendMessage("OK");
        read();
    }

    private void scheduleJob() throws Exception {
        sendMessage("SCHD " + jobCounter + " " + optimalServerType + " " + optimalServerID);
        read();
        jobCounter++;
    }

    private void sendMessage(String message) throws Exception {
        outStream.write((message + "\n").getBytes("UTF-8"));
    }

    private String read() throws Exception {
        return inStream.readLine();
    }
}