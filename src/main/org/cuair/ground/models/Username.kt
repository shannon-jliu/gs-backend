package org.cuair.ground.models

import javax.persistence.Entity
import javax.persistence.Column

/**
 * Contains the information about a Username, which is effectively a tuple of a username the user's IP address.
 * Only one username can ever exist a time
 * Only one username can exist for each address
 */
@Entity
data class Username(
    @Column(unique = true) val username: String,
    @Column(unique = true) val address: String
) : CUAirModel() {
    /**
     * Returns true if both the username and address match the other User object
     * @param other the object to compare
     * @return true if the object equals this Username
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Username) return false

        if (this.username != other.username) return false
        if (this.address != other.address) return false
        return true
    }
}
