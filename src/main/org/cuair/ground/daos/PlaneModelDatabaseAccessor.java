package org.cuair.ground.daos;

import org.cuair.ground.models.PlaneModel;

/**
 * Database Accessor Object that provides an interface for persisting plane models.
 *
 * @param <T> PlaneModel type being accessed from the database
 */
public class PlaneModelDatabaseAccessor<T extends PlaneModel>
    extends TimestampDatabaseAccessor<T> {

    /**
     * Constructor for PlaneModelDatabaseAccessor
     *
     * @param modelClass Class of model that we are storing in the database
     */
    PlaneModelDatabaseAccessor(Class<T> modelClass) {
        super(modelClass);
    }
}
