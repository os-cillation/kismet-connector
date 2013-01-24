package de.oscillation.kismet.connector;

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 * TODO
 * 
 * @author Benedikt Meurer
 */
@Activation(messageListeners = KismetMessageService.class)
public class KismetActivationSpec implements ActivationSpec, Serializable {
    /** The serial version UID of this class. */
    private static final long serialVersionUID = 1L;

    /** The kismet server name. */
    @ConfigProperty(description = "Kismet server name")
    private String serverName = "localhost";

    /** The kismet server port number. */
    @ConfigProperty(description = "Kismet server port number")
    private Integer portNumber = 2501;

    /** The resource adapter. */
    private ResourceAdapter resourceAdapter;

    /**
     * Returns the server name of this <code>KismetActivationSpec</code> object.
     * 
     * @return the server name.
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Set the server name of this <code>KismetActivationSpec</code> object to the specified <code>serverName</code>.
     * 
     * @param serverName the server name.
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Returns the port number of this <code>KismetActivationSpec</code> object.
     * 
     * @return the port number.
     */
    public Integer getPortNumber() {
        return this.portNumber;
    }

    /**
     * Set the port number of this <code>KismetActivationSpec</code> object to the specified <code>portNumber</code>.
     * 
     * @param portNumber the port number.
     */
    public void setPort(Integer portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * @see javax.resource.spi.ResourceAdapterAssociation#getResourceAdapter()
     */
    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.resourceAdapter;
    }

    /**
     * @see javax.resource.spi.ResourceAdapterAssociation#setResourceAdapter(javax.resource.spi.ResourceAdapter)
     */
    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        this.resourceAdapter = resourceAdapter;
    }

    /**
     * @see javax.resource.spi.ActivationSpec#validate()
     */
    @Override
    public void validate() throws InvalidPropertyException {
        if (this.serverName == null) {
            throw new InvalidPropertyException("serverName must not be null");
        }
        if (this.portNumber == null) {
            throw new InvalidPropertyException("portNumber must not be null");
        }
        if (this.portNumber <= 0 || this.portNumber >= 65536) {
            throw new InvalidPropertyException("Invalid portNumber " + this.portNumber);
        }
    }
}
