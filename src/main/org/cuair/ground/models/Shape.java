package org.cuair.ground.models;

import com.fasterxml.jackson.annotation.JsonValue;
import org.cuair.ground.protobuf.InteropApi.Odlc;

/** Represents the different shapes of Targets */
public enum Shape {
    CIRCLE("circle"),
    SEMICIRCLE("semicircle"),
    QUARTERCIRCLE("quarter_circle"),
    TRIANGLE("triangle"),
    SQUARE("square"),
    RECTANGLE("rectangle"),
    TRAPEZOID("trapezoid"),
    PENTAGON("pentagon"),
    HEXAGON("hexagon"),
    HEPTAGON("heptagon"),
    OCTAGON("octagon"),
    STAR("star"),
    CROSS("cross");

    /** Name of the shape */
    private String name;

    /**
     * Gets the name of the shape
     *
     * @return String name of the shape
     */
    @JsonValue
    public String getName() {
        return name;
    }

    /**
     * Creates a shape
     *
     * @param name String name of the shape
     */
    Shape(String name) {
        this.name = name;
    }

    public Odlc.Shape asProtoShape() {
        switch (this) {
          case CIRCLE:
            return Odlc.Shape.CIRCLE;
          case SEMICIRCLE:
            return Odlc.Shape.SEMICIRCLE;
          case QUARTERCIRCLE:
            return Odlc.Shape.QUARTER_CIRCLE;
          case TRIANGLE:
            return Odlc.Shape.TRIANGLE;
          case SQUARE:
            return Odlc.Shape.SQUARE;
          case RECTANGLE:
            return Odlc.Shape.RECTANGLE;
          case TRAPEZOID:
            return Odlc.Shape.TRAPEZOID;
          case PENTAGON:
            return Odlc.Shape.PENTAGON;
          case HEXAGON:
            return Odlc.Shape.HEXAGON;
          case HEPTAGON:
            return Odlc.Shape.HEPTAGON;
          case OCTAGON:
            return Odlc.Shape.OCTAGON;
          case STAR:
            return Odlc.Shape.STAR;
          case CROSS:
            return Odlc.Shape.CROSS;
        }
        return null;
    }
}
