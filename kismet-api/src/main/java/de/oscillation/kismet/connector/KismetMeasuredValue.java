package de.oscillation.kismet.connector;

import java.io.Serializable;
import java.util.UUID;

import de.benediktmeurer.eui4j.EUI48;

/**
 * Representation of a signal strength value measured by a kismet drone for a given device. The
 * drone is identified by its UUID, while the device is identified by its EUI-48 address.
 * 
 * @author Benedikt Meurer
 * @see KismetMessage
 */
public final class KismetMeasuredValue implements Serializable {
    /** The serial version UID of this class. */
    private static final long serialVersionUID = 1L;

    /** The extended unique identifier (EUI-48) of the device. */
    private final EUI48 deviceId;

    /** The unique identifier of the drone. */
    private final UUID droneId;

    /** The signal strength in dBm. */
    private final int signalStrength;

    /** The kismet server timestamp in seconds. */
    private final long timestamp;

    /**
     * Constructs a new measured value with the given parameters.
     * 
     * @param deviceId the extended unique identifier (EUI-48) of the device.
     * @param droneId the universial unique identifier (UUID) of the drone.
     * @param signalStrength the signal strength in dBm.
     * @param timestamp the kismet server timestamp in seconds.
     * @throws NullPointerException if either <code>deviceId</code> or <code>droneId</code> is
     *             <code>null</code>.
     */
    public KismetMeasuredValue(EUI48 deviceId, UUID droneId, int signalStrength, long timestamp) {
        if (deviceId == null) {
            throw new NullPointerException("deviceId must not be null");
        }
        if (droneId == null) {
            throw new NullPointerException("droneId must not be null");
        }
        this.deviceId = deviceId;
        this.droneId = droneId;
        this.signalStrength = signalStrength;
        this.timestamp = timestamp;
    }

    /**
     * Returns the unique identifier of the device. Every device is uniquely identified by its
     * EUI-48 address.
     * 
     * @return the device identifier.
     */
    public EUI48 getDeviceId() {
        return this.deviceId;
    }

    /**
     * Returns the unique identifier of the drone. Every kismet drone is uniquely identified by its
     * UUID.
     * 
     * @return the drone identifier.
     */
    public UUID getDroneId() {
        return this.droneId;
    }

    /**
     * Returns the signal strength in dBm.
     * 
     * @return the signal strength.
     */
    public int getSignalStrength() {
        return this.signalStrength;
    }

    /**
     * Returns the kismet server timestamp in seconds.
     * 
     * @return the kismet server timestamp.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        else if (o instanceof KismetMeasuredValue) {
            KismetMeasuredValue v = (KismetMeasuredValue) o;
            if (this.deviceId.equals(v.deviceId) && this.droneId.equals(v.droneId)
                && this.signalStrength == v.signalStrength && this.timestamp == v.timestamp) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + (this.deviceId == null ? 0 : this.deviceId.hashCode());
        hash = hash * 13 + (this.droneId == null ? 0 : this.droneId.hashCode());
        hash = hash * 13 + this.signalStrength;
        hash = hash * 13 + ((int) this.timestamp ^ (int) (this.timestamp >> 32));
        return hash;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KismetMeasuredValue[deviceId=");
        sb.append(this.deviceId);
        sb.append(",droneId=");
        sb.append(this.droneId);
        sb.append(",signalStrength=");
        sb.append(this.signalStrength);
        sb.append(",timestamp=");
        sb.append(this.timestamp);
        sb.append(']');
        return sb.toString();
    }
}
