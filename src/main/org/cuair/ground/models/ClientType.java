package org.cuair.ground.models;

import io.ebean.annotation.EnumValue;

/** Enum representing the different possible creators for a model */
public enum ClientType {
  /** Signifies that the MDLC client created the model */
  @EnumValue("mdlc")
  MDLC,
  /** Signifies that the ADLC client created the model */
  @EnumValue("adlc")
  ADLC
}
