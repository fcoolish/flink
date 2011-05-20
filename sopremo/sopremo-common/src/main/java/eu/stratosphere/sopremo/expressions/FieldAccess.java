package eu.stratosphere.sopremo.expressions;

import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import eu.stratosphere.sopremo.EvaluationContext;
import eu.stratosphere.sopremo.operator.StreamArray;
import eu.stratosphere.util.AbstractIterator;

public class FieldAccess extends EvaluableExpression {

	private String field;

	public FieldAccess(String field) {
		this.field = field;
	}

	@Override
	protected void toString(StringBuilder builder) {
		builder.append('.').append(this.field);
	}

	@Override
	public int hashCode() {
		return 43 + this.field.hashCode();
	}

	@Override
	public JsonNode evaluate(final JsonNode node, EvaluationContext context) {
		if (node.isArray()) {
			if (node instanceof StreamArray)
				return new StreamArray(new AbstractIterator<JsonNode>() {
					Iterator<JsonNode> children = node.iterator();

					@Override
					protected JsonNode loadNext() {
						if (!this.children.hasNext())
							return this.noMoreElements();
						return this.children.next().get(FieldAccess.this.field);
					}
				});
			// spread
			ArrayNode arrayNode = new ArrayNode(NODE_FACTORY);
			for (int index = 0, size = node.size(); index < size; index++)
				arrayNode.add(node.get(index).get(this.field));
			return arrayNode;
		}
		return node.get(this.field);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass())
			return false;
		return this.field.equals(((FieldAccess) obj).field);
	}
}