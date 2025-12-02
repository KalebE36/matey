package edu.ufl.cnt4007.peer;

import edu.ufl.cnt4007.net.ConnectionHandler;
import edu.ufl.cnt4007.protocol.Message;
import edu.ufl.cnt4007.protocol.MessageType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChokingManager {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final PeerState peerState;
  private volatile Integer currentOptimisticallyUnchokedPeerId = null;

  public ChokingManager(PeerState peerState) {
    this.peerState = peerState;

    scheduleUnchokingTask();
    scheduleOptimisticUnchokingTask();
  }

  private void scheduleOptimisticUnchokingTask() {
    // Use the configured interval for optimistic unchoking
    int interval = peerState.getConfigLoader().getCommonConfig().getOptimisticUnchokingInterval();

    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            System.out.println("[DEBUG] Running optimistic unchoking rotation...");

            // 1. Get current preferred neighbors to identify candidates
            Set<Integer> preferredNeighbors = selectPreferredNeighbors();
            Set<Integer> interestedPeers = peerState.getInterestedPeers();

            // --- Step 2: Handle Re-Choke of Previous Peer ---
            Integer previousOptimisticPeerId = currentOptimisticallyUnchokedPeerId;

            if (previousOptimisticPeerId != null) {
              // Check if the previous optimistic peer is still choked by us (i.e., not a
              // preferred neighbor)
              if (!preferredNeighbors.contains(previousOptimisticPeerId)) {
                ConnectionHandler previousCon =
                    peerState.getConnectionHandler(previousOptimisticPeerId);
                if (previousCon != null) {
                  // Send CHOKE to the rotating-out peer
                  peerState.setPeerChoked(previousOptimisticPeerId, true);
                  previousCon.send(new Message(MessageType.CHOKE, null));
                  System.out.println(
                      "[INFO] Re-choked previous optimistic peer: " + previousOptimisticPeerId);
                } else {
                  System.err.println(
                      "[ERROR] No connection handler found for previous optimistic unchoke peer "
                          + previousOptimisticPeerId);
                }
              }
              // Crucial: Clear the state, as the previous peer is either re-choked or now a
              // preferred neighbor
              // and should no longer be tracked as the *optimistic* one.
              currentOptimisticallyUnchokedPeerId = null;
            }

            // --- Step 3: Select New Optimistic Peer ---

            // Candidates are interested peers NOT in the preferred list
            Set<Integer> candidates = new HashSet<>(interestedPeers);
            candidates.removeAll(preferredNeighbors);

            if (candidates.isEmpty()) {
              System.out.println(
                  "[OPTIMISTIC] No choked and interested peers available for optimistic unchoke.");
              return;
            }

            // Select one peer randomly
            List<Integer> candidateList = new ArrayList<>(candidates);
            Integer newOptimisticNeighbor =
                candidateList.get((int) (Math.random() * candidateList.size()));

            // --- Step 4: Unchoke the New Peer and Update State ---

            ConnectionHandler con = peerState.getConnectionHandler(newOptimisticNeighbor);
            if (con != null) {
              // Update the state tracker first
              currentOptimisticallyUnchokedPeerId = newOptimisticNeighbor;

              // Send UNCHOKE
              peerState.setPeerChoked(newOptimisticNeighbor, false);
              con.send(new Message(MessageType.UNCHOKE, null));
              System.out.println("[INFO] Optimistically unchoked peer: " + newOptimisticNeighbor);
            } else {
              System.err.println(
                  "[ERROR] No connection handler found for optimistic unchoke peer "
                      + newOptimisticNeighbor);
              // If connection is lost, the state remains null (or is set to null in step 2)
              currentOptimisticallyUnchokedPeerId = null;
            }

          } catch (Exception e) {
            System.err.println("[ERROR] Exception in optimistic unchoking task: " + e);
            e.printStackTrace();
          }
        },
        0,
        interval,
        TimeUnit.SECONDS);
  }

  private void scheduleUnchokingTask() {
    int interval = peerState.getConfigLoader().getCommonConfig().getUnchokingInterval();

    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            // Calculate the download rates before selecting preferred neighbors
            // This also resets the bytes downloaded in the interval to prepare for the next
            // interval
            peerState.calculateDownloadRates();

            System.out.println("[DEBUG] Running choking algorithm...");

            // Select preferred neighbors safely
            Set<Integer> preferredNeighbors = selectPreferredNeighbors();
            System.out.println(
                preferredNeighbors.isEmpty()
                    ? "[DEBUG] No preferred neighbors selected."
                    : "[DEBUG] Preferred neighbors: " + preferredNeighbors);

            // Print interested peers
            Set<Integer> interestedPeers = peerState.getInterestedPeers();
            System.out.println(
                interestedPeers.isEmpty()
                    ? "[DEBUG] No interested peers."
                    : "[DEBUG] Interested peers: " + interestedPeers);

            // Iterate over interested peers
            for (Integer peerId : interestedPeers) {
              ConnectionHandler handler = peerState.getConnectionHandler(peerId);
              if (handler == null) {
                System.err.println("[ERROR] No connection handler found for peer " + peerId);
                continue;
              }

              boolean shouldBeChoked = !preferredNeighbors.contains(peerId);
              boolean isCurrentlyChoked = peerState.isPeerChoked(peerId);

              try {
                if (shouldBeChoked && !isCurrentlyChoked) {
                  peerState.setPeerChoked(peerId, true);
                  handler.send(new Message(MessageType.CHOKE, null));
                  System.out.println("[INFO] Choked peer " + peerId);
                } else if (!shouldBeChoked && isCurrentlyChoked) {
                  peerState.setPeerChoked(peerId, false);
                  handler.send(new Message(MessageType.UNCHOKE, null));
                  System.out.println("[INFO] Unchoked peer " + peerId);
                }
              } catch (Exception e) {
                System.err.println(
                    "[ERROR] Failed to send CHOKE/UNCHOKE to peer " + peerId + ": " + e);
              }
            }
          } catch (Exception e) {
            // Catch anything that escapes and prevent scheduler from dying
            System.err.println("[ERROR] Exception in choking task: " + e);
            e.printStackTrace();
          }
        },
        0,
        interval,
        TimeUnit.SECONDS);
  }

  private Set<Integer> selectPreferredNeighbors() {
    // Sort by download rate (default 0.0)
    // Return top N preferred neighbors (based on Config)

    List<Integer> candidates = new ArrayList<>(peerState.getInterestedPeers());
    Map<Integer, Double> downloadRates = peerState.getDownloadRates();
    candidates.sort(
        (a, b) ->
            Double.compare(downloadRates.getOrDefault(b, 0.0), downloadRates.getOrDefault(a, 0.0)));

    Set<Integer> preferred =
        new HashSet<>(
            candidates.subList(
                0,
                Math.min(
                    peerState.getConfigLoader().getCommonConfig().getNumberOfPreferredNeighbors(),
                    candidates.size())));

    return preferred;
  }
}
