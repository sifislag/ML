package com.ibm.wala.cast.python.analysis;

import java.util.Map;

import com.ibm.wala.cast.python.types.TensorType;
import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.dataflow.graph.IKilldallFramework;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.propagation.PointsToSetVariable;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.graph.Graph;

public class TensorTypes extends DataflowSolver<PointsToSetVariable, TensorVariable> {
	
	private static IKilldallFramework<PointsToSetVariable, TensorVariable> createProblem(Graph<PointsToSetVariable> G) {
		return new IKilldallFramework<PointsToSetVariable, TensorVariable>() {

			UnaryOperator<TensorVariable> nodeOp = new UnaryOperator<TensorVariable>() {

				@Override
				public byte evaluate(TensorVariable lhs, TensorVariable rhs) {
					if (rhs != null && rhs.state != null) {
						if (lhs == null || lhs.state == null) {
							lhs.copyState(rhs);
							return CHANGED;
						} else {
							return lhs.state.addAll(rhs.state)? CHANGED: NOT_CHANGED;
						}
					} else {
						return NOT_CHANGED;
					}
				}

				@Override
				public int hashCode() {
					return 817504253;
				}

				@Override
				public boolean equals(Object o) {
					return o == this;
				}

				@Override
				public String toString() {
					return "propagate node tensor types";
				}
				
			};
			
			@Override
			public Graph<PointsToSetVariable> getFlowGraph() {
				return G;
			}

			@Override
			public ITransferFunctionProvider<PointsToSetVariable, TensorVariable> getTransferFunctionProvider() {
				return new ITransferFunctionProvider<PointsToSetVariable, TensorVariable>() {

					@Override
					public UnaryOperator<TensorVariable> getNodeTransferFunction(PointsToSetVariable node) {
						return nodeOp;
					}

					@Override
					public boolean hasNodeTransferFunctions() {
						return true;
					}

					@Override
					public UnaryOperator<TensorVariable> getEdgeTransferFunction(PointsToSetVariable src,
							PointsToSetVariable dst) {
						assert false;
						return null;
					}

					@Override
					public boolean hasEdgeTransferFunctions() {
						return false;
					}

					@Override
					public AbstractMeetOperator<TensorVariable> getMeetOperator() {
						return new AbstractMeetOperator<TensorVariable>() {

							@Override
							public byte evaluate(TensorVariable lhs, TensorVariable[] rhs) {
								boolean changed = false;
								for(TensorVariable r : rhs) {
									changed |= lhs.state.addAll(r.state);
								}
								return changed? CHANGED: NOT_CHANGED;
							}

							@Override
							public int hashCode() {
								return 413158523;
							}

							@Override
							public boolean equals(Object o) {
								return this == o;
							}

							@Override
							public String toString() {
								return "Tensor types set union";
							}
						};
					}		
				};
			}	
		};
	}
	
	private final Map<PointsToSetVariable,TensorType> init;
	
	public TensorTypes(Graph<PointsToSetVariable> G, Map<PointsToSetVariable,TensorType> init) {
		this(createProblem(G), init);
	}
	
	private TensorTypes(IKilldallFramework<PointsToSetVariable, TensorVariable> problem, Map<PointsToSetVariable,TensorType> init) {
		super(problem);
		this.init = init;
	}

	@Override
	protected TensorVariable makeNodeVariable(PointsToSetVariable n, boolean IN) {
		return new TensorVariable();
	}

	@Override
	protected TensorVariable makeEdgeVariable(PointsToSetVariable src, PointsToSetVariable dst) {
		assert false;
		return null;
	}

	@Override
	protected TensorVariable[] makeStmtRHS(int size) {
		return new TensorVariable[size];
	}

	@Override
	protected void initializeVariables() {
		super.initializeVariables();
		for(PointsToSetVariable src : init.keySet()) {
			getOut(src).state.add(init.get(src));
		}
	}


}
