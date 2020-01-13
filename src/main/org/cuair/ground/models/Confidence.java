package org.cuair.ground.models;

import io.ebean.annotation.EnumValue;

/** Represents confidence level in target classification */
public enum Confidence {
    @EnumValue("high")
    HIGH,
    @EnumValue("medium")
    MEDIUM,
    @EnumValue("low")
    LOW
}
