package org.cuair.ground.controllers;

import org.cuair.ground.util.AuthUtil;
import org.cuair.ground.models.AuthToken;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

/** API callbacks to handle authenticating with the ground server */
@CrossOrigin
@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    @Value("${cuair.auth.user_password_hash}") private String CUAIR_AUTH_USER_PASSWORD_HASH;
    @Value("${cuair.auth.admin_password_hash}") private String CUAIR_AUTH_ADMIN_PASSWORD_HASH;
    @Value("${cuair.auth.overwritable_usernames}") private List<String> CUAIR_AUTH_OVERWRITABLE_USERNAMES;
    @Value("${cuair.auth.camera_password_hash}") private String CUAIR_AUTH_CAMERA_PASSWORD_HASH;
    @Value("${cuair.auth.judges_password_hash}") private String CUAIR_AUTH_JUDGES_PASSWORD_HASH;

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    /**
     * Gets an auth token if the credentials are valid
     *
     * @return auth token
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity get(@RequestHeader HttpHeaders headers) {
        System.out.println("auth controller");
        AuthToken confirmationToken = AuthUtil.Companion.getToken(headers);

        if (confirmationToken != null) {
            if (confirmationToken.getAdmin()) {
                return ResponseEntity.ok("admin");
            }
            return ResponseEntity.ok("");
        }

        // TODO: Fix admin/logging in if already have auth token
        // ResponseEntity confirmation = ResponseEntity.ok("");
        // if (confirmationToken.getAdmin()) {
        //     confirmation = ResponseEntity.ok("admin");
        // }

        List<String> password = headers.get("Authorization");
        if (password != null) {
            List<String> username = headers.get("Username");
            if (username != null) {
                assert !username.isEmpty();
                if (password.size() > 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only one value should be in the \"Authorization\" header");
                }
                if (username.size() > 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only one value should be in the \"Username\" header");
                }

                boolean reserved = CUAIR_AUTH_OVERWRITABLE_USERNAMES.contains(username.get(0));
                System.out.println("username here");

                String confirmationTokenUsername = (confirmationToken != null) ? confirmationToken.getUsername() : null;

                if (AuthUtil.Companion.userExists(username.get(0)) && confirmationTokenUsername != username.get(0) && !reserved) {
                    System.out.println("username here 1");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken");
                }

                String hash = AuthUtil.Companion.hashPassword(password.get(0));
                System.out.println(hash);
                if (hash == CUAIR_AUTH_USER_PASSWORD_HASH || true) { // todo get rid of this here
                    if (reserved) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is reserved");
                    }
                    return ResponseEntity.ok(AuthUtil.Companion.createToken(username.get(0), false));
                } else if (hash == CUAIR_AUTH_ADMIN_PASSWORD_HASH) {
                    if (reserved) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is reserved");
                    }
                    return ResponseEntity.ok(AuthUtil.Companion.createToken(username.get(0), true));
                } else if (hash == CUAIR_AUTH_CAMERA_PASSWORD_HASH) {
                    if (username.get(0) != "Camera") {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username should be \"Camera\"");
                    }
                    return ResponseEntity.ok(AuthUtil.Companion.createToken(username.get(0), true));
                } else if (hash == CUAIR_AUTH_JUDGES_PASSWORD_HASH) {
                    if (username.get(0) != "JudgesView") {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username should be \"JudgesView\"");
                    }
                    return ResponseEntity.ok(AuthUtil.Companion.createToken(username.get(0), true));
                }
                System.out.println("username here 2");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid password");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing \"Username\" header");
            }
        } else {
            System.out.println("username here 3");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing \"Authorization\" header");
        }
    }
}
