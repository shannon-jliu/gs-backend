package org.cuair.ground.daos;

import io.ebean.Ebean;
import org.cuair.ground.models.plane.settings.PlaneSettingsModel;
import org.cuair.ground.models.plane.settings.PlaneSettingsModel.PlaneModelStatus;

/**
 * Database Accessor Object that provides an interface for persisting plane settings models.
 *
 * @param <T> PlaneModelSetting type being accessed from the database
 */
public class PlaneSettingsModelDatabaseAccessor<T extends PlaneSettingsModel>
    extends PlaneModelDatabaseAccessor<T> {

  /**
   * Constructor for PlaneSettingsModelDatabaseAccessor
   *
   * @param modelClass Class of model that we are storing in the database
   */
  PlaneSettingsModelDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }

  /**
   * Retrieves the most recent unfailed settings instance of Model T. Returns null if no settings
   * exist in the database
   *
   * @return T the most recent settings model
   */
  public T getRecentUnfailed() {
    return Ebean.find(getModelClass())
        .orderBy()
        .desc("timestamp")
        .where()
        .ne("status", 1) // This corresponds to "failed"
        .setMaxRows(1)
        .findOne();
  }

  /**
   * Gets the most recent setting if failed
   *
   * @return the most recent setting if failed, null otherwise
   */
  public T getRecentFailed() {
    T setting = super.getRecent();
    if (setting != null && setting.getStatus().equals(PlaneModelStatus.failed)) {
      return setting;
    }
    return null;
  }
}
