package org.cuair.ground.daos

import io.ebean.Ebean
import org.cuair.ground.models.AuthToken

/** Database accessor for authentication tokens  */
class AuthTokenDatabaseAccessor internal constructor() : DatabaseAccessor<AuthToken>(AuthToken::class.java) {
    
    /**
     * Gets an authentication token model by looking for the provided token value
     *
     * @param token the token value represented by a string
     * @return the authentication token model or null if no such token is present
     */
    fun getByToken(token: String): AuthToken? {
        return Ebean.find(AuthToken::class.java).where().eq("token", token).findOne()
    }
    
    /**
     * Gets the authentication token corresponding to the user with the provided username
     *
     * @param username the username to check against
     * @return the authentication token if present or null otherwise
     */
    fun getByUsername(username: String): AuthToken? {
        return Ebean.find(AuthToken::class.java).where().eq("username", username).findOne()
    }
    
    /**
     * Deletes an authentication token model by looking for the provided username
     *
     * @param username the username
     */
    fun deleteByUsername(username: String) {
        val token = Ebean.find(AuthToken::class.java).where().eq("username", username).findOne()
        token?.let { super.delete(it.id) }
    }
}
