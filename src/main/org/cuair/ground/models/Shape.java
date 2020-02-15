package org.cuair.ground.models;

import com.fasterxml.jackson.annotation.JsonValue;

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
}
