package org.cuair.ground.daos

import io.ebean.Ebean
import org.cuair.ground.models.ClientType
import org.cuair.ground.models.geotag.GpsLocation
import org.cuair.ground.models.plane.target.AlphanumTargetSighting

/**
 * Database Accessor object for Alphanumeric Target Sightings
 *
 * @param <T> subclass of AlphanumTargetSighting
 */
class AlphanumTargetSightingsDatabaseAccessor<T : AlphanumTargetSighting>(modelClass: Class<T>) : TargetSightingsDatabaseAccessor<T>(modelClass) {
    /**
     *  Retrieves GpsLocations for the {@code numTags} saved ADLC target sightings that have non-null geotags with the
     *  highest {@code adlcClassConf} values. If there are fewer entries in the database than {@code numTags}, the list's
     *  length is equivalent to the number of entries.
     *
     *  @param numTags number of GpsLocations to return
     *  @return List<GpsLocation> of top locations in descending order. Entries may be null if associated geotags have
     *          null {@code gpsLocation} field. If two confidences are equal, the one with the lower id is first.
     */
    fun getTopAdlcLocations(numTags : Int) : List<GpsLocation?> {
        return Ebean.find(modelClass)
            .select("geotag")
            .where()
            .isNotNull("geotag")
            .eq("creator", ClientType.ADLC)
            .order("adlc_class_conf DESC, id ASC")
            .setMaxRows(numTags)
            .findList()
            .map { ts -> ts?.geotag?.gpsLocation }
    }
}
