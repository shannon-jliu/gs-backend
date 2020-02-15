package org.cuair.ground.daos;

import io.ebean.Ebean;
import java.util.List;
import java.util.stream.Collectors;
import org.cuair.ground.models.CUAirModel;

/**
 * Database Accessor class that provides an interface for persisting models into a database.
 *
 * @param <T> Model model type being accessed from the database
 */
public class DatabaseAccessor<T extends CUAirModel> {

    private Class<T> modelClass;

    /**
     * Constructor that creates the DatabaseAccessor for {@code modelClass}
     *
     * @param modelClass Class of model that we are storing in the database
     */
    DatabaseAccessor(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * Retrieves the class of the model that this Database Accessor object is operating on
     *
     * @return Class<T> the class of the model
     */
    public Class<T> getModelClass() {
        return this.modelClass;
    }

    /**
     * Retrieves all instances of T models from the database. Returns an empty list if no such models
     * exist in the database.
     *
     * @return List<T> of all instances
     */
    public List<T> getAll() {
        return Ebean.find(modelClass).findList();
    }

    /**
     * Retrieves the instance of Model T with id {@code id}. If a model with the corresponding id
     * doesn't exist, then it returns null.
     *
     * @param id of the model
     * @return instance of T
     */
    public T get(Long id) {
        if (id == null) {
            return null;
        }
        return Ebean.find(modelClass).where().eq("id", id).findOne();
    }

    /**
     * Retrieves the list of ids corresponding to each instance of the model in the database
     *
     * @return List<Long> of all the ids
     */
    public List<Long> getAllIds() {
        return Ebean.find(modelClass)
            .findIds()
            .stream()
            .map(o -> (Long) o)
            .collect(Collectors.toList());
    }

    /**
     * Stores {@code object} in the database and returns true. Returns false if the object has already
     * been entered in the database.
     *
     * @param object to be stored in the database
     * @return boolean whether the object was successfully entered into the database
     */
    public boolean create(T object) {
        if (get(object.getId()) != null) {
            return false;
        }
        Ebean.save(object);
        return true;
    }

    /**
     * Updates {@code object} in the database
     *
     * @param object to be updated in the database
     * @return boolean whether the object was successfully updated in the database
     */
    public boolean update(T object) {
        if (get(object.getId()) == null) {
            return false;
        }
        Ebean.update(object);
        return true;
    }

    /**
     * Deletes object with {@code id} from the database
     *
     * @param id id of the object to be deleted
     * @return boolean whether the object was successfully deleted from the database
     */
    public boolean delete(Long id) {
        if (get(id) == null) {
            return false;
        }
        Ebean.delete(modelClass, id);
        return true;
    }
}
