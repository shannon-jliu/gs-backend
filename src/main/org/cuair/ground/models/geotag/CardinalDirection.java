package org.cuair.ground.models.geotag;

import org.cuair.ground.protobuf.InteropApi.Odlc;

/**
 * Represents one of the eight cardinal directions -> assumes radians is given from north counter
 * clockwise
 */
public enum CardinalDirection {
    /**
     * The cardinal direction north. The range of radians that falls under this direction is
     * [15*Math.PI/8, Math.PI/8)
     */
    NORTH("N"),

    /**
     * The ordinal direction northeast. The range of radians that falls under this direction is
     * [Math.PI/8, 3*Math.PI/8)
     */
    NORTHEAST("NE"),

    /**
     * The cardinal direction east. The range of radians that falls under this direction is
     * [3*Math.PI/8, 5*Math.PI/8)
     */
    EAST("E"),

    /**
     * The ordinal direction southeast. The range of radians that falls under this direction is
     * [5*Math.PI/8, 7*Math.PI/8)
     */
    SOUTHEAST("SE"),

    /**
     * The cardinal direction south. The range of radians that falls under this direction is
     * [7*Math.PI/8, 9*Math.PI/8)
     */
    SOUTH("S"),

    /**
     * The ordinal direction southwest. The range of radians that falls under this direction is
     * [9*Math.PI/8, 11*Math.PI/8)
     */
    SOUTHWEST("SW"),

    /**
     * The cardinal direction west. The range of radians that falls under this direction is
     * [11*Math.PI/8, 13*Math.PI/8)
     */
    WEST("W"),

    /**
     * The ordinal direction northwest. The range of radians that falls under this direction is
     * [13*Math.PI/8, 15*Math.PI/8)
     */
    NORTHWEST("NW");

    /** One to Two letter abbreviation for the CardinalDirection */
    private String abbreviation;

    /**
     * Creates a CardinalDirection
     *
     * @param abbreviation String abbreviation for a direction
     */
    CardinalDirection(String abbreviation) {
        this.setAbbreviation(abbreviation);
    }

    /**
     * Gets the abbreviation of this CaridinalDirection
     *
     * @return String abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Sets the abbreviation for this CardinalDirection
     *
     * @param abbreviation String new abbreviation
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Given an arbitrary radian value, determine which cardinal direction it should be classified as.
     *
     * @param radian The radian value to bucket into a cardinal direction
     * @return The CardinalDirection the radian value falls under
     */
    public static CardinalDirection getFromRadians(Double radian) {
        int index = (int) ((Radian.normalize(radian + Math.PI / 8)) / (Math.PI / 4));
        return CardinalDirection.values()[index];
    }

    public Odlc.Orientation asProtoOrientation() {
        switch (this) {
          case NORTH:
            return Odlc.Orientation.N;
          case NORTHEAST:
            return Odlc.Orientation.NE;
          case EAST:
            return Odlc.Orientation.E;
          case SOUTHEAST:
            return Odlc.Orientation.SE;
          case SOUTH:
            return Odlc.Orientation.S;
          case SOUTHWEST:
            return Odlc.Orientation.SW;
          case WEST:
            return Odlc.Orientation.W;
          case NORTHWEST:
            return Odlc.Orientation.NW;
        }
        return null;
    }
}
