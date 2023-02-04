package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ebeaninternal.server.type.ScalarTypeJsonMapPostgres;
import org.cuair.ground.clients.InteropClient;
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

/**
 * Contains all the callbacks for all the public api endpoints for the Target.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/targets")
public class TargetController {
    private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao =
            (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory.getDAO(
                    DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

    /**
     * Constructs an HTTP response with all the targets and their characteristics.
     *
     * @return a mapping of all airdrop ids and their associated characteristics
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getTargets() {
        List<AlphanumTarget> targets = targetDao.getAll();
        Map<String, Object> targetMap = new HashMap<>();

        for (AlphanumTarget target : targets) {
            Map<String, Object> data = new HashMap<>();

            data.put("alpha", target.getAlpha());
            data.put("alphaColor", target.getAlphaColor().getName());
            data.put("shape", target.getShape().getName());
            data.put("shapeColor", target.getShapeColor().getName());

            targetMap.put(target.getAirdropId().toString(), data);
        }
        return ok(targetMap);
    }

}
