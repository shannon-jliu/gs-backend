package org.cuair.ground.util

import org.cuair.ground.daos.AuthTokenDatabaseAccessor
import org.cuair.ground.daos.DAOFactory
import org.cuair.ground.models.AuthToken
import org.mindrot.jbcrypt.BCrypt
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value

/** Utilities for accessing authentication information from requests */
class AuthUtil {
    companion object {
        /** Flag to enabled authentication */
        val enabled = false

        /** Database accessor for checking whether or not authentication tokens are present in the DB */
        private val authTokenDao = DAOFactory.getDAO(DAOFactory.ModellessDAOType.AUTH_TOKEN_DATABASE_ACCESSOR) as AuthTokenDatabaseAccessor

        /** Header used to specify the authentication token */
        private val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"

        /** Generated application secret */
        private val APPLICATION_SECRET = "PlayConfig.PLAY_CRYPTO_SECRET"

        /** Generated bcrypt salt */
        // TODO: IMPLEMENT FLAG
        private val salt = "\$2a\$10\$jYhW.WEWTi0DKMmfvw6tne"

        /**
         * Hashes the password with a salt
         *
         * @param password the
         */
        fun hashPassword(password: String): String = BCrypt.hashpw(password, salt)

        /**
         * Creates an authentication token given the username and admin properties
         *
         * @param username the username
         * @param isAdmin whether the token should have admin privileges
         * @return the authentication token
         */
        fun createToken(username: String, isAdmin: Boolean): AuthToken {
            authTokenDao.deleteByUsername(username)
            var token =
                BCrypt.hashpw(username + System.currentTimeMillis().toString() + APPLICATION_SECRET, salt)
            while (authTokenDao.getByToken(token) != null) token = token[0].inc() + token.drop(1)
            val authToken = AuthToken(token, username, isAdmin)
            authTokenDao.create(authToken)
            return authToken
        }

        /**
         * Checks whether or not an authenticated user with the provided username exists
         *
         * @param username the username to check against
         * @return true iff such a user is present
         */
        fun userExists(username: String): Boolean = authTokenDao.getByUsername(username) != null

        /**
         * Gets the authentication token of a given request
         *
         * @param json the request headers
         * @return the authentication token
         */
        fun getToken(headers: HttpHeaders): AuthToken? {
            val token = headers.get(AUTH_TOKEN_HEADER)?.get(0);

            if (token != null) {
                return authTokenDao.getByToken(token)
            } else {
                return null
            }
        }
    }
}
