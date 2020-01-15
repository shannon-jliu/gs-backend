package org.cuair.ground.controllers;

import org.cuair.ground.util.AuthUtil;
import org.cuair.ground.models.AuthToken;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.HttpHeaders;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

/** API callbacks to handle authenticating with the ground server */
@RestController
@RequestMapping(value = "/assignment")
public class AuthController {

    /**
     * Gets an auth token if the credentials are valid
     *
     * @return auth token
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity get(@RequestHeader HttpHeaders headers, @RequestParam String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = null;
        try {
            json = (ObjectNode) mapper.readTree(jsonString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when parsing json from request: \n" + e);
        }
        AuthToken confirmationToken = AuthUtil.Companion.getToken(json);

        ResponseEntity confirmation = ResponseEntity.ok("");
        if (confirmationToken.getAdmin()) {
            confirmation = ResponseEntity.ok("admin");
        }

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
