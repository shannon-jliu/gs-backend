package org.cuair.ground.models

import java.util.Objects
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

/**
 * Base class for models in the CUAir system. All models should extend from this class. The model
 * consists of a unique id.
 */
@MappedSuperclass
abstract class CUAirModel : Comparable<Any> {

    /** Unique identifier for every CUAirModel  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * Compares two CUAirModel instances via id. Sorts by earliest ID first. (e.g., ID 1 > ID 2)
     *
     * @param other The object to compare
     * @return 0 if equal, -1 if o is less than, 1 if o greater than
     */
    override operator fun compareTo(other: Any): Int {
        other as CUAirModel
        return java.lang.Long.signum(other.id!! - this.id!!)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CUAirModel) return false
        return this.id == other.id
    }
}
