package org.aksw.commons.accessors;

public class SingleValuedAccessorDirect<T>
	implements SingleValuedAccessor<T>
{
	protected T value;
	
	
	public SingleValuedAccessorDirect() {
		this(null);
	}
	
	public SingleValuedAccessorDirect(T value) {
		super();
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public void set(T value) {
		this.value = value;
	}


}
