# DS-Client Benchmarking Analysis Tool

This repository contains a the DS-Client Project aimed at testing and analyzing the performance of a distributed systems client (DS-Client) in various simulated environments. The project is structured into multiple stages, with each stage focusing on different aspects of benchmarking and analysis.

## Stage 1: Basic Client Testing

The initial stage involves testing the DS-Client using a provided script (`S1Tests-wk6.sh`) within the `stage01` directory. This script facilitates the execution of the DS-Client against a set of predefined configurations to assess its behavior and performance. The process is outlined below:

1. Compile the DS-Client Java program:
   ```
   javac dsclient.java && ./S1Tests-wk6.sh dsclient.class -n | tee complete_log.txt
   ```

2. Additional testing involves using a demo script (`demoS1.sh`) to further evaluate the client's performance under different server simulations. Before running this script, ensure that the demo environment is correctly set up by extracting `demoS1.tar` in the same directory as the DS-Client and DS-Server executables.

### Pseudo-Code Overview

The core logic of the DS-Client's operation during testing includes:

- Establishing a connection to the server-side simulator.
- Initializing communication by sending a HELLO message and handling authentication with an AUTH command.
- Entering a loop to handle job dispatching, where the client requests jobs using the READY message and schedules them based on server responses.
- The client focuses on scheduling jobs efficiently, prioritizing server types with the highest core counts for job allocations.
- The simulation concludes with the client gracefully terminating the connection after all jobs have been handled.

## Stage 2: Advanced Benchmarking and Analysis

In Stage 2, the focus shifts towards a more comprehensive benchmarking and analysis approach, utilizing a Python script `s2_test.py` for testing the DS-Client's performance against a variety of server configurations. This stage aims to collect detailed performance metrics and compare the client's efficiency against baseline algorithms under different conditions.

The script requires the presence of the DS-Server, DS-Client, and a custom client executable in the directory. It supports the specification of a directory containing configuration files for the tests, with `S2TestConfigs` being the default directory used if none is specified.

### Running the Script

1. Ensure all necessary executables and the Python script are in the same directory.
2. Execute the Python script with the following command format:
   ```
   python3 ./s2_test.py "java MyClient" -n -c S2TestConfigs
   ```

### Output and Results

Upon completion, the script generates three tables representing different performance metrics for each configuration file tested. It compares the results of baseline algorithms to those of the custom DS-Client, highlighting areas where the custom client excels or needs improvement. The color coding in the final results indicates the client's performance relative to the baseline algorithms, with green indicating superior performance, yellow indicating mixed results, and red showing areas where the client underperforms.
