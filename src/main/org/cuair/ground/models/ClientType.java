package org.cuair.ground.models;

import io.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Enum representing the different possible creators for a model */
public enum ClientType {
    /** Signifies that the MDLC client created the model */
    @JsonProperty("mdlc")
    @EnumValue("0")
    MDLC("mdlc"),
    /** Signifies that the ADLC client created the model */
    @JsonProperty("adlc")
    @EnumValue("1")
    ADLC("adlc");

    /** Name of the client type */
    private int id;
    private String name;

    /**
     * Gets the name of the client type
     *
     * @return
     */
    // @JsonValue
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the client type
     *
     * @return
     */
    // @JsonValue
    public String getName() {
        return name;
    }

    /**
     * Creates a client type
     *
     * @param name String name of the client type
     */
    ClientType(String name) {
        this.name = name;
        if (name == "mdlc") {
            id = 0;
        } else {
            id = 1;
        }
    }
}
