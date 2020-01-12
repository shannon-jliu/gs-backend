package org.cuair.ground.models

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToOne

/** Represents an assignment of an image to either an MDLC or ADLC client for processing  */
@Entity
class Assignment : TimestampModel {

  /** Represents the Image this assignment has assigned  */
  @ManyToOne(cascade = arrayOf(CascadeType.ALL)) var image: Image?

  /** Represents the type of client this assignment has assigned to  */
  var assignee: ClientType

  /** Represents whether the image has been processed  */
  var done: Boolean

  /** Represents the user assigned to this assignment  */
  var username: String?

  /**
   * Creates an unprocessed Assignment object
   *
   * @param image the image this assignment is assigning
   * @param assignee the type of client this assignment is assigning to
   */
  constructor(image: Image?, assignee: ClientType) {
    this.image = image
    this.assignee = assignee
    this.done = false
    this.username = null
  }

  /**
   * Creates an unprocessed Assignment object
   *
   * @param image the image this assignment is assigning
   * @param assignee the type of client this assignment is assigning to
   * @param username the username of the client this assignment is assigning to
   */
  constructor(image: Image?, assignee: ClientType, username: String?) {
    this.image = image
    this.assignee = assignee
    this.done = false
    this.username = username
  }
}
