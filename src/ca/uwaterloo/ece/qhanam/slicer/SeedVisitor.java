package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Add any variables or fields that appear in the seed statement
 * to the list of variables that we need to check for.
 * @author qhanam
 */
public class SeedVisitor extends ASTVisitor {
	LinkedList<String> seedVariables;
	
	/**
	 * Create a SeedVisitor
	 * @param seedVariables The list that SeedVisitor will fill with the seed variables.
	 */
	public SeedVisitor(LinkedList<String> seedVariables){
		super();
		this.seedVariables = seedVariables;
	}
	
	/**
	 * The first thing we do is add the variable to the node aliases if it is
	 * present in a statement.
	 */
	public boolean visit(SimpleName node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveBinding();
		
		/* Make sure this is a variable. */
		if(binding instanceof IVariableBinding){
			seedVariables.add(binding.getKey());
		}
		
		return true;
	}
}
