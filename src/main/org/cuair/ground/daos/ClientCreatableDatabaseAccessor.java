package org.cuair.ground.daos;

import org.cuair.ground.models.ClientCreatable;

/**
 * Database Accessor object for Client Creatable
 *
 * @param <T> subclass of ClientCreatable
 */
public class ClientCreatableDatabaseAccessor<T extends ClientCreatable>
    extends DatabaseAccessor<T> {

  /**
   * Creates a ClientCreatable database accessor object
   *
   * @param modelClass class of dao
   */
  ClientCreatableDatabaseAccessor(Class<T> modelClass) {
    super(modelClass);
  }
}
