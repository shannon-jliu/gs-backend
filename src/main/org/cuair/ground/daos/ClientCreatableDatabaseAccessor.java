package org.cuair.ground.daos;

import io.ebean.Ebean;
import java.util.List;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.ClientType;

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

    /**
     * Retrieves all instances of this ClientCreatable that is associated with the given creator.
     * Returns an empty list if no such models exist in the database.
     *
     * @return List<T> of all instances belonging to {@param creator}
     */
    public List<T> getAllForCreator(ClientType creator) {
        return Ebean.find(getModelClass()).where().eq("creator", creator).findList();
    }
}
