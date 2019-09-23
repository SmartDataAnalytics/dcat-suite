package org.aksw.dcat_suite.algebra;

public interface OpVisitor<T> {
	T visit(OpCode op);
	T visit(OpConvert op);
	T visit(OpValue op);
	T visit(OpPath op);
}