import java.io.*;
import java.net.*;

public class dsclient {
    public static void main(String[] args) {

        try {
            // Establish a socket connection to the server-side simulator
            Socket socket = new Socket("localhost", 50000);
            // Set up input and output streams for the socket
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Connect to the data server
            // Send HELO message to initiate communication
            outputStream.write(("HELO\n").getBytes());
            outputStream.flush();
            String receivedMsg = (String) inputStream.readLine();

            // Send AUTH username to authenticate the client
            String userName = System.getProperty("user.name");
            outputStream.write(("AUTH " + userName + "\n").getBytes());
            outputStream.flush();
            receivedMsg = (String) inputStream.readLine();

            // Variables for finding the server type and ID with the most cores
            int maxCores = 0;
            int serverCount = 0;
            String largestServerType = "";
            boolean firstTime = true;
            int currServer = 0;

            while (true) {  
                // Send REDY message to receive the next job from the server
                outputStream.write(("REDY\n").getBytes());
                outputStream.flush();
                receivedMsg = inputStream.readLine();

                // If the job type is NONE, there are no more jobs and the loop should be exited
                if (receivedMsg.equals("NONE"))
                    break; // Exit the loop

                // Get the job type and ID from the received message
                String[] jobData = receivedMsg.split(" ");
                String jobType = jobData[0];
                String jobId = jobData[2];

                // If the job type is JCPL, continue to the next iteration
                if (jobType.equals("JCPL")) // JCPL - provide the information on most recent job completion
                    continue; // Ignore the message and continue to the next iteration

                // Request server information from the server-side simulator for the first job
                if (firstTime) {
                    outputStream.write(("GETS All\n").getBytes()); // GETS - request information on all servers
                    outputStream.flush();
                    receivedMsg = (String) inputStream.readLine();

                    outputStream.write(("OK\n").getBytes());
                    outputStream.flush();

                    String[] serverListInfo = receivedMsg.split(" ");
                    int numServers = Integer.parseInt(serverListInfo[1]);

                    // Iterate through the server list and find the largest server type
                    for (int i = 0; i < numServers; i++) {
                        receivedMsg = (String) inputStream.readLine();

                        // Find the largest server type and ID (LRR scheduling - Largest server, Round robin)
                        String[] serverData = receivedMsg.split(" ");
                        String serverType = serverData[0];
                        int coreCount = Integer.parseInt(serverData[4]);

                        if (coreCount > maxCores) {
                            largestServerType = serverType;
                            maxCores = coreCount;
                            serverCount = 1;
                        } else if (serverType.equals(largestServerType)) {
                            serverCount++;
                        }
                    }

                    outputStream.write(("OK\n").getBytes());
                    outputStream.flush();
                    receivedMsg = (String) inputStream.readLine();
                }
                firstTime = false;

                // Schedule the job if it's a JOBN type
                if (jobType.equals("JOBN")) {
                    // Send SCHD message to schedule the job on the server with the largest core count
                    String scheduleMsg = "SCHD " + jobId + " " + largestServerType + " " + currServer + "\n"; // SCHD - schedule a job on a server
                    outputStream.write(scheduleMsg.getBytes());

                    outputStream.flush();
                    currServer++;
                    currServer = currServer % serverCount;
                    receivedMsg = inputStream.readLine();
                }
            }

            // Terminate the simulation gracefully by sending the QUIT message
            outputStream.write(("QUIT\n").getBytes());
            outputStream.flush(); // Immediately write the message to the output stream
            receivedMsg = inputStream.readLine();

            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}