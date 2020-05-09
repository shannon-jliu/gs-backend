package org.cuair.ground.daos;

import io.ebean.Ebean;
import java.util.List;
import java.util.stream.Collectors;
import org.cuair.ground.models.Confidence;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.plane.target.TargetSighting;

/**
 * Database Accessor object for Target Sightings
 *
 * @param <T> subclass of Target Sighting
 */
public class TargetSightingsDatabaseAccessor<T extends TargetSighting>
    extends ClientCreatableDatabaseAccessor<T> {

  /**
   * Creates a Target Sightings database accessor object
   *
   * @param modelClass class that
   */
  TargetSightingsDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }

  /**
   * Retrieves all instances of TargetSightings that don't have Targets. Returns an empty list if no
   * such models exist in the database.
   *
   * @return a list of all instances that don't have a Target
   */
  public List<T> getAllNoTargets() {
    return Ebean.find(getModelClass()).where().isNull("target_id").findList();
  }

  /**
   * Retrieves all instances of TargetSightings that have the same Target.
   *
   * @param id Long id of the Target that all TargetSightings share
   * @return a list of all instances that have the same Target
   */
  public List<T> getAllTargetSightingsForTarget(Long id) {
    return Ebean.find(getModelClass()).where().eq("target_id", id).findList();
  }

  /**
   * Given a target id, we take all target sightings of that target and remove their pointer to that
   * target
   *
   * @param id Long id of target
   */
  public void unassociateAllTargetSightingsForTarget(Long id) {
    List<T> sightings = Ebean.find(getModelClass()).where().eq("target_id", id).findList();
    for (TargetSighting sighting : sightings) {
      sighting.makeAssociatedTargetNull();
      Ebean.update(sighting);
    }
  }

  /**
   * Retrieves all instances of TargetSightings that were created by the same Assignment.
   *
   * @param assignmentId the id of the assignment
   * @return a list of all instances that were created by the assignment
   */
  public List<T> getAllTargetSightingsForAssignment(Long assignmentId) {
    return Ebean.find(getModelClass()).where().eq("assignment_id", assignmentId).findList();
  }

  /**
   * Retrieves all non-null GpsLocations of MDLC target sightings for which {@code mdlcClassConf} is
   * {@code classConf}
   *
   * @param classConf the Confidence of MDLC tags
   * @return a list of the GpsLocations for these sightings
   */
  public List<GpsLocation> getLocationsByMdlcClassConf(Confidence classConf) {
    return Ebean.find(getModelClass())
        .select("geotag")
        .where()
        .or()
        .eq("creator", ODLCUser.UserType.MDLCTAGGER)
        .eq("creator", ODLCUser.UserType.MDLCOPERATOR)
        .endOr()
        .eq("mdlc_class_conf", classConf)
        .isNotNull("geotag")
        .findList()
        .stream()
        .map(ts -> ts.getGeotag().getGpsLocation())
        .filter(gpsL -> gpsL != null)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves target sighting with highest id associated with a specific target
   *
   * @param id target id
   * @return target sighting, null if none exist
   */
  public T getLastSightingForTarget(long id) {
    return Ebean.find(getModelClass())
        .where()
        .eq("target_id", id)
        .order("id DESC")
        .setMaxRows(1)
        .findOne();
  }
}
