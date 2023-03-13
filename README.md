# comp3100-project

Testing dsclient with S1Tests-wk6.sh:
- run in "stage01" directory

`javac dsclient.java && ./S1Tests-wk6.sh dsclient.class -n | tee complete_log.txt`

Testing dsclient with demoS1.sh:
- extract demoS1.tar in same directory as dsclient.class and ds-server before running command

 `./demoS1.sh -n dsclient.class`


Psudo-code:
```

Begin
  Create a socket connection to the server-side simulator
  Set up input and output streams for the socket

  Send HELO message to initiate communication
  Receive response

  Send AUTH username to authenticate the client
  Receive response

  Initialize variables for finding the server type and ID with the most cores

  While true
    Send REDY message to receive the next job from the server
    Receive response

    If response is "NONE"
      Exit the loop

    Split response into jobData
    Get jobType and jobId from jobData

    If jobType is "JCPL"
      Continue to the next iteration

    If it's the first time receiving a job
      Request server information from the server-side simulator
      Send "GETS All" message
      Receive response

      Send "OK" message
      Receive response

      Get numServers from response

      For i = 0 to numServers - 1
        Receive server information
        Split server information into serverData
        Get serverType and coreCount from serverData

        Find the largest server type and ID

      Send "OK" message
      Receive response
      Set firstTime to false

    If jobType is "JOBN"
      Send SCHD message to schedule the job on the server with the largest core count
      Update currServer
      Receive response

  Terminate the simulation gracefully by sending the QUIT message
  Close input stream, output stream, and socket connection
End

```