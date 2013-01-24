package de.oscillation.kismet.connector;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Logger;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

import de.benediktmeurer.eui4j.EUI48;

/**
 * Represents a connection to a kismet server established by the kismet resource adapter.
 * 
 * @see KismetResourceAdapter
 * @author Benedikt Meurer
 */
public class KismetServerConnection implements Work {
    /** The kismet activation specification. */
    private final KismetActivationSpec activationSpec;

    /** The factory for the kismet message listeners. */
    private final MessageEndpointFactory endpointFactory;

    /** The logger. */
    private final Logger logger = getLogger(KismetServerConnection.class.getName());

    /** The kismet server connection socket. */
    private final Socket socket;

    /**
     * Constructs and initializes a new kismet server connection using the specified
     * <code>activationSpec</code> and <code>endpointFactory</code>.
     * 
     * @param activationSpec the kismet activation specification.
     * @param endpointFactory the message endpoint factory to handle the kismet messages.
     * @throws IOException in case of an I/O error.
     * @throws NullPointerException if either <code>activationSpec</code> or
     *             <code>endpointFactory</code> is <code>null</code>.
     * @throws UnknownHostException if the host specified by the <code>activationSpec</code> cannot
     *             be resolved.
     */
    public KismetServerConnection(KismetActivationSpec activationSpec, MessageEndpointFactory endpointFactory) throws UnknownHostException, IOException {
        if (endpointFactory == null) {
            throw new NullPointerException("endpointFactory must not be null");
        }
        this.activationSpec = activationSpec;
        this.endpointFactory = endpointFactory;
        this.socket = new Socket(activationSpec.getServerName(), activationSpec.getPortNumber());
        this.logger.info("Successfully established new kismet server connection to " + activationSpec.getServerName()
                         + " on port " + activationSpec.getPortNumber());
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            // Properly wrap the connection socket's input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            OutputStreamWriter out = new OutputStreamWriter(this.socket.getOutputStream());

            // Enable the CLISRC and SOURCE protocols
            this.logger.info("Enabling CLISRC and SOURCE protocols for kismet server connection");
            out.write("!1 ENABLE CLISRC *\n");
            out.write("!2 ENABLE SOURCE *\n");
            out.flush();

            // Process the incoming kismet messages (collecting drones and measured values)
            HashMap<UUID, String> droneNames = new HashMap<UUID, String>();
            LinkedList<KismetMeasuredValue> measuredValues = new LinkedList<KismetMeasuredValue>();
            for (;;) {
                StringTokenizer tokenizer = new StringTokenizer(in.readLine(), " ");
                String header = tokenizer.nextToken();
                if ("*CLISRC:".equals(header)) {
                    // Parse the CLISRC sentence
                    tokenizer.nextToken(); // BSSID
                    EUI48 deviceId = EUI48.fromString(tokenizer.nextToken());
                    UUID droneId = UUID.fromString(tokenizer.nextToken());
                    long timestamp = parseLong(tokenizer.nextToken());
                    tokenizer.nextToken(); // Packet count
                    int signalStrength = parseInt(tokenizer.nextToken());

                    // Collect the measured value
                    measuredValues.add(new KismetMeasuredValue(deviceId, droneId, signalStrength, timestamp));
                }
                else if ("*SOURCE:".equals(header)) {
                    // Parse the SOURCE sentence
                    String interf = tokenizer.nextToken();
                    String type = tokenizer.nextToken();
                    if ("drone".equals(interf) && "drone".equals(type)) {
                        // Extract the drone name and UUID and insert it into the drone mapping
                        String name = tokenizer.nextToken();
                        tokenizer.nextToken(); // Channel
                        UUID id = UUID.fromString(tokenizer.nextToken());
                        droneNames.put(id, name);
                    }
                }
                else if ("*TIME:".equals(header)) {
                    // Parse the TIME sentence
                    long timestamp = parseLong(tokenizer.nextToken());

                    try {
                        // Prepare to send the collected data as message using our configured
                        // message endpoint factory
                        MessageEndpoint messageEndpoint = this.endpointFactory.createEndpoint(null);
                        messageEndpoint.beforeDelivery(KismetMessageService.class.getDeclaredMethod("onMessage", KismetMessage.class));
                        try {
                            // Send the message to the endpoint
                            KismetMessage message = new KismetMessage(new HashMap<UUID, String>(droneNames), measuredValues, timestamp);
                            ((KismetMessageService) messageEndpoint).onMessage(message);

                            // Reset the measured values
                            measuredValues = new LinkedList<KismetMeasuredValue>();
                        }
                        finally {
                            messageEndpoint.afterDelivery();
                            messageEndpoint.release();
                        }
                    }
                    catch (Exception exn) {
                        this.logger.log(WARNING, "Failed to pass kismet message to endpoint", exn);
                    }
                }
            }
        }
        catch (Exception exn) {
            this.logger.log(SEVERE, "Error in kismet server connection, terminating connection", exn);
        }
    }

    /**
     * @see javax.resource.spi.work.Work#release()
     */
    @Override
    public void release() {
        try {
            // Try to close the connection socket
            this.socket.close();
        }
        catch (IOException exn) {
            this.logger.log(WARNING, "Failed to close kismet server connection", exn);
        }
    }

    /**
     * Returns the activationSpec of this <code>KismetServerConnection</code> object.
     * 
     * @return the activationSpec.
     */
    public KismetActivationSpec getActivationSpec() {
        return this.activationSpec;
    }

    /**
     * Returns the endpointFactory of this <code>KismetServerConnection</code> object.
     * 
     * @return the endpointFactory.
     */
    public MessageEndpointFactory getEndpointFactory() {
        return this.endpointFactory;
    }
}
