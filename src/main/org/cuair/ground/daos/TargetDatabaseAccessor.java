package org.cuair.ground.daos;

import org.cuair.ground.models.plane.target.Target;

/**
 * Database Accessor object for Targets
 *
 * @param <T> subclass of Target
 */
public class TargetDatabaseAccessor<T extends Target> extends ClientCreatableDatabaseAccessor<T> {

  /**
   * Creates a Target database accessor object
   *
   * @param modelClass Class of model stored in the database
   */
  TargetDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }
}
