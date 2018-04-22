package hello;

/**
 * The 'remote' interface that is implemented on the server and invoked by the
 * client.
 *
 * @author Guy Gur-Ari
 */
public interface Hello {
	/**
	 * Returns a hello string.
	 */
	public String hello();
}

