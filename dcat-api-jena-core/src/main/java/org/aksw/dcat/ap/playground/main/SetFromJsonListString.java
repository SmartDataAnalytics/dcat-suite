package org.aksw.dcat.ap.playground.main;

import java.lang.reflect.Type;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.collections.SinglePrefetchIterator;

import com.google.common.collect.Iterators;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class SetFromJsonListString
	extends AbstractSet<Object>
{
	//protected PropertySource source;
	//protected String key;
	
	protected SingleValuedAccessor<String> accessor;
	protected Gson gson;
	protected boolean unsetIfEmpty;

	@SuppressWarnings("serial")
	protected final Type type = new TypeToken<List<Object>>() {}.getType();

	public SetFromJsonListString(SingleValuedAccessor<String> accessor, boolean unsetIfEmpty) {
		this(accessor, unsetIfEmpty, new Gson());
	}

	
	public SetFromJsonListString(SingleValuedAccessor<String> accessor, boolean unsetIfEmpty, Gson gson) {
		super();
		this.accessor = accessor;
		this.gson = gson;
		this.unsetIfEmpty = unsetIfEmpty;
	}


	@Override
	public boolean add(Object e) {
		List<Object> list = getUnderlyingList();
		boolean result = list.add(e);
		updateList(list);
		return result;
	}
	
	List<Object> getUnderlyingList() {
		String str = accessor.get();
		// Parse the string as a json list
		List<Object> items = gson.fromJson(str, type);

		items = items == null ? new ArrayList<>() : items;
		
		return items;
	}
	
	void updateList(List<Object> list) {
		if(list.isEmpty() && unsetIfEmpty) {
			accessor.set(null);
		} else {
			String newStr = gson.toJson(list);
			accessor.set(newStr);
		}
	}
	
	@Override
	public Iterator<Object> iterator() {
		return new SinglePrefetchIterator<Object>() {
			List<Object> list = getUnderlyingList();
			Iterator<Object> it = list.iterator();
			
			@Override
			protected Object prefetch() throws Exception {
				return it.hasNext() ? it.next() : finish();
			}
			
			@Override
			protected void doRemove(Object item) {
				// serialize the array without the item
				it.remove();
				updateList(list);
			}
		};
	}

	@Override
	public int size() {
		return Iterators.size(iterator());
	}

}
