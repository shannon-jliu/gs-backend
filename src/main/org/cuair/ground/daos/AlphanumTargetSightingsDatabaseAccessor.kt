package org.cuair.ground.daos

import org.cuair.ground.models.plane.target.AlphanumTargetSighting

/**
 * Database Accessor object for Alphanumeric Target Sightings
 *
 * @param <T> subclass of AlphanumTargetSighting
 */
class AlphanumTargetSightingsDatabaseAccessor<T : AlphanumTargetSighting>(modelClass: Class<T>) : TargetSightingsDatabaseAccessor<T>(modelClass) {}
