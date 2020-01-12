package org.cuair.ground.models;

import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Base class for all settings that get sent to the plane. All State models should extend this
 * class. It consists of a timestamp indicating when it was created.
 */
@MappedSuperclass
public abstract class PlaneModel extends TimestampModel {

    /** A timestamp for the plane */
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp planeTimestamp;
    
    /**
     * Retrieves the plane timestamp
     *
     * @return Timestamp timestamp
     */
    public Timestamp getPlaneTimestamp() {
        return planeTimestamp;
    }
    
    /**
     * Sets the timestamp
     *
     * @param planeTimestamp new timestamp
     */
    public void setPlaneTimestamp(Timestamp planeTimestamp) {
        this.planeTimestamp = planeTimestamp;
    }
    
    @Override
    public boolean equals(@NotNull Object o) {
        if (o == null || !super.equals(o)) {
            return false;
        }
        PlaneModel other = (PlaneModel) o;
        
        return Objects.deepEquals(this.planeTimestamp, other.getPlaneTimestamp());
    }
}
