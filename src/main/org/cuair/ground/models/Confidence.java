package org.cuair.ground.models;

import io.ebean.annotation.EnumValue;

/** Represents confidence level in target classification */
public enum Confidence {
  @EnumValue("2")
  HIGH("high"),
  @EnumValue("1")
  MEDIUM("medium"),
  @EnumValue("0")
  LOW("low");

  /** Name of the confidence */
  private int id;
  private String name;

  /**
   * Creates a confidence
   *
   * @param name String name of the confidence
   */
  Confidence(String name) {
    this.name = name.toLowerCase();
    if (name == "low") {
      id = 0;
    } else if (name == "medium") {
      id = 1;
    } else {
      id = 2;
    }
  }

  /**
   * Gets the name of the confidence
   *
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the name of the confidence
   *
   * @return
   */
  public String getName() {
    return name;
  }
}
