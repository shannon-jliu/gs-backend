package org.cuair.ground.daos;

import org.cuair.ground.models.Image;

/** Database Accessor Object that provides an interface for persisting images. */
public class ImageDatabaseAccessor extends TimestampDatabaseAccessor<Image> {
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
