package org.cuair.ground.models.plane.settings

import org.cuair.ground.models.plane.PlaneModel
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class PlaneSettingsModel : PlaneModel()
// TODO get rid of the "open" once this class is populated with more methods to avoid default Kotlin final class
