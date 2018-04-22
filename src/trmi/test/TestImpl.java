package trmi.test;

public class TestImpl implements TestIface {
	public Integer echo(Integer i) {
		return i;
	}

	public int echoPrimitive(int i) {
		return i;
	}

	public int getSum(int i, Short s, byte b) {
		return i + (int) s.shortValue() + (int) b;
	}

	public void throwAnException() throws AnException {
		throw new AnException();
	}

	public void throwARuntimeException() throws ARuntimeException {
		throw new ARuntimeException();
	}

    public void setValue(int val) {
        this.val = val;
    }

    public void setValueOn(TestIface obj, int val) {
        if (obj != null) {
            obj.setValue(val);
        }
    }

    public int getValue() {
        return val;
    }

    public TestIface getRemoteIface() {
        return new TestImpl();
    }

    public I1 getI1() {
        return new I1I2Impl();
    }

    public void castToI2(I1 i1) {
        I2 i2 = (I2) i1;
    }

    public void callRun(Runnable runnable) {
        runnable.run();
    }

    public NonSerializable returnNonSerializable() {
        return new NonSerializable();
    }

    public int val = -1;
}

