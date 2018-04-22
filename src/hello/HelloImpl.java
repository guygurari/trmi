package hello;

/**
 * Implementation of the <code>Hello</code> interface.
 *
 * @author Guy Gur-Ari
 */
public class HelloImpl implements Hello {
	public String hello() {
		System.out.println("hello() called");
		return "Hi there";
	}
}

