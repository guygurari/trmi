package trmi.test;

public interface TestIface {
	public Integer echo(Integer i);
	public int echoPrimitive(int i);

	public int getSum(int i, Short s, byte b);

	public void throwAnException() throws AnException;
	public void throwARuntimeException() throws ARuntimeException;

    public void setValue(int val);
    public void setValueOn(TestIface obj, int val);
    public int getValue();

    public void callRun(Runnable runnable);
    public NonSerializable returnNonSerializable();
    
    public TestIface getRemoteIface();

    public I1 getI1();
    public void castToI2(I1 i1);
}
