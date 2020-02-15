package org.cuair.ground.models

import javax.persistence.Column
import javax.persistence.Entity

/** Model that represent a user's authentication token */
@Entity
data class AuthToken(@Column(unique = true) val token: String,
                     @Column(unique = true) val username: String,
                     val admin: Boolean = false) : CUAirModel()
