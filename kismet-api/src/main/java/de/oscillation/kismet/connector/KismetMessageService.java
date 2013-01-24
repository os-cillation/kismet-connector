package de.oscillation.kismet.connector;

/**
 * The interface to the kismet message service beans, used to deliver incoming messages from the kismet server. It is
 * used to send messages from the kismet resource adapter, which handles the connection to the kismet server, to the
 * interested kismet message service beans. The messages implement the {@link KismetMessage} interface and are generated
 * on a regular basis by the kismet resource adapter from the data collected by the kismet server.
 * 
 * @author Benedikt Meurer
 * @see KismetMessage
 */
public interface KismetMessageService {
    /**
     * Invoked on reception of the <code>message</code> from the kismet resource adapter.
     * 
     * @param message the message from the kismet resource adapter.
     * @throws NullPointerException if <code>message</code> is <code>null</code>.
     * @see KismetMessage
     */
    public void onMessage(KismetMessage message);
}
