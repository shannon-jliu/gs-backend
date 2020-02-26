// package org.cuair.ground.daos

// import org.cuair.ground.models.CUAirModel
// import org.cuair.ground.models.TimestampModel
// import org.cuair.ground.models.Username
// import org.cuair.ground.models.plane.target.TargetSighting

// /** Factory for creating an managing DAO instances */
// class DAOFactory {
//     enum class ModellessDAOType {
//         USERNAME_DATABASE_ACCESSOR {
//             override fun createInstance(): DatabaseAccessor<*> {
//                 return UsernameDatabaseAccessor()
//             }
//         },
//         AUTH_TOKEN_DATABASE_ACCESSOR {
//             override fun createInstance(): DatabaseAccessor<*> {
//                 return AuthTokenDatabaseAccessor()
//             }
//         };

//         /**
//          * Creates an instance of the database accessor
//          *
//          * @return the DAO instance
//          */
//         abstract fun createInstance() : DatabaseAccessor<*>
//   }

//   /** Enumeration of all database accessor types that are parametrized on a model */
//   enum class ModelDAOType {
//     DATABASE_ACCESSOR {
//       override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
//         return DatabaseAccessor(clazz)
//       }
//     },
//     TIMESTAMP_DATABASE_ACCESSOR {
//         override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
//             return TimestampDatabaseAccessor(clazz.asSubclass(TimestampModel::class.java))
//         }
//     },
//     TARGET_SIGHTINGS_DATABASE_ACCESSOR {
//       override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
//         return TargetSightingsDatabaseAccessor(clazz.asSubclass(TargetSighting::class.java))
//       }
//     };

//     /**
//      * Creates an instance of the database accessor for the specified model class
//      *
//      * @param clazz the model class
//      * @return the DAO instance
//      */
//     abstract fun <M : CUAirModel> createInstance(clazz: Class<M>) : DatabaseAccessor<*>
//   }

//   companion object {

//     /**
//      * Mapping from ModelDAOType to another map of CUAirModel to DatabaseAccessor to keep track of
//      * which DAO instances already exist for DAOs that are parametrized on models
//      * DAOs already exist for DAOs that need a Model to be passed.
//      */
//     private val daoWithModelMap = hashMapOf<ModelDAOType, MutableMap<Class<CUAirModel>, DatabaseAccessor<*>>>()

//     /**
//      * Mapping from ModellessDAOType to DAO to keep track of which DAO instancess already exist for
//      * DAOs that are not parametrized on models
//      */
//     private val daoWithoutModelMap = hashMapOf<ModellessDAOType, DatabaseAccessor<*>>()


//     /**
//      * Gets a DAO instance given a ModellessDAOType
//      *
//      * @param daoType the ModellessDAOType
//      * @return the DAO instance
//      */
//     @JvmStatic
//     fun getDAO(daoType: ModellessDAOType): DatabaseAccessor<*> {
//         synchronized(daoType) {
//             var dao: DatabaseAccessor<*>? = daoWithoutModelMap[daoType]
//             if (dao === null) {
//                 dao = daoType.createInstance()
//                 daoWithoutModelMap.put(daoType, dao)
//             }
//             return dao
//         }
//     }

//     /**
//      * Gets a DAO instance given a ModelDAOType and the model class
//      *
//      * @param daoType the ModelDAOType
//      * @param modelClass the model class
//      * @return the DAO instance
//      */
//     @JvmStatic
//     fun <M : CUAirModel> getDAO(daoType: ModelDAOType, modelClass: Class<M>): DatabaseAccessor<M> {
//         synchronized(daoType) {
//             var daoMap = daoWithModelMap[daoType]
//             if (daoMap === null) {
//                 daoMap = hashMapOf<Class<CUAirModel>, DatabaseAccessor<*>>()
//                 daoWithModelMap.put(daoType, daoMap)
//             }

//             @Suppress("UNCHECKED_CAST")
//             modelClass as Class<CUAirModel>

//             var dao = daoMap[modelClass]
//             if (dao === null) {
//                 dao = daoType.createInstance<M>(modelClass)
//                 daoMap.put(modelClass, dao)
//             }

//             @Suppress("UNCHECKED_CAST")
//             return dao as DatabaseAccessor<M>
//         }
//     }

//     /**
//      * Gets a DAO instance given just a ModelDAOType and statically determines the model class by
//      * leveraging reified generics. Note that this is not callable from Java as reified generics are
//      * a Kotlin specific language feature (hence the lack of the @JvmStatic annotation).
//      *
//      * @param daoType the ModelDAOType
//      * @return the DAO instance
//      */
//     inline fun <reified M : CUAirModel> getDAO(daoType: ModelDAOType): DatabaseAccessor<M> {
//         return getDAO(daoType, M::class.java)
//     }
//   }
// }



package org.cuair.ground.daos

import org.cuair.ground.models.ClientCreatable
import org.cuair.ground.models.CUAirModel
import org.cuair.ground.models.PlaneModel
import org.cuair.ground.models.PlaneSettingsModel
import org.cuair.ground.models.TimestampModel
import org.cuair.ground.models.plane.target.AlphanumTarget
import org.cuair.ground.models.plane.target.AlphanumTargetSighting
import org.cuair.ground.models.plane.target.TargetSighting

/** Factory for creating an managing DAO instances */
class DAOFactory {

  /** Enumeration of all database accessor types that are not parametrized on a model */
  enum class ModellessDAOType {
    ASSIGNMENT_DATABASE_ACCESSOR {
        override fun createInstance(): DatabaseAccessor<*> {
            return AssignmentDatabaseAccessor()
        }
    },
    AUTH_TOKEN_DATABASE_ACCESSOR {
        override fun createInstance(): DatabaseAccessor<*> {
            return AuthTokenDatabaseAccessor()
        }
    },
    MGT_IMAGE_DATABASE_ACCESSOR {
        override fun createInstance(): DatabaseAccessor<*> {
            return MGTImageDatabaseAccessor()
        }
    },
    USERNAME_DATABASE_ACCESSOR {
        override fun createInstance(): DatabaseAccessor<*> {
            return UsernameDatabaseAccessor()
        }
    };

    /**
     * Creates an instance of the database accessor
     *
     * @return the DAO instance
     */
    abstract fun createInstance() : DatabaseAccessor<*>
  }

  /** Enumeration of all database accessor types that are parametrized on a model */
  enum class ModelDAOType {
    DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return DatabaseAccessor(clazz)
      }
    },
    ALPHANUM_TARGET_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return AlphanumTargetDatabaseAccessor(clazz.asSubclass(AlphanumTarget::class.java))
      }
    },
    ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return AlphanumTargetSightingsDatabaseAccessor(clazz.asSubclass(AlphanumTargetSighting::class.java))
      }
    },
    CLIENT_CREATABLE_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return ClientCreatableDatabaseAccessor(clazz.asSubclass(ClientCreatable::class.java))
      }
    },
    PLANE_MODEL_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return PlaneModelDatabaseAccessor(clazz.asSubclass(PlaneModel::class.java))
      }
    },
    PLANE_SETTINGS_MODEL_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return PlaneSettingsModelDatabaseAccessor(clazz.asSubclass(PlaneSettingsModel::class.java))
      }
    },
    TARGET_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return TargetDatabaseAccessor(clazz.asSubclass(org.cuair.ground.models.plane.target.Target::class.java))
      }
    },
    TARGET_SIGHTINGS_DATABASE_ACCESSOR {
      override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
        return TargetSightingsDatabaseAccessor(clazz.asSubclass(TargetSighting::class.java))
      }
    },
    TIMESTAMP_DATABASE_ACCESSOR {
        override fun <M : CUAirModel> createInstance(clazz: Class<M>): DatabaseAccessor<*> {
            return TimestampDatabaseAccessor(clazz.asSubclass(TimestampModel::class.java))
        }
    };

    /**
     * Creates an instance of the database accessor for the specified model class
     *
     * @param clazz the model class
     * @return the DAO instance
     */
    abstract fun <M : CUAirModel> createInstance(clazz: Class<M>) : DatabaseAccessor<*>
  }

  companion object {

    /**
     * Mapping from ModelDAOType to another map of CUAirModel to DatabaseAccessor to keep track of
     * which DAO instances already exist for DAOs that are parametrized on models
     * DAOs already exist for DAOs that need a Model to be passed.
     */
    private val daoWithModelMap = hashMapOf<ModelDAOType, MutableMap<Class<CUAirModel>, DatabaseAccessor<*>>>()

    /**
     * Mapping from ModellessDAOType to DAO to keep track of which DAO instancess already exist for
     * DAOs that are not parametrized on models
     */
    private val daoWithoutModelMap = hashMapOf<ModellessDAOType, DatabaseAccessor<*>>()

    /**
     * Gets a DAO instance given a ModellessDAOType
     *
     * @param daoType the ModellessDAOType
     * @return the DAO instance
     */
    @JvmStatic
    fun getDAO(daoType: ModellessDAOType): DatabaseAccessor<*> {
        synchronized(daoType) {
            var dao: DatabaseAccessor<*>? = daoWithoutModelMap[daoType]
            if (dao === null) {
                dao = daoType.createInstance()
                daoWithoutModelMap.put(daoType, dao)
            }
            return dao
        }
    }

    /**
     * Gets a DAO instance given a ModelDAOType and the model class
     *
     * @param daoType the ModelDAOType
     * @param modelClass the model class
     * @return the DAO instance
     */
    @JvmStatic
    fun <M : CUAirModel> getDAO(daoType: ModelDAOType, modelClass: Class<M>): DatabaseAccessor<M> {
        synchronized(daoType) {
            var daoMap = daoWithModelMap[daoType]
            if (daoMap === null) {
                daoMap = hashMapOf<Class<CUAirModel>, DatabaseAccessor<*>>()
                daoWithModelMap.put(daoType, daoMap)
            }

            @Suppress("UNCHECKED_CAST")
            modelClass as Class<CUAirModel>

            var dao = daoMap[modelClass]
            if (dao === null) {
                dao = daoType.createInstance<M>(modelClass)
                daoMap.put(modelClass, dao)
            }

            @Suppress("UNCHECKED_CAST")
            return dao as DatabaseAccessor<M>
        }
    }

    /**
     * Gets a DAO instance given just a ModelDAOType and statically determines the model class by
     * leveraging reified generics. Note that this is not callable from Java as reified generics are
     * a Kotlin specific language feature (hence the lack of the @JvmStatic annotation).
     *
     * @param daoType the ModelDAOType
     * @return the DAO instance
     */
    inline fun <reified M : CUAirModel> getDAO(daoType: ModelDAOType): DatabaseAccessor<M> {
        return getDAO(daoType, M::class.java)
    }
  }
}
