package org.cuair.ground.controllers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.Color;
import org.cuair.ground.models.Shape;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.ResponseEntity.ok;
import org.cuair.ground.controllers.ImageController;
import org.cuair.ground.util.Flags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Contains all the callbacks for all the public api endpoints for thumbnails.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/thumbnail")
public class ThumbnailController {

    private String thumbnailDir = Flags.SYNTH_IMAGE_DIR;
    String shape;
    String shapeColor;
    String alpha;
    String alphaColor;

    private static final AlphanumTargetDatabaseAccessor<AlphanumTarget> targetDao =
            (AlphanumTargetDatabaseAccessor<AlphanumTarget>) DAOFactory.getDAO(
                    DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

    ImageController ic = new ImageController();

    /**
     * Constructs an HTTP response with the corresponding thumbnail
     *
     * @return the thumbnail associated with the airdropId
     */
    @RequestMapping(value = "/{airdropId}", method = RequestMethod.GET)
    public ResponseEntity getThumbnail(@PathVariable Long airdropId) {
        AlphanumTarget target = null;
        List<AlphanumTarget> targets = targetDao.getAll();
        for (AlphanumTarget t : targets) {
            if (t.getAirdropId() == airdropId) {
                target = t;
                break;
            }
        }
        if (target != null) {
            shape = target.getShape().getName().toUpperCase();
            shapeColor = target.getShapeColor().getName().toUpperCase();
            alpha = target.getAlpha();
            alphaColor = target.getAlphaColor().getName().toUpperCase();
        }

        String file = thumbnailDir + alphaColor + "_" + alpha + "_" + shapeColor + "_" + shape + ".jpg";


        File image = FileUtils.getFile(file);
        if (image.exists()) {
            HttpHeaders headers = new HttpHeaders();
            InputStream in = null;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File not found: "  + file);
            }

            byte[] media = null;
            try {
                media = IOUtils.toByteArray(in);
            } catch (Exception e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error reading file: " + file);
            }
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());

            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
            return responseEntity;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
