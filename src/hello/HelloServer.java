package hello;

import trmi.*;

/**
 * A server that maintains an implementation of the {@link Hello} interface.
 * The implementation is exposed to remote clients.<p>
 *
 * Synopsis:<br>
 * <code>java hello.HelloServer //hostname/objname</code>
 *
 * @author Guy Gur-Ari
 */
public class HelloServer {
	public static void main(String[] args) {
		// Parse args
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		String name = args[0];

		// Setup the security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new java.rmi.RMISecurityManager());
		}

		System.out.println("Starting trmi hello server...");
		
		try {
			// Create the object and bind it
			Hello hello = new HelloImpl();
			Naming.rebind(name, hello, new Class[] {Hello.class});
			System.out.println("Hello object bound");
		}
		
		catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void usage() {
		System.out.println("java " + HelloServer.class.getName()
				+ " <object name>\n");
	}
}

