package hello;

import trmi.*;

/**
 * Client implementation that connects to the <code>HelloServer</code> and
 * repeatedly calls {@link Hello#hello()}.
 *
 * Synopsis:<br>
 * <code>java hello.HelloClient //hostname/objname</code>
 *
 * @author Guy Gur-Ari
 */
public class HelloClient {
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

		// Object lookup
		Hello hello = null;
				
		try {
			System.out.println("Looking up hello at " + name);
			hello = (Hello) trmi.Naming.lookup(name);
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Call hello()
		while (true) {
			// Look ma, no try block!
			System.out.println("Saying hello...");
			String response = hello.hello();
			System.out.println("Hello server replied: " + response + "\n");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {}
		}
	}

	private static void usage() {
		System.out.println("java " + HelloClient.class.getName()
				+ " <object name>\n");
	}
}

