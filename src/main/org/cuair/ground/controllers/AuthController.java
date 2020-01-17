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

/** API callbacks to handle authenticating with the ground server */
@CrossOrigin
@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    /**
     * Gets an auth token if the credentials are valid
     *
     * @return auth token
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity get(@RequestHeader HttpHeaders headers) {
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

                String confirmationTokenUsername = (confirmationToken != null) ? confirmationToken.getUsername() : null;

                if (AuthUtil.Companion.userExists(username.get(0)) && confirmationTokenUsername != username.get(0)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken");
                }

                // TODO: Implement hash checking
                return ResponseEntity.ok(AuthUtil.Companion.createToken(username.get(0), true));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing \"Username\" header");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing \"Authorization\" header");
        }
    }
}
