package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ebeaninternal.server.type.ScalarTypeJsonMapPostgres;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.Target;
import org.cuair.ground.util.Flags;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin
@RestController
@RequestMapping(value = "/logs")
public class LogsController {

    @RequestMapping(value = "/manual_input", method = RequestMethod.GET)
    public ResponseEntity getManualInput() {
        String data = "";
        return ok(data);
    }
}
