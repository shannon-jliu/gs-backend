package org.cuair.ground.daos;

import org.cuair.ground.models.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Database Accessor Object that provides an interface for persisting images. */
public class ImageDatabaseAccessor extends TimestampDatabaseAccessor<Image> {

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(ImageDatabaseAccessor.class);

    /** Constructs a database accessor object for the image class */
    ImageDatabaseAccessor() {
        super(Image.class);
    }


    public boolean setImageHasMDLCAssignment(Image i) {
        i.setHasMdlcAssignment(true);
        return this.update(i);
    }

    public boolean setImageHasADLCAssignment(Image i) {
        i.setHasAdlcAssignment(true);
        return this.update(i);
    }
}
