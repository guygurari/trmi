package trmi.test;

import java.lang.reflect.*;

public class BasicInvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) {
        called = true;
        return null;
    }

    public volatile boolean called = false;
}
