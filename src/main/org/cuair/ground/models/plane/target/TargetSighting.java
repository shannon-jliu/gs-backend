package org.cuair.ground.models.plane.target;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.io.File;
import javax.imageio.ImageIO;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ClientCreatable;
import org.cuair.ground.models.Confidence;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.plane.target.Target;
import org.cuair.ground.util.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model to represent a target sighting. The target sighting is the sighting of
 * a target in a
 * specific image.
 */
@MappedSuperclass
public abstract class TargetSighting extends ClientCreatable {

  /**
   * Represents the Geotag of this target sighting that records the gps location
   * and the direction
   * that the target sighting is facing
   */
  @OneToOne(cascade = CascadeType.ALL)
  protected Geotag geotag;
  /**
   * The x pixel coordinate of the center of the target sighting in the specific
   * Image
   */
  protected Integer pixelx;
  /**
   * The y pixel coordinate of the center of the target sighting in the specific
   * Image
   */
  protected Integer pixely;
  /** The horizontal pixel width of the target sighting in the specific image */
  protected Integer width;
  /** The vertical pixel height of the target sighting in the specific image */
  protected Integer height;
  /** The confidence MDLC taggers have in the classification accuracy */
  protected Confidence mdlcClassConf;
  /**
   * The assignment from which this target sighting was created (contains the
   * image that this target
   * sighting was tagged in)
   */
  @ManyToOne
  private Assignment assignment;

  /**
   * Creates a TargetSighting
   *
   * @param creator               the ODLCUser that created this Target Sighting
   * @param geotag                Geotag of this Target Sighting
   * @param pixelx                Integer x pixel coordinate of the center of the
   *                              target sighting in the specific Image
   * @param pixely                Integer y pixel coordinate of the center of the
   *                              target sighting in the specific Image
   * @param mdlcClassConf         the confidence MDLC taggers have in the target
   *                              classification
   * @param assignment            the assignment that created this TargetSighting
   * @param width                 the horizontal pixel width of the TargetSighting
   * @param height                the vertical pixel height of the TargetSighting
   */
  public TargetSighting(
      ODLCUser creator,
      Integer pixelx,
      Integer pixely,
      Integer width,
      Integer height,
      Geotag geotag,
      Confidence mdlcClassConf,
      Assignment assignment) {
    super(creator);
    this.pixelx = pixelx;
    this.pixely = pixely;
    this.width = width;
    this.height = height;
    this.geotag = geotag;
    this.mdlcClassConf = mdlcClassConf;
    this.assignment = assignment;
  }

  /**
   * Given another target sighting, it updates all fields of this instance if
   * there are any
   * differences
   *
   * @param other TargetSighting containing updated fields
   */
  public void updateFromTargetSighting(TargetSighting other) {
    assert this.getAssignment() != null;

    if (other.getpixelx() != null) {
      this.pixelx = other.getpixelx();
    }
    if (other.getpixely() != null) {
      this.pixely = other.getpixely();
    }
    if (other.getWidth() != null) {
      this.width = other.getWidth();
    }
    if (other.getHeight() != null) {
      this.height = other.getHeight();
    }
    if (other.getGeotag() != null) {
      this.geotag = other.getGeotag();
    }
    if (other.getMdlcClassConf() != null) {
      this.mdlcClassConf = other.getMdlcClassConf();
    }
    if (other.getAssignment() != null) {
      this.assignment = other.getAssignment();
    }
  }

  /** Sets this target to be null */
  public abstract void makeAssociatedTargetNull();

  /**
   * Returns the raw content of the thumbnail corresponding to this target
   * sighting, for
   * submission to interop.
   */
  public byte[] thumbnailImage() throws IOException {
    Logger logger = LoggerFactory.getLogger(TargetSighting.class);
    Image image = this.getAssignment().getImage();
    assert image != null;
    String imgPathLocal = image.getLocalImageUrl();
    logger.info(imgPathLocal);
    // InputStream in = getClass().getResourceAsStream(imgPathLocal);
    // assert in != null;
    BufferedImage initialImage = ImageIO.read(new File(imgPathLocal));

    // Produce cropped thumbnail - need to scale up as values are from compressed
    // frontend image
    double scaleUpW = Flags.RAW_IMAGE_WIDTH / Flags.FRONTEND_IMAGE_WIDTH;
    double scaleUpH = Flags.RAW_IMAGE_HEIGHT / Flags.FRONTEND_IMAGE_HEIGHT;
    BufferedImage croppedThumb = initialImage.getSubimage(
        (int) (scaleUpW * (pixelx - width / 2)),
        (int) (scaleUpH * (pixely - height / 2)),
        (int) (scaleUpW * width),
        (int) (scaleUpH * height));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(croppedThumb, "jpg", baos);

    return baos.toByteArray();
  }

  /**
   * Gets the target
   *
   * @return Target
   */
  public abstract Target getTarget();

  /**
   * Gets the x pixel coordinate of the TargetSighting
   *
   * @return pixelx Integer x pixel coordinate
   */
  public Integer getpixelx() {
    return pixelx;
  }

  /**
   * Gets the y pixel coordinate of the TargetSighting
   *
   * @return Integer y pixel coordinate
   */
  public Integer getpixely() {
    return pixely;
  }

  /**
   * Gets the width of the TargetSighting in pixels
   *
   * @return width Integer pixel width
   */
  public Integer getWidth() {
    return width;
  }

  /**
   * Gets the height of the TargetSighting in pixels
   *
   * @return height Integer pixel height
   */
  public Integer getHeight() {
    return height;
  }

  /**
   * Gets the geotag of this TargetSighting
   *
   * @return Geotag representing the direction and location of the
   *         AlphanumTargetSighting
   */
  public Geotag getGeotag() {
    return geotag;
  }

  /**
   * Sets the geotag of this TargetSighting
   *
   * @param geotag Geotag representing the direction and location of the
   *               AlphanumTargetSighting
   */
  public void setGeotag(Geotag geotag) {
    this.geotag = geotag;
  }

  /**
   * Gets the assignment from which this target sighting was created
   *
   * @return the assignment for the target sighting
   */
  public Assignment getAssignment() {
    return assignment;
  }

  /**
   * Sets the assignment for this target sighting
   *
   * @param assignment the assignment to set for this target sighting
   */
  public void setAssignment(Assignment assignment) {
    this.assignment = assignment;
  }

  /**
   * Gets the confidence we have in the classification accuracy
   *
   * @return Confidence representing the classification confidence
   */
  public Confidence getMdlcClassConf() {
    return mdlcClassConf;
  }

  /**
   * Determines if the given object is logically equal to this
   * AlphanumTargetSighting
   *
   * @param o The object to compare
   * @return True if the object equals this AlphanumTargetSighting
   */
  @Override
  public boolean equals(Object o) {
    TargetSighting other = (TargetSighting) o;

    if (!super.equals(o))
      return false;

    if (!Objects.deepEquals(this.geotag, other.getGeotag()))
      return false;

    if (!Objects.deepEquals(this.pixelx, other.getpixelx()))
      return false;

    if (!Objects.deepEquals(this.pixely, other.getpixely()))
      return false;

    if (!Objects.deepEquals(this.width, other.getWidth()))
      return false;

    if (!Objects.deepEquals(this.height, other.getHeight()))
      return false;

    if (!Objects.deepEquals(this.mdlcClassConf, other.getMdlcClassConf()))
      return false;

    return Objects.deepEquals(this.assignment, other.getAssignment());
  }
}
