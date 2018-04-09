package org.aksw.dcat.util.view;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.aksw.commons.collections.SinglePrefetchIterator;

import com.google.common.collect.Iterators;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;

public class SetFromCkanExtras
	extends AbstractSet<String>
{
	protected CkanDataset ckanDataset;
	protected String key;

	public SetFromCkanExtras(CkanDataset ckanDataset, String key) {
		super();
		this.ckanDataset = ckanDataset;
		this.key = key;
	}

	@Override
	public boolean add(String value) {
		boolean result = !contains(value);
		if(result) {
			List<CkanPair> extras = ckanDataset.getExtras();
			if(extras == null) {
				extras = new ArrayList<>();
				ckanDataset.setExtras(extras);
			}

			extras.add(new CkanPair(key, value));
		}
		
		return result;
	}

	@Override
	public Iterator<String> iterator() {
		Iterator<CkanPair> baseIt = Optional.ofNullable(ckanDataset.getExtras()).orElse(Collections.emptyList()).iterator();

		return new SinglePrefetchIterator<String>() {
			@Override
			protected String prefetch() throws Exception {
				CkanPair e = null;
				while(baseIt.hasNext()) {
					e = baseIt.next();
					if(e.getKey().equals(key)) {
						return e.getValue();
					}
				}
				return finish();
			}
			@Override
			public void remove() { baseIt.remove(); }
		};
	}

	@Override
	public int size() {
		return Iterators.size(iterator());
	}

}