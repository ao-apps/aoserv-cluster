/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2019, 2020, 2021, 2022, 2024, 2025  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-cluster.
 *
 * aoserv-cluster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-cluster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-cluster.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoindustries.aoserv.cluster.optimize;

import com.aoapps.lang.io.IoUtils;
import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.DomU;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.analyze.AnalyzedClusterConfiguration;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Optimizes the cluster using a best-first heuristic search.
 *
 * <p>The current optimization only tries to eliminate all problems.  A future version
 * of this should factory in the hardware and monthly costs to maximize the amount
 * of revenue we can make per cost.</p>
 *
 * <p>The search is performed by exploring two possible moves:</p>
 *
 * <ol>
 *   <li>Swap DomU between primary and secondary Dom0 (live-migrate if same architecture or shutdown/create if different)</li>
 *   <li>Move the secondary storage to a different Dom0</li>
 * </ol>
 *
 * <p>The following transitions are possible, but can also occur less directly as
 * a result of the previous two transitions.  These are not yet implemented.</p>
 *
 * <ol>
 *   <li>pvmove primary storage to different physical volumes</li>
 *   <li>pvmove secondary storage to different physical volumes</li>
 * </ol>
 *
 * <p>To reduce the number of non-live-migrate swaps, this search could try to move
 * between same architectures in preference to different architectures.</p>
 *
 * <p>TODO: To manage heap consumption, allow two optional parameters:
 *           maxPathLen
 *           something to make it a "Beam Search" (http://pages.cs.wisc.edu/~dyer/cs540/notes/search2.html)</p>
 *
 * @author  AO Industries, Inc.
 */
public class ClusterOptimizer {

  private static final boolean USE_SKIP_SAME_HEURISTIC_HACK = false;

  private final ClusterConfiguration clusterConfiguration;
  private final HeuristicFunction heuristicFunction;
  private final boolean allowPathThroughCritical;
  private final boolean randomizeChildren;

  /**
   * Creates a new cluster optimizer for the given configuration and heuristic.
   */
  public ClusterOptimizer(ClusterConfiguration clusterConfiguration, HeuristicFunction heuristicFunction, boolean allowPathThroughCritical, boolean randomizeChildren) {
    this.clusterConfiguration = clusterConfiguration;
    this.heuristicFunction = heuristicFunction;
    this.allowPathThroughCritical = allowPathThroughCritical;
    this.randomizeChildren = randomizeChildren;
  }

  /**
   * Optimizes the cluster and returns the path to the first optimal configuration found or <code>null</code> if no
   * optimal configuration was found.
   */
  public ListElement getOptimizedClusterConfiguration() {
    return getOptimizedClusterConfiguration(null);
  }

  /**
   * Optimizes the cluster and returns the best path (possibly limited by an OptimizedResultHandler)
   * or <code>null</code> if no optimal configuration was found.
   *
   * <p>Best-first search implementation.</p>
   *
   * <p>TODO: Since we are limited by heap space more than CPU speed, perhaps we should look for optimal
   *       solutions before adding onto the open list?  This means we could at least look one more move ahead.</p>
   *
   * <p>Could allow heuristic to return Double.POSITIVE_INFINITY to indicate no solution from that state:
   *     (see http://pages.cs.wisc.edu/~dyer/cs540/notes/search2.html)</p>
   *
   * <p>TODO: Add in transition cost because the best path depends not on the number of steps, but the total time of the steps.
   * TODO: This could be a real-world estimate on how many seconds the operation would take.</p>
   *
   * <p>TODO: If something MUST take a path through a CRITICAL state, try to use path with shortest time in CRITICAL
   * TODO: based on time estimates above.</p>
   *
   * @param  handler  if null, returns the first path found, not necessarily the shortest
   */
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public ListElement getOptimizedClusterConfiguration(OptimizedClusterConfigurationHandler handler) {

    // Reused inside loop below
    List<ClusterConfiguration> children = new ArrayList<>();
    List<Transition> childTransitions = new ArrayList<>();

    // Return value is stored here upon success or remains null on failure
    ListElement shortestPath = null;

    // Initialize the open list
    PriorityQueue<ListElement> openQueue = new PriorityQueue<>();
    Map<ClusterConfiguration, ListElement> openMap = new HashMap<>();
    {
      ListElement openListElement = new ListElement(
          null,
          null,
          clusterConfiguration,
          heuristicFunction.getHeuristic(clusterConfiguration, 0)
      );
      openQueue.add(openListElement);
      openMap.put(clusterConfiguration, openListElement);
    }

    // Initialize the closed list
    Map<ClusterConfiguration, ListElement> closedMap = new HashMap<>();

    long loopCounter = 0;
    long existingOpenCount = 0;
    long existingClosedCount = 0;
    long openQueueRemoveCount = 0;
    long skipCriticalPathCount = 0;
    long lastDisplayTime = System.currentTimeMillis();
    double lastHeurisic = Double.NaN;
    while (!openQueue.isEmpty()) {
      loopCounter++;
      assert openQueue.size() == openMap.size() : "openQueue and openMap have different sizes";
      ListElement current = openQueue.remove();
      openMap.remove(current.clusterConfiguration);
      assert shortestPath == null || current.pathLen < shortestPath.pathLen : "Should only explore paths shorter than shortestPath";
      long currentTime = System.currentTimeMillis();
      long timeSince = currentTime - lastDisplayTime;
      if (timeSince < 0 || timeSince >= 60000) {
        System.out.println(
            "        open:" + openMap.size()
                + " closed:" + closedMap.size()
                + " transitions:" + current.pathLen
                + " heuristic:" + current.heuristic
                + " existingOpen:" + existingOpenCount
                + " existingClosed:" + existingClosedCount
                + " openQueueRemove:" + openQueueRemoveCount
                + " skipCriticalPath:" + skipCriticalPathCount
        );
        lastDisplayTime = currentTime;
      }
      // Is this the goal?
      AnalyzedClusterConfiguration analyzed = new AnalyzedClusterConfiguration(current.clusterConfiguration);
      if (analyzed.isOptimal()) {
        shortestPath = current;

        // Give handler a chance to cancel before trimming
        if (
            handler == null
                || !handler.handleOptimizedClusterConfiguration(current, loopCounter)
        ) {
          break;
        }

        // Trim anything out of open/closed that has transitions.length >= this path
        //System.out.println(
        //    "        Before trim: openQueue: "+openQueue.size()
        //    + " openMap: "+openMap.size()
        //    + " closedMap:"+closedMap.size()
        //);
        // openQueue and openMap
        int shortestPathLen = shortestPath.pathLen;
        Iterator<Map.Entry<ClusterConfiguration, ListElement>> openIter = openMap.entrySet().iterator();
        while (openIter.hasNext()) {
          Map.Entry<ClusterConfiguration, ListElement> entry = openIter.next();
          ListElement listElement = entry.getValue();
          int lePathLen = listElement.pathLen;
          if (lePathLen >= shortestPathLen) {
            openIter.remove();
            if (!openQueue.remove(listElement)) {
              throw new AssertionError("listElement not found in openQueue");
            }
          }
        }
        // closedMap
        Iterator<Map.Entry<ClusterConfiguration, ListElement>> closedIter = closedMap.entrySet().iterator();
        while (closedIter.hasNext()) {
          Map.Entry<ClusterConfiguration, ListElement> entry = closedIter.next();
          if (entry.getValue().pathLen >= shortestPathLen) {
            closedIter.remove();
          }
        }
        //System.out.println(
        //    "        After trim: openQueue: "+openQueue.size()
        //    + " openMap: "+openMap.size()
        //    + " closedMap:"+closedMap.size()
        //);
      } else {
        if (!USE_SKIP_SAME_HEURISTIC_HACK || lastHeurisic != current.heuristic) {
          if (USE_SKIP_SAME_HEURISTIC_HACK) {
            lastHeurisic = current.heuristic;
          }
          // generate children of X if depth limit not reached
          // max depth is determined by any path already found
          if (
              // + 1 to match size of newTransitions below
              shortestPath == null || (current.pathLen + 1) < shortestPath.pathLen
          ) {
            generateChildren(current.clusterConfiguration, children, childTransitions, randomizeChildren);
            //System.out.println("        children: "+children.size());
            boolean endsCritical = allowPathThroughCritical ? true : analyzed.hasCritical();
            // for each child of X do
            for (int i = 0, size = children.size(); i < size; i++) {
              ClusterConfiguration child = children.get(i);
              // Don't keep any path that has a transition from not having any critical to have at least one critical
              boolean childHasCritical = allowPathThroughCritical ? false : new AnalyzedClusterConfiguration(child).hasCritical();
              if (endsCritical || !childHasCritical) {
                ListElement existingOpen = openMap.get(child);
                if (existingOpen != null) {
                  existingOpenCount++;
                  // if the child was reached by a shorter path
                  if (
                      // + 1 to match size of newTransitions below
                      (current.pathLen + 1) < existingOpen.pathLen
                  ) {
                    // then give the state of open the shorter path

                    // removing and adding back to open because a short path affects the heuristic and therefore
                    // the position within the queue.
                    openQueue.remove(existingOpen); // This runs in O(n)
                    openQueueRemoveCount++;

                    ListElement openListElement = new ListElement(
                        current,
                        childTransitions.get(i),
                        child,
                        heuristicFunction.getHeuristic(child, current.pathLen + 1)
                    );
                    openQueue.add(openListElement);
                    openMap.put(child, openListElement);
                  }
                } else {
                  ListElement existingClosed = closedMap.get(child);
                  if (existingClosed != null) {
                    existingClosedCount++;
                    // If the child was reached by a shorter path then
                    if ((current.pathLen + 1) < existingClosed.pathLen) {
                      // remove the state from closed
                      closedMap.remove(child);
                      // add the child to open
                      ListElement openListElement = new ListElement(
                          current,
                          childTransitions.get(i),
                          child,
                          heuristicFunction.getHeuristic(child, current.pathLen + 1)
                      );
                      openQueue.add(openListElement);
                      openMap.put(child, openListElement);
                    }
                  } else {
                    // the child is not on open or closed
                    // add the child to open
                    ListElement openListElement = new ListElement(
                        current,
                        childTransitions.get(i),
                        child,
                        heuristicFunction.getHeuristic(child, current.pathLen + 1)
                    );
                    openQueue.add(openListElement);
                    openMap.put(child, openListElement);
                  }
                }
              } else {
                skipCriticalPathCount++;
              }
            }
          }
        }
      }
      // put X on closed
      closedMap.put(current.clusterConfiguration, current);
    }
    return shortestPath;
  }

  /**
   * A fast pseudo-random number generator for non-cryptographic purposes.
   */
  private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(Long.BYTES)));

  private void generateChildren(ClusterConfiguration clusterConfiguration, List<ClusterConfiguration> children, List<Transition> childTransitions, boolean randomizeChildren) {
    children.clear();
    childTransitions.clear();

    // Try swapping each of the primary/secondary pairs
    for (DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
      DomU domU = domUConfiguration.getDomU();
      if (!domU.isSecondaryDom0Locked()) {
        Dom0 primaryDom0 = domUConfiguration.getPrimaryDom0();
        Dom0 secondaryDom0 = domUConfiguration.getSecondaryDom0();
        if (
            !domU.isPrimaryDom0Locked()
                // TODO: Don't hard-code these
                && !"gw1.fc.aoindustries.com".equals(secondaryDom0.getHostname())
                && !"gw2.fc.aoindustries.com".equals(secondaryDom0.getHostname())
        ) {
          // Can't swap if either primary or secondary is locked
          ClusterConfiguration swappedClusterConfiguration = clusterConfiguration.liveMigrate(domU);
          Transition transition = new MigrateTransition(domU, primaryDom0, secondaryDom0);
          int size = children.size();
          if (randomizeChildren && size != 0) {
            // It may be faster to build the list and randomize at the end instead of incuring the overhead of inserting into an ArrayList
            // However, since the two lists children and childrenTransitions need to be kept in sync, a simple call to
            // Collections.shuffle will not work
            int index = fastRandom.nextInt(size + 1);
            children.add(index, swappedClusterConfiguration);
            childTransitions.add(index, transition);
          } else {
            children.add(swappedClusterConfiguration);
            childTransitions.add(transition);
          }
        }

        for (Map.Entry<String, Dom0> entry : clusterConfiguration.getCluster().getDom0s().entrySet()) {
          String dom0Hostname = entry.getKey();
          Dom0 dom0 = entry.getValue();
          // Can't move to current primary or secondary
          if (
              !dom0.equals(primaryDom0)
                  && !dom0.equals(secondaryDom0)
                  // TODO: Don't hard-code these
                  && !"gw1.fc.aoindustries.com".equals(dom0Hostname)
                  && !"gw2.fc.aoindustries.com".equals(dom0Hostname)
          ) {
            for (ClusterConfiguration movedClusterConfiguration : clusterConfiguration.moveSecondary(domU, dom0)) {
              Transition transition = new MoveSecondaryTransition(domU, secondaryDom0, dom0);
              int size = children.size();
              if (randomizeChildren && size != 0) {
                int index = fastRandom.nextInt(size + 1);
                children.add(index, movedClusterConfiguration);
                childTransitions.add(index, transition);
              } else {
                children.add(movedClusterConfiguration);
                childTransitions.add(transition);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Gets the starting clusterConfiguration.
   */
  public ClusterConfiguration getClusterConfiguration() {
    return clusterConfiguration;
  }

  /**
   * Gets the heuristic function used during the search.
   */
  public HeuristicFunction getHeuristicFunction() {
    return heuristicFunction;
  }

  /**
   * When true, a transition from non-critical to critical will be allowed.
   * Otherwise, any path with this transition will be ignored and not expanded.
   */
  public boolean allowsPathThroughCritical() {
    return allowPathThroughCritical;
  }

  /**
   * When true, the search will randomize the list of children as it expands
   * each node.  The impact of this is not yet tested, but it is expected to
   * provide different alternatives when several different paths yield exactly
   * the same heuristic value because only the first of a provided path length
   * is kept as the sortest path.
   */
  public boolean getRandomizeChildren() {
    return randomizeChildren;
  }
}
