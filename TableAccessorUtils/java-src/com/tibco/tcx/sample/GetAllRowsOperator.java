//
// Copyright (c) 2004-2015 TIBCO Software Inc. All rights reserved.
//
package com.tibco.tcx.sample;

import java.util.ArrayList;
import java.util.List;

import com.streambase.sample.AbstractTableOperator;
import com.streambase.sb.CompleteDataType;
import com.streambase.sb.Schema;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sb.operator.Operator;
import com.streambase.sb.operator.Parameterizable;
import com.streambase.sb.operator.PreparedQuery;
import com.streambase.sb.operator.TableAccessor;
import com.streambase.sb.operator.TypecheckException;
import com.tibco.tcx.streambase.operator.TableAccessorUtils;

/**
 * This Java-based operators illustrates table querying using the {@link TableAccessor} and {@link PreparedQuery} classes.
 * Its "table" property is used to connect to the table of interest.
 * The single input port corresponds to the primary-key Tuple parameter of the
 * "delete" method.  The single output port is used to deliver the matched Tuples.
 * 
 * @see TableAccessor
 * @see Parameterizable
 * @see Operator
 * For in-depth information on implementing a custom Java Operator, please see
 * "Developing StreamBase Java Operators" in the StreamBase documentation.
 */
public class GetAllRowsOperator extends AbstractTableOperator implements Parameterizable {

	private static final long serialVersionUID = -211389479219308922L;

	/**
	 * The constructor is called when the QueryJavaOperator instance is created, but before the operator 
	 * is connected to the StreamBase application. The input port count is set to 1 and the output port count
	 * is set to 1.
	 */
	public GetAllRowsOperator() {
		super(1);
	}

	// not a pretty override, but don't want the PreparedQueryCache to build up with dead TableAccessors
	public void typecheck() throws TypecheckException {
		if (tableAccessor != null) TableAccessorUtils.removeTableAccessor(tableAccessor);
		super.typecheck();
	}
	
	/**
	 * This method performs typechecking that is specific to this subclass of {@link AbstractTableOperator}
	 * and is called by {@link AbstractTableOperator#typecheck()}.
	 */
	protected void typecheckInSubclass() throws TypecheckException {
		
		// set the output schema to be a list of tuples whose schema is the same as the schema of the table we're querying
		if (tableAccessor != null) {
			List<Schema.Field> fields=new ArrayList<Schema.Field>();
			fields.add(Schema.createListField("rows", new CompleteDataType.TupleType(tableAccessor.getSchema())));
			Schema getAllOutputSchema = new Schema("", fields );
			setOutputSchema(0, getAllOutputSchema);
		}
	}

	/**
	 * This method will be called by the StreamBase server for each Tuple given
	 * to the Operator to process. This is the only time an operator should 
	 * enqueue output Tuples.
	 * @param inputPort the input port that the tuple is from (ports are zero based)
	 * @param inputTuple the tuple from the given input port
	 * @throws StreamBaseException Terminates the application.
	 */
	public void processTuple(int inputPort, Tuple inputTuple) throws StreamBaseException {
			List<Tuple> l = TableAccessorUtils.getAllRows(tableAccessor);
			Tuple ot = getRuntimeOutputSchema(0).createTuple();
			ot.setList("rows", l);
			sendOutput (0, ot);
	}

}
