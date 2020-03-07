package org.cuair.ground.models

import org.cuair.ground.models.TimestampModel
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class PlaneModel : TimestampModel()