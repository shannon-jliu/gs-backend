package org.cuair.ground.models.geotag;

import java.util.Objects;

/** A unit of angular measure such that a full circle corresponds to angle of 2PI */
public class Radian {

  /**
   * Determines if the given object is logically equal to this radian
   *
   * @param one The object to compare
   * @return True if the object equals this radian
   */
  public static boolean equals(Double one, Double two) {
    Double normalizeOne = one == null ? normalize(one) : null;
    Double normalizeTwo = one == null ? normalize(two) : null;
    return Objects.deepEquals(normalizeOne, normalizeTwo);
  }

  /**
   * Normalizes the Radian value to be within the [0,2PI] range
   *
   * @param radian A Double containing the radian value
   * @return A Double containing the normalized radian value
   */
  public static Double normalize(Double radian) {
    double r = radian.doubleValue();
    final double two_pi = 2 * Math.PI;
    if (r >= 0.0) {
      r = r % two_pi;
    } else {
      r = (r % two_pi) + two_pi;
    }
    return new Double(r);
  }

  /**
   * Adds two Radians
   *
   * @param one The first argument into the addition operator
   * @param two The second argument into the addition operator
   */
  public static Double add(Double one, Double two) {
    return normalize(one + two);
  }
}
