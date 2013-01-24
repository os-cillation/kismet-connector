package de.oscillation.kismet.connector;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * The resource adapter used to talk to the Kismet server.
 * 
 * @author Benedikt Meurer
 */
@Connector(description = "Kismet Resource Adapter", eisType = "Kismet", vendorName = "os-cillation GmbH", version = "1.0")
public class KismetResourceAdapter implements ResourceAdapter, Serializable {
    /** The serial version UID of this class. */
    private static final long serialVersionUID = 1L;

    /** The list of active kismet server connections. */
    private LinkedList<KismetServerConnection> connections = new LinkedList<KismetServerConnection>();

    /** The logger. */
    private Logger logger = Logger.getLogger(KismetResourceAdapter.class.getName());

    /** The work manager. */
    private WorkManager workManager;

    /**
     * @see javax.resource.spi.ResourceAdapter#start(javax.resource.spi.BootstrapContext)
     */
    @Override
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        this.logger.info("Starting kismet resource adapter");
        this.workManager = bootstrapContext.getWorkManager();
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#stop()
     */
    @Override
    public void stop() {
        this.logger.info("Stopping kismet resource adapter");
        this.workManager = null;
    }

    /**
     * @see ResourceAdapter#endpointActivation(MessageEndpointFactory,ActivationSpec)
     */
    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec as) throws ResourceException {
        this.logger.info("Activating message endpoint with factory " + endpointFactory + " and activation spec " + as);
        if (as instanceof KismetActivationSpec) {
            // Validate the activation spec first
            KismetActivationSpec activationSpec = (KismetActivationSpec) as;
            activationSpec.validate();

            try {
                // Establish a new connection to the given kismet server
                KismetServerConnection connection = new KismetServerConnection(activationSpec, endpointFactory);
                this.workManager.scheduleWork(connection);
                this.connections.add(connection);
            }
            catch (Throwable cause) {
                throw new ResourceException("Failed to establish new connection to kismet server at " + activationSpec.getServerName() + " on port " + activationSpec.getPortNumber(), cause);
            }
        }
        else {
            throw new ResourceException("Expected " + KismetActivationSpec.class.getName() + ", but got " + as.getClass().getName());
        }
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#endpointDeactivation(MessageEndpointFactory,ActivationSpec)
     */
    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec as) {
        this.logger.info("Deactivating message endpoint with factory " + endpointFactory + " and activation spec " + as);
        for (Iterator<KismetServerConnection> it = this.connections.iterator(); it.hasNext(); ) {
            KismetServerConnection connection = it.next();
            if (connection.getActivationSpec().equals(as) && connection.getEndpointFactory().equals(endpointFactory)) {
                // Stop the connection and remove it from the list
                connection.release();
                it.remove();
                return;
            }
        }
    }

    /**
     * @see javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.ActivationSpec[])
     */
    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 11 + (this.workManager == null ? 0 : this.workManager.hashCode());
        hash = hash * 23 + this.connections.hashCode();
        return hash;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        else if (o instanceof KismetResourceAdapter) {
            KismetResourceAdapter ra = (KismetResourceAdapter) o;
            if ((this.workManager == null ? ra.workManager == null : this.workManager.equals(ra.workManager)) && this.connections.equals(ra.connections)) {
                return true;
            }
        }
        return false;
    }
}
