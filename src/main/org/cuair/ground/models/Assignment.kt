package org.cuair.ground.models

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToOne

/** Represents an assignment of an image to either an MDLC or ADLC client for processing  */
@Entity
class Assignment : TimestampModel {

  /** Represents the Image this assignment has assigned  */
  @ManyToOne(cascade = arrayOf(CascadeType.ALL)) var image: Image?

  /** Represents the client this assignment has been assigned to  */
  // TODO fix issue #61 (null fields on assignee after pulling from assignment dao)
  @ManyToOne(cascade = arrayOf(CascadeType.ALL)) var assignee: ODLCUser

  /** Represents whether the image has been processed  */
  var done: Boolean

  /**
   * Creates an unprocessed Assignment object
   *
   * @param image the image this assignment is assigning
   * @param assignee the ODLCUser this assignment is assigning to
   */
  constructor(image: Image?, assignee: ODLCUser) {
    this.image = image
    this.assignee = assignee
    this.done = false
  }
}
