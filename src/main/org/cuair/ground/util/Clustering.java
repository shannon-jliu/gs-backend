package org.cuair.ground.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.ROI;
import org.cuair.ground.models.geotag.GpsLocation;

public class Clustering {

  /**
   * Finds all neighbors of q that is within the distance of eps including q
   *
   * @param rois all of the ROIs
   * @param q    the ROI to find neighbors of
   * @param eps  the max distance for a neighbor
   * @return a list of ROIs that are in range of q
   */
  private static Set<ROI> rangeQuery(Set<ROI> rois, ROI q, double eps) {
    Set<ROI> neighbors = new HashSet<ROI>();
    for (ROI p : rois) {
      if (p.getGpsLocation().euclideanDistance(q.getGpsLocation()) <= eps) {
        neighbors.add(p);
      }
    }
    return neighbors;
  }

  /**
   * Performs DBSCAN algorithm to cluster ROIs
   *
   * The algorithm determines which ROIs would be considered to be "core ROIs" if there are
   * minPts ROIs within eps from it. Any ROI that is within eps of a core ROI would be considered to
   * be in the same cluster as that core ROI.
   *
   * This is based off of the algorithm at this article: https://en.wikipedia.org/wiki/DBSCAN
   *
   * @param rois   the ROIs to cluster
   * @param eps    the epsilon parameter for neighbors
   * @param minPts the minimum amount of points for a node to be considered a core node
   * @return A map containing all ROIs that have values identifying their cluster number
   */
  private static Map<ROI, Integer> clusterHelper(Set<ROI> rois, double eps, int minPts) {
    Map<ROI, Integer> labels = new HashMap<>();
    int c = 0;
    for (ROI p : rois) {
      if (labels.containsKey(p)) {
        continue;
      }
      Set<ROI> neighbors = rangeQuery(rois, p, eps);
      if (neighbors.size() < minPts) {
        labels.put(p, -1);
        continue;
      }
      c += 1;
      labels.put(p, c);
      Set<ROI> s = new HashSet<>(neighbors);
      s.remove(p);
      for (ROI q : neighbors) {
        if (labels.containsKey(q) && labels.get(q) == -1) {
          labels.put(q, c);
        }
        if (labels.containsKey(q)) {
          continue;
        }
        labels.put(q, c);
        Set<ROI> n = rangeQuery(rois, q, eps);
        if (n.size() >= minPts) {
          s.addAll(n);
        }
      }
    }
    return labels;
  }

  /**
   * Finds the median ROIs within clusters given a list of ROIs and an epsilon
   *
   * @param rois the ROIs to find the medians of
   * @param eps  the epsilon parameter for neighbors
   * @return A list of medianed ROIs
   */
  public static List<ROI> cluster(List<ROI> rois, double eps) {
    Map<ROI, Integer> labels = clusterHelper(new HashSet<>(rois), eps, 1);
    Map<Integer, List<GpsLocation>> clusters = new HashMap<>();

    for (Map.Entry<ROI, Integer> pair : labels.entrySet()) {
      ROI roi = pair.getKey();
      Integer clusterLabel = pair.getValue();
      List<GpsLocation> gpsLocations =
          clusters.containsKey(clusterLabel) ? clusters.get(clusterLabel) : new ArrayList<>();
      gpsLocations.add(roi.getGpsLocation());
      clusters.put(clusterLabel, gpsLocations);
    }

    List<ROI> medianedRois = new ArrayList<>();
    for (Map.Entry<Integer, List<GpsLocation>> pair : clusters.entrySet()) {
      List<GpsLocation> cluster = pair.getValue();
      GpsLocation[] clusterArr = new GpsLocation[cluster.size()];
      clusterArr = cluster.toArray(clusterArr);
      GpsLocation median = GpsLocation.median(clusterArr);
      medianedRois.add(
          new ROI(new ODLCUser(Flags.DEFAULT_USERNAME, "localhost", ODLCUser.UserType.MDLCTAGGER),
              median));
    }
    return medianedRois;
  }

  /**
   * Finds the median ROIs within clusters given a list of ROIs
   *
   * @param rois the ROIs to find the medians of
   * @return A list of medianed ROIs
   */
  public static List<ROI> cluster(List<ROI> rois) {
    return cluster(rois, Flags.CUAIR_CLUSTERING_EPSILON);
  }
}
