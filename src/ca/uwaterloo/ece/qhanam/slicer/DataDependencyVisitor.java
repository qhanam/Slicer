package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Checks whether this ASTNode (Statement) contains a data dependency from
 * the given list.
 * @author qhanam
 */
public class DataDependencyVisitor extends ASTVisitor {
	
	/* The list of all possible variables and their aliases at this point in the CFG. */
	private LinkedList<String> aliases;
	
	/* We store the result of our analysis here. */
	public boolean result;
	
	/**
	 * Create DataDependencyVisitor
	 * @param 
	 */
	public DataDependencyVisitor(LinkedList<String> aliases){
		super();
		this.aliases = aliases;
		this.result = false;
	}
	
	/**
	 * The first thing we do is add the variable to the node aliases if it is
	 * present in a statement.
	 */
	public boolean visit(SimpleName node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveBinding();
		
		/* Make sure this is a variable.
		 * If we are just analyzing one source file,
		 * we won't have binding info... so do our 
		 * best effort at matching variables. */
		if(binding == null){
			if(!(node.getParent() instanceof MethodInvocation)){
				if(this.aliases.contains(node.getFullyQualifiedName()))
					this.result = true;
			}
		}
		else if(binding instanceof IVariableBinding){
			/* If this variable is in the alias list, then this statement 
			 * is a data dependency. */
			if(this.aliases.contains(binding.getKey())){
				this.result = true;
			}
		}
		
		return true;
	}
}
