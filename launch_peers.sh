#!/bin/bash

# --- Configuration ---
START_PEER=1001
END_PEER=1003
MAVEN_COMMAND="mvn exec:java -Dexec.args="
LOG_DIR="./logs"

# --- Setup ---
echo "Starting peer execution..."
mkdir -p "$LOG_DIR"

# --- Launch Peers ---
for ((id = START_PEER; id <= END_PEER; id++)); do
    echo "Launching peer $id. Output redirected to $LOG_DIR/$id.log"
    
    # Run the command in the background, redirecting stdout/stderr to a log file
    # The command is: mvn exec:java -Dexec.args="<id>" > logs/<id>.log 2>&1 &
    ($MAVEN_COMMAND$id > "$LOG_DIR/$id.log" 2>&1) &
    
    # Store the PID for later use (optional)
    PIDS[$id]=$!
    
    # Little pause to prevent weird race conditions, if needed (optional)
    sleep 0.1
done

echo "All peers launched! PIDs: ${PIDS[*]}"
echo "---"

# --- View Logs Concurrently ---
echo "Now tailing the logs concurrently (Ctrl+C to stop viewing)..."
echo "To stop the peers, you may need to use 'kill' on the PIDs listed above."
# Use 'tail -f' on all log files at once to view the output streaming in real-time.
tail -f "$LOG_DIR"/10*.log
