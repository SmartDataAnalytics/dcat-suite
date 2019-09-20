package org.aksw.dcat_suite.algebra;

import java.nio.file.Path;
import java.util.List;

import org.aksw.dcat_suite.server.conneg.HashSpace;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class OpUtils {
	public static int getNumOps(Op op) {
		
		// TODO We may want to exclude counting leaf nodes
		// as they do not require any operation
		// then again, a node with multiple children may require more time
		// than one with fewer
		/**
		 * Get the number of operations in the expression.
		 * Can be used as a poor-mans cost estimate
		 */
		int result = (int)Streams.stream(Traverser.forTree(Op::getSubOps)
			.depthFirstPreOrder(op))
			.count();
		
		return result;
	}
	
	
	public Op optimize(Op op, OpVisitor<String> hasher, HashSpace hashSpace) {
		String hash = op.accept(hasher);
		
		Path path = hashSpace.get(hash);
		Op result;
		if(path != null) {
			// In-place change the description of the op into a static reference
			// TODO This does not clear children of the op - so it leaves clutter
			// behind in the model which is not very aesthetic - then again, it is harmless
			op.removeProperties();			
			OpValue opValue = op.as(OpValue.class);
			opValue.setValue(path.toString());
		} else {
			List<Op> children = op.getSubOps();
			for(Op child : children) {
				optimize(op, hasher, hashSpace);
			}
		}
		
		return op;
	}
	
	
}