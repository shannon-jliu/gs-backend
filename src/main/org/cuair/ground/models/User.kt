package org.cuair.ground.models

import javax.persistence.Entity
import javax.persistence.Column

@Entity
data class User(
    @Column(unique = true) val username: String,
    @Column(unique = true) val hostname: String
) : CUAirModel() {
    /**
     * Returns true if both the username and hostname match the other User object
     * @param other the object to compare
     * @return true if the object equals this User
     */
    override fun equals(other: Any?): Boolean {
        if (other !is User) return false

        if (this.username != other.username) return false
        if (this.hostname != other.hostname) return false
        return true
    }
}
