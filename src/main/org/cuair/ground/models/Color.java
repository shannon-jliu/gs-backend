package org.cuair.ground.models;

import com.fasterxml.jackson.annotation.JsonValue;

/** Represents the different colors on the Target */
public enum Color {
  WHITE("white"),
  BLACK("black"),
  GRAY("gray"),
  RED("red"),
  BLUE("blue"),
  GREEN("green"),
  YELLOW("yellow"),
  PURPLE("purple"),
  BROWN("brown"),
  ORANGE("orange");

  /** Name of the color */
  private String name;

  /**
   * Creates a color
   *
   * @param name String name of the color
   */
  Color(String name) {
    this.name = name;
  }

  /**
   * Gets the name of the color
   *
   * @return
   */
  @JsonValue
  public String getName() {
    return name;
  }
}
