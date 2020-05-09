package org.cuair.ground.models

import javax.persistence.Column
import javax.persistence.Entity
import io.ebean.annotation.EnumValue

/**
 * Contains the information about an ODLCUser
 * Only one ip can exist at a time
 * Only one username can exist at a time
 */
@Entity
data class ODLCUser(
    @Column(unique = true) val username: String,
    @Column val address: String,
    @Column val userType: UserType
) : CUAirModel() {


    /** The possible user types: tagger, operator and adlc */
    enum class UserType (val mode: String) {
        @EnumValue("0") MDLCTAGGER("tagger"),
        @EnumValue("1") MDLCOPERATOR("operator"),
        @EnumValue("2") ADLC("adlc")
    }

    /**
     * Returns true if the ip address matches the other User object's ip address
     * @param other the object to compare
     * @return true if the object equals this ODLCUser
     */
    override fun equals(other: Any?): Boolean {
        if (other !is ODLCUser) return false
        if (this.username != other.username) return false
        if (this.address != other.address) return false
        if (this.userType != other.userType) return false
        return true
    }
}
