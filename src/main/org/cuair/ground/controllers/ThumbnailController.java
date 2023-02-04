package org.cuair.ground.controllers;

import org.apache.commons.io.FileUtils;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.models.Color;
import org.cuair.ground.models.Shape;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.ResponseEntity.ok;
import org.cuair.ground.controllers.ImageController;
import org.cuair.ground.util.Flags;
import java.util.List;

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
            shape = target.getShape().getName();
            shapeColor = target.getShapeColor().getName();
            alpha = target.getAlpha();
            alphaColor = target.getAlphaColor().getName();
        }

        String filename = alphaColor + "_" + alpha + "_" + shapeColor + "_" + shape;

        return (ic.getFile(thumbnailDir + filename));
    }

}
