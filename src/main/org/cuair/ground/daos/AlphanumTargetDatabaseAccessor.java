package org.cuair.ground.daos;

import io.ebean.Ebean;
import org.cuair.ground.models.plane.target.AlphanumTarget;

/**
 * Database Accessor object for Alphanumeric Targets
 *
 * @param <T> subclass of AlphanumTarget
 */
public class AlphanumTargetDatabaseAccessor<T extends AlphanumTarget>
    extends TargetDatabaseAccessor<T> {

  /**
   * Creates an AlphanumTarget database accessor object
   *
   * @param modelClass class of dao
   */
  AlphanumTargetDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }

  /**
   * Retrieves the sole offaxis target
   *
   * @return Offaxis target
   */
  public T getOffaxisTarget() {
    return Ebean.find(getModelClass()).where().eq("offaxis", true).findOne();
  }
}
