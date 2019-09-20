package org.aksw.dcat_suite.algebra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

class Hasher
	implements OpVisitor<String>
{
	protected Map<String, String> varToHash;

	public Hasher(Map<String, String> varToHash) {
		super();
		this.varToHash = varToHash;
	}
	
	public static Hasher create(Map<String, String> varToHash) {
		return new Hasher(varToHash);
	}

	@Override
	public String visit(OpCode op) {
		String result = computeHash(
				(op.isDecode() ? "decode" : "encode"),
				op.getSubOp().accept(this));
		return result;
	}

	@Override
	public String visit(OpConvert op) {
		String result = computeHash(
				op.getTargetContentType(),
				op.getSourceContentType(),
				op.getSubOp().accept(this));
		return result;		
	}

	@Override
	public String visit(OpVar op) {
		String varName = op.getName();
		String result = Objects.requireNonNull(varToHash.get(varName));
		return result;
	}

	@Override
	public String visit(OpValue op) {
		Object value = op.getValue();
		String result = computeHash("value", Objects.toString(value));
		return result;
	}

	public static String computeHash(String opName, String ...args) {
		String str = opName + "(" +
				Arrays.asList(args).stream().collect(Collectors.joining(", ")) + ")";
		
		ByteSource bs = ByteSource.wrap(str.getBytes(StandardCharsets.UTF_8));
		HashCode hashCode;
		try {
			hashCode = bs.hash(Hashing.sha256());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String result = hashCode.toString();
		return result;
	}
}