package trmi.test;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import junit.framework.*;
import trmi.*;

public class TRMITest extends TestCase {
	/* --- Management --- */

	public TRMITest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
        setupRegistry();

        try {
            // Array of remote objects
            impls = new TestImpl[NUM_OBJS];
            stubs = new TestIface[impls.length];

            for (int i = 0; i < impls.length; i++) {
                String name = NAME + String.valueOf(i);

                impls[i] = new TestImpl();
                bind(name, impls[i]);

                stubs[i] = (TestIface) Naming.lookup(name);
            }

            // A single remote object
            impl = new TestImpl();
            bind(NAME, impl);
            stub = (TestIface) Naming.lookup(NAME);
        } catch (RemoteException e) {
            System.out.println("Did you forget to run rmiregistry?");
            throw e;
        }
	}

	protected void tearDown() throws Exception {
		for (int i = 0; i < impls.length; i++) {
			stubs[i] = null;
			Naming.unbind(NAME + String.valueOf(i));
		}

		Naming.unbind(NAME);
		stub = null;
	}


	/* --- The Tests --- */

	public void testBasicFunctionality() throws Exception {
		for (int n = 0; n < 50; n++) {
			Integer expected = new Integer(rand.nextInt());
			Integer actual = stub.echo(expected);
			assertEquals(expected, actual);
		}
	}

	public void testPrimitiveFunctionality() throws Exception {
		for (int n = 0; n < 50; n++) {
			int expected = rand.nextInt();
			int actual = stub.echoPrimitive(expected);
			assertEquals(expected, actual);
		}
	}

	public void testComplexMethodInvocation() throws Exception {
		for (int n = 0; n < 50; n++) {
			assertComplexMethodInvocation(stub);
		}
	}

	public void testExceptionPropagation() throws Exception {
		try {
			stub.throwAnException();
			fail();
		} catch (AnException e) {}

		try {
			stub.throwARuntimeException();
			fail();
		} catch (ARuntimeException e) {}
	}

	public void testInvalidExposedInterfaces() throws Exception {
		try {
			Naming.rebind(
					NAME, 
					impl, 
					new Class[] {Runnable.class});
			fail();
		} catch (IllegalArgumentException e) {}
	}

	public void testMultipleObjects() throws Exception {
		for (int n = 0; n < 50; n++) {
			int idx = rand.nextInt(stubs.length);
			assertComplexMethodInvocation(stubs[idx]);
		}
	}

    public void testSerializableStub() throws Exception {
        for (int i = 0; i < stubs.length - 1; i++) {
            TestIface server = stubs[i];
            TestIface callback = stubs[i+1];

            int val = rand.nextInt(1000);
            server.setValueOn(callback, val);
            assertEquals(val, callback.getValue());
        }
    }

    public void testAutomaticStubCreation() throws Exception {
        TestIface server = stub;
        TestIface callback = new TestImpl();

		for (int n = 0; n < 50; n++) {
            int val = rand.nextInt(1000);
            server.setValueOn(callback, val);
            assertEquals(val, callback.getValue());

            server = callback;
            callback = new TestImpl();
        }
    }

    public void testAutomaticStubCreationOnReturn() throws Exception {
        TestIface server = stub;
        TestIface callback = server.getRemoteIface();

		for (int n = 0; n < 50; n++) {
            int val = rand.nextInt(1000);
            server.setValueOn(callback, val);
            assertEquals(val, callback.getValue());

            server = callback;
            callback = server.getRemoteIface();
        }
    }

    public void testNullParameters() throws Exception {
        TestIface server = stub;

		for (int n = 0; n < 50; n++) {
            int val = rand.nextInt(1000);
            server.setValueOn(null, val);
        }
    }

    public void testCastOfReturnedObjectToUndeclaredInterface() 
        throws Exception {

        TestIface server = stub;
        I1 i1 = server.getI1();
        I2 i2 = (I2) i1;
    }

    public void testCastOfParameterToUndeclaredInterface() throws Exception { 
        TestIface server = stub;
        I1 i1 = new I1I2Impl();
        server.castToI2(i1);
    }

    public void testRemoteObjectWrapperImplExposedInterfacesBug() 
        throws Exception {

        RemoteObjectWrapper wrapper = new RemoteObjectWrapperImpl(
                new I1I2Impl(),
                new Class[] {I2.class, I1.class});
    }


    public void testRemoteObjectWrapperImplExposedInterfacesBug2()
        throws Exception {

        RemoteObjectWrapper wrapper = new RemoteObjectWrapperImpl(
                new I1ChildImpl(),
                new Class[] {I1.class});
    }

    public void testRemoteObjectWrapperImplExposedInterfaceUpcast()
        throws Exception {

        try {
            RemoteObjectWrapper wrapper = new RemoteObjectWrapperImpl(
                    new I1I2Impl(),
                    new Class[] {I1Child.class});
            fail();
        }
        catch (IllegalArgumentException e) {}
    }

    public void testProxyAsWrappedObject() throws Exception {
        BasicInvocationHandler handler = new BasicInvocationHandler();

        Runnable runnable = (Runnable) Proxy.newProxyInstance(
                Runnable.class.getClassLoader(),
                new Class[] { Runnable.class },
                handler);

        TestIface server = stub;
        server.callRun(runnable);
        assertTrue(handler.called);
    }

    public void testProxyAsWrappedObjectWhereHandlerIsSerializable()
        throws Exception {

        SerializableInvocationHandler handler = 
            new SerializableInvocationHandler();

        Runnable runnable = (Runnable) Proxy.newProxyInstance(
                Runnable.class.getClassLoader(),
                new Class[] { Runnable.class },
                handler);

        TestIface server = stub;
        server.callRun(runnable);

        // Actual call cannot be verified because a serializable handler is
        // replicated and not called-back, but if no exception was thrown its
        // okay (this is the real test)
    }

    public void testNonSerializableReturnValue() throws Exception {
        try {
            stub.returnNonSerializable();
            fail();
        }
        catch (RuntimeException e) {
            Throwable origCause = getOriginalCause(e);
            assertTrue(origCause instanceof NotSerializableException);
        }
    }


	/* --- Utils --- */

    private void setupRegistry() throws RemoteException {
        // Best-effort default property values, assuming the tests are run on
        // localhost and use the current directory as the codebase.
        if (System.getProperty("java.rmi.server.codebase") == null) {
            System.setProperty(
                    "java.rmi.server.codebase", 
                    "file:///" + System.getProperty("user.dir") + "/");
        }

        if (System.getProperty("java.rmi.server.hostname") == null) {
            System.setProperty(
                    "java.rmi.server.hostname",
                    "localhost");
        }

        /*try {
            Registry registry = LocateRegistry.getRegistry();
        } catch (RemoteException e) {
            // Registry could not be located, creating local registry
            System.out.println("Creating local RMI registry...");
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }*/
    }

	private void assertComplexMethodInvocation(TestIface iface) {
		int i = rand.nextInt();
		short s = (short) rand.nextInt();
		byte b = (byte) rand.nextInt();

		int expected = i + (int) s + (int) b;
		int actual = iface.getSum(i, new Short(s), b);
		assertEquals(new Integer(expected), new Integer(actual));
	}

	private void bind(String name, TestImpl impl) throws Exception {
		Naming.rebind(name, impl, new Class[] {TestIface.class});
	}

    private Throwable getOriginalCause(Exception e) {
        Throwable result = e;

        while (result.getCause() != null) {
            result = result.getCause();
        }

        return result;
    }


	/* --- Members --- */

	private TestImpl impl;
	private TestIface stub;

	private TestImpl[] impls;
	private TestIface[] stubs;

	private static Random rand = new Random();

	private static final String NAME = "//localhost/trmi/test";
	private static final int NUM_OBJS = 10;
}

