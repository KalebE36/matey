# CNT4007 BitTorrent-Like P2P System

This repository contains a simplified BitTorrent-style peer-to-peer file sharing system for the CNT4007 course.  
Each peer participates in a distributed swarm, exchanging file pieces until all peers complete the download.

---

## Prerequisites

Before running the project, ensure you have:

- **Java 21** installed and available on your PATH  
- **Maven** installed (`mvn -v` should work)  
- A Unix-like shell environment (macOS/Linux or WSL)

---

## Project Setup

1. **Clone the repository**
   ```
   git clone [<repo-url>](https://github.com/KalebE36/matey)
   cd matey
   ```

2. **Build the project**
   ```
   make install
   ```

This performs a clean Maven build and prepares the project for execution.

---

## Running the P2P System

> Note: We use the `algo.pdf` as the test file. This is approximately 5Mb and we transfer at a rate of 256Kb per piece.
> You can configure all of these, but if you change the file make sure to put it in root and change the `cfg/Common.cfg`
> to match the new file.

### Option 1: Use the launcher script (recommended)

1. Make the script executable:
   ```
   chmod +x ./launch_peers.sh
   ```

2. Start the full swarm:
   ```
   ./launch_peers.sh
   ```

This automatically launches all peer terminals and starts the system.

---

### Option 2: Run peers manually

Open multiple terminal windows and run:

```
make run-peer-0
make run-peer-1
...
make run-peer-5
```

**Important:** Start the lower-numbered peers first.  
**Peer `1001` is currently the seed peer**, so ensure it starts before the others.

---

## Output and Logs

- Logs generated via `launch_peers.sh` are stored under:
  ```
  logs/
  ```

- Each peer stores its downloaded file under:
  ```
  peer_[peerId]/
  ```

Example:
```
peer_1002/
peer_1003/
...
```

---

## Notes

- Ensure all peers use the same configuration files included in the root directory.
- If needed, delete peer directories and logs to reset the environment before re-running.

---

Happy testing!  
This implementation supports the core protocol mechanics required for CNT4007’s networking project.
