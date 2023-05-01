package org.cuair.ground.models.geotag

import java.util.Objects

/** A unit of angular measure such that a full circle corresponds to angle of 2PI  */
object Radian {
    /**
     * Determines if the given object is logically equal to this radian
     *
     * @param one The object to compare
     * @return True if the object equals this radian
     */
    fun equals(one: Double?, two: Double?): Boolean {
        val normalizeOne = if (one != null) normalize(one) else null
        val normalizeTwo = if (two != null) normalize(two) else null
        return Objects.deepEquals(normalizeOne, normalizeTwo)
    }

    /**
     * Normalizes the Radian value to be within the [0,2PI] range
     *
     * @param radian A Double containing the radian value
     * @return A Double containing the normalized radian value
     */
    @JvmStatic
    fun normalize(radian: Double): Double {
        var r = radian
        val twoPi = 2 * Math.PI
        r = if (r >= 0.0) {
            r % twoPi
        } else {
            r % twoPi + twoPi
        }
        return r
    }

    /**
     * Adds two Radians
     *
     * @param one The first argument into the addition operator
     * @param two The second argument into the addition operator
     */
    fun add(one: Double, two: Double): Double {
        return normalize(one + two)
    }

    /**
     * Get the average of a variable number of radians
     *
     * @param radians A variable number of radians, passed in as varargs or an array
     * @return A new Radian representing the average
     */
    @JvmStatic
    fun median(vararg radians: Double?): Double? {
        var nonNullRadians = 0
        val rads: MutableList<Double> = ArrayList(radians.size)
        for (radian in radians) {
            if (radian != null && !radian.isNaN()) {
                nonNullRadians++
                rads.add(radian)
            }
        }
        if (nonNullRadians == 0) {
            return null
        }
        rads.sort()
        val medianIndex = nonNullRadians / 2
        val medianRadVal = if (nonNullRadians % 2 == 0) (rads[medianIndex] + rads[medianIndex - 1]) / 2 else rads[medianIndex]
        return normalize(medianRadVal)
    }
}
