package de.oscillation.kismet.connector;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message send from the kismet resource adapter to the {@link KismetMessageService}. A message contains
 * data collected from the kismet server.
 * 
 * @author Benedikt Meurer
 * @see KismetMessageService
 */
public class KismetMessage implements Serializable {
    /** The serial version UID of this class. */
    private static final long serialVersionUID = 1L;

    /** The mapping of known drone UUIDs to their configured names. */
    private final Map<UUID, String> droneNames;

    /** The list of measured values. */
    private final List<KismetMeasuredValue> measuredValues;

    /** The kismet server timestamp in seconds. */
    private final long timestamp;

    /**
     * Constructs a new kismet message with the specified parameters.
     * 
     * @param droneNames the mapping of known drone UUIDs to their configured names.
     * @param measuredValues the list of measured values.
     * @param timestamp the kismet server timestamp in seconds.
     * @throws NullPointerException if either <code>droneNames</code> or <code>measuredValues</code> is
     *             <code>null</code>.
     */
    public KismetMessage(Map<UUID, String> droneNames, List<KismetMeasuredValue> measuredValues, long timestamp) {
        if (droneNames == null) {
            throw new NullPointerException("droneNames must not be null");
        }
        if (measuredValues == null) {
            throw new NullPointerException("measuredValues must not be null");
        }
        this.droneNames = droneNames;
        this.measuredValues = measuredValues;
        this.timestamp = timestamp;
    }

    /**
     * Returns the mapping of known drone UUIDs to their configured names. The mapping contains all drones seen by the
     * kismet resource adapter up to the point where this message was generated, so it is grow-only and will also catch
     * up drones added to the kismet server at runtime.
     * 
     * @return the mapping of drone UUIDs to their names.
     */
    public Map<UUID, String> getDroneNames() {
        return this.droneNames;
    }

    /**
     * Returns a list of values measured by the kismet resource adapter since the last message was generated. The list
     * is ordered by the time of reception from the kismet server.
     * 
     * @return the list of measured values.
     * @see KismetMeasuredValue
     */
    public List<KismetMeasuredValue> getMeasuredValues() {
        return this.measuredValues;
    }

    /**
     * Returns the kismet server timestamp in seconds at the time when this message was generated.
     * 
     * @return the last timestamp from the kismet server.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        else if (o instanceof KismetMessage) {
            KismetMessage msg = (KismetMessage) o;
            if (this.droneNames.equals(msg.droneNames) && this.measuredValues.equals(msg.measuredValues) && this.timestamp == msg.timestamp) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.droneNames.hashCode() ^ this.measuredValues.hashCode() ^ (int) this.timestamp ^ (int) (this.timestamp >> 32);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(KismetMessage.class.getName());
        sb.append("[droneNames=" + this.droneNames + "]");
        sb.append("[measuredValues=" + this.measuredValues + "]");
        sb.append("[timestamp=" + this.timestamp + "]");
        return sb.toString();
    }
}
