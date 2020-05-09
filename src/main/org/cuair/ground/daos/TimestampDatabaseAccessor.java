package org.cuair.ground.daos;

import io.ebean.DB;
import org.cuair.ground.models.TimestampModel;

/**
 * Database accessor object for CUAirModels with timestamp fields
 *
 * @param <T> Model being accessed from the database
 */
public class TimestampDatabaseAccessor<T extends TimestampModel> extends DatabaseAccessor<T> {

  /**
   * Creates a TimestampDatabaseAccessor
   *
   * @param modelClass
   */
  TimestampDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }

  /**
   * Retrieves the most recent settings instance of Model T. Returns null if no settings exist in
   * the database
   *
   * @return the most recent settings model
   */
  public T getRecent() {
    return DB.find(getModelClass()).orderBy().desc("timestamp").setMaxRows(1).findOne();
  }
}
