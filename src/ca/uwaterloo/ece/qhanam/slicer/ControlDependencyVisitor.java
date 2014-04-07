package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * Checks whether this ASTNode (Statement) contains a data dependency from
 * the given list.
 * @author qhanam
 */
public class ControlDependencyVisitor extends DependencyVisitor {
	
	/* The slicer options. */
	private List<Slicer.Options> options;
	
	/* The seed statement. */
	private ASTNode seed;
	
	/**
	 * Create DataDependencyVisitor
	 * @param 
	 */
	public ControlDependencyVisitor(List<Slicer.Options> options, ASTNode seed){
		super();
		this.options = options;
		this.seed = seed;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(IfStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(WhileStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(DoStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(EnhancedForStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(ForStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * Check to see if the seed statement is contained within the body of 
	 * this conditional statement.
	 */
	public boolean visit(SwitchStatement node){
		this.isControlDependency(node);
		return false;
	}
	
	/**
	 * The seed statement is control dependent on another statement
	 * if that statement affects the whether or not the seed
	 * statement is executed. We therefore look at conditional
	 * statements that the seed is enclosed in as well as 
	 * conditional statements that contain return statements.
	 * 
	 * @param node The conditional statement.
	 * @return
	 */
	public void isControlDependency(ASTNode node){
		ReturnVisitor rv = new ReturnVisitor();
		
		/* If this conditional statement contains the seed statement, then
		 * it is a control dependency */
		if(ControlDependencyVisitor.contains(node, this.seed)){
			this.result = true;
			return;
		}

		/* If this conditional statement contains a return statement in its
		 * body, then it is a control dependency.
		 */
		node.accept(rv);
		this.result = rv.result;
	}
	
	/**
	 * Returns true if the node contains the seed statement.
	 * @param node An ASTNode (should have a body containing statements).
	 * @param seed The seed statement
	 * @return
	 */
	public static boolean contains(ASTNode node, ASTNode seed){
		int seedPosition = seed.getStartPosition();
		int start = node.getStartPosition();
		int end = start + node.getLength();
		
		/* If this conditional statement contains the seed statement, then
		 * it is a control dependency */
		if(seedPosition >= start & seedPosition <= end)
			return true;
		return false;
	}
	
	/**
	 * A class to find if there is a return statement within the
	 * expression being visited.
	 */
	private class ReturnVisitor extends DependencyVisitor {
		
		/**
		 * If there is a return statement somewhere in the body of the
		 * conditional statement, then this node is a control dependency.
		 */
		public boolean visit(ReturnStatement node){
			this.result = true;
			return false;
		}
	}
}
