package org.cuair.ground.daos;

import io.ebean.Ebean;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.geotag.MGTImage;

/** Database accessor for the manually geotagged images database */
public class MGTImageDatabaseAccessor extends DatabaseAccessor<MGTImage> {
    
    /** Constructs a database accessor object for the MGTImage class */
    MGTImageDatabaseAccessor() {
        super(MGTImage.class);
    }
    
    /**
     * Finds an MGT image that has not yet been assigned to a client
     *
     * @return an MGT image found
     */
    public MGTImage getUnsentMGTImage() {
        return Ebean.find(getModelClass())
            .where()
            .eq("is_sent", false)
            .where()
            .eq("hasTelemetry", true)
            .where()
            .eq("hasTarget", true)
            .setMaxRows(1)
            .findOne();
    }
    
    /**
     * Gets the MGT image from the corresponding image id
     *
     * @param imageId the image id
     * @return the MGT image
     */
    private MGTImage getMGTImageForImageId(Long imageId) {
        return Ebean.find(getModelClass()).where().eq("image_id", imageId).findOne();
    }
    
    /**
     * Updates an MGTImage to have an associated target
     *
     * @param i the associated image to be updated
     */
    public void setHasTarget(Image i) {
        if (i == null) return;
        MGTImage mgtImage = this.getMGTImageForImageId(i.getId());
        if (mgtImage == null) {
            mgtImage = new MGTImage(i);
            mgtImage.setHasTarget();
            create(mgtImage);
        } else {
            mgtImage.setHasTarget();
            update(mgtImage);
        }
    }
    
    /**
     * Updates an MGTImage to have telemetry data
     *
     * @param i the associated image to be updated
     */
    public void setHasTelemetry(Image i) {
        if (i == null) return;
        MGTImage mgtImage = this.getMGTImageForImageId(i.getId());
        if (mgtImage == null) {
            mgtImage = new MGTImage(i);
            mgtImage.setHasTelemetry();
            create(mgtImage);
        } else {
            mgtImage.setHasTelemetry();
            update(mgtImage);
        }
    }
}
