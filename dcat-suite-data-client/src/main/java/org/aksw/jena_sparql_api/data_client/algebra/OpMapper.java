package org.aksw.jena_sparql_api.data_client.algebra;

public class OpMapper
	implements OpVisitor<Op>
{
	@Override
	public Op visit(OpModel op) {
		return null;
	}

	@Override
	public Op visit(OpConstruct op) {
		return null;
	}

	@Override
	public Op visit(OpUpdateRequest op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Op visit(OpUnion op) {
		// TODO Auto-generated method stub
		return null;
	}
}
