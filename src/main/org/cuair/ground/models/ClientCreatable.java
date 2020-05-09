package org.cuair.ground.models;

import java.util.Objects;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/** Model to represent any model that can be created by a ground server client */
@MappedSuperclass
public abstract class ClientCreatable extends CUAirModel {

  /** Designates who created the model */
  @ManyToOne
  private ODLCUser creator;

  /**
   * Creates a ClientCreatable
   *
   * @param creator the ODLCUser creator of the model
   */
  public ClientCreatable(ODLCUser creator) {
    this.creator = creator;
  }

  /**
   * Gets the ODLCUser creator of this model
   *
   * @return the ODLCUser creator of this model
   */
  public ODLCUser getCreator() {
    return creator;
  }

  /**
   * Sets the ODLCUser creator of this model
   *
   * @param creator new ODLCUser creator of this model
   */
  public void setCreator(ODLCUser creator) {
    this.creator = creator;
  }

  /**
   * Determines if the given object is logically equal to this model
   *
   * @param o The object to compare
   * @return True if the object equals this model
   */
  @Override
  public boolean equals(Object o) {
    ClientCreatable other = (ClientCreatable) o;
    return Objects.deepEquals(this.creator, other.getCreator());
  }
}
