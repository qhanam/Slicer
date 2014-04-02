package ca.uwaterloo.ece.qhanam.slicer;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class DependencyVisitor extends ASTVisitor {
	/* We store the result of our analysis here. */
	public boolean result;
	
	/**
	 * Initialize result to false (assume this statement
	 * is not a dependency).
	 */
	public DependencyVisitor(){
		super();
		this.result = false;
	}
}
