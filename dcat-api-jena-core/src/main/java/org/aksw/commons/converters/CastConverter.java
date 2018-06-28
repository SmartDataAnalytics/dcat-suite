package org.aksw.commons.converters;

import com.google.common.base.Converter;

public class CastConverter<I, O>
	extends Converter<I, O>
{
	@SuppressWarnings("unchecked")
	@Override
	protected O doForward(I a) {
		return (O)a;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected I doBackward(O b) {
		return (I)b;
	}

}
