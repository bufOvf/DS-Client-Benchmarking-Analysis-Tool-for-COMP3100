import java.io.*;
import java.net.*;

public class dsclient {
    public static void main(String[] args) {

        try {
            // Establish a socket connection
            Socket socket = new Socket("localhost", 50000);
            // Set up input and output streams for the socket
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Connect to the data server
            // Send HELO message
            outputStream.write(("HELO\n").getBytes());
            outputStream.flush();
            String receivedMsg = (String) inputStream.readLine();

            // Send AUTH username
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
                outputStream.write(("REDY\n").getBytes());
                outputStream.flush();
                receivedMsg = inputStream.readLine();

                // If the job type is NONE, there are no more jobs and the loop should be exited
                if (receivedMsg.equals("NONE"))
                    break;

                // Get the job type and ID
                String[] jobData = receivedMsg.split(" ");
                String jobType = jobData[0];
                String jobId = jobData[2];

                // If the job type is JCPL, continue to the next iteration
                if (jobType.equals("JCPL"))
                    continue;

                if (firstTime) {
                    outputStream.write(("GETS All\n").getBytes());
                    outputStream.flush();
                    receivedMsg = (String) inputStream.readLine();

                    outputStream.write(("OK\n").getBytes());
                    outputStream.flush();

                    String[] serverListInfo = receivedMsg.split(" ");
                    int numServers = Integer.parseInt(serverListInfo[1]);

                    for (int i = 0; i < numServers; i++) {
                        receivedMsg = (String) inputStream.readLine();

                        // Find the largest server type and ID
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
                    String scheduleMsg = "SCHD " + jobId + " " + largestServerType + " " + currServer + "\n";
                    outputStream.write(scheduleMsg.getBytes());
                    outputStream.flush();
                    currServer++;
                    currServer = currServer % serverCount;
                    receivedMsg = inputStream.readLine();
                }
            }

            // Terminate the simulation gracefully
            outputStream.write(("QUIT\n").getBytes());
            outputStream.flush();
            receivedMsg = inputStream.readLine();

            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
