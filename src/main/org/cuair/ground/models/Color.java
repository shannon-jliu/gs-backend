package org.cuair.ground.models;

import com.fasterxml.jackson.annotation.JsonValue;
import org.cuair.ground.protobuf.InteropApi.Odlc;

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
     * Gets the name of the color
     *
     * @return
     */
    @JsonValue
    public String getName() {
        return name;
    }

    /**
     * Creates a color
     *
     * @param name String name of the color
     */
    Color(String name) {
        this.name = name;
    }

    public Odlc.Color asProtoColor() {
        switch (this) {
          case WHITE:
            return Odlc.Color.WHITE;
          case BLACK:
            return Odlc.Color.BLACK;
          case GRAY:
            return Odlc.Color.GRAY;
          case RED:
            return Odlc.Color.RED;
          case BLUE:
            return Odlc.Color.BLUE;
          case GREEN:
            return Odlc.Color.GREEN;
          case YELLOW:
            return Odlc.Color.YELLOW;
          case PURPLE:
            return Odlc.Color.PURPLE;
          case BROWN:
            return Odlc.Color.BROWN;
          case ORANGE:
            return Odlc.Color.ORANGE;
        }
        return null;
    }
}
