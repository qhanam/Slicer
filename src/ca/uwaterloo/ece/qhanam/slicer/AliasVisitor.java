package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IBinding;


public class AliasVisitor extends ASTVisitor {
	
	private int seedLine;		// The line number of the seed statement
	private boolean backwards;	// Indicates we are constructing a backwards slice
	private LinkedList<String> aliases;	// TODO: this isn't really a suitable structure for this...
	
	public AliasVisitor(int seedLine, boolean backwards){
		super();
		this.seedLine = seedLine;
		this.backwards = backwards;
	}
	
	/**
	 * We need this for alias analysis.
	 */
	public boolean visit(Assignment node){
		
		if(node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME){
			/* Local variable being assigned. */
			SimpleName localVar = (SimpleName) node.getLeftHandSide();
			IBinding binding = localVar.resolveBinding();
			System.out.println("Assigning local variable " + binding.getKey());
		}
		else if(node.getLeftHandSide().getNodeType() == ASTNode.FIELD_ACCESS){
			/* Field being assigned. */
			FieldAccess fieldVar = (FieldAccess) node.getLeftHandSide();
			IVariableBinding binding = fieldVar.resolveFieldBinding();
			System.out.println("Assigning field: " + binding.getKey());
		}
		
		if(node.getRightHandSide().getNodeType() == ASTNode.SIMPLE_NAME){
			/* Local variable being read. */
			SimpleName localVar = (SimpleName) node.getLeftHandSide();
			IBinding binding = localVar.resolveBinding();
			System.out.println("Assigning local variable " + binding.getKey());
		}
		else if(node.getRightHandSide().getNodeType() == ASTNode.METHOD_INVOCATION){
			/* Method return value being read (ie. we don't know what's being returned). */
			MethodInvocation methodInv = (MethodInvocation) node.getRightHandSide();
			IMethodBinding binding = methodInv.resolveMethodBinding();
			System.out.println("Reading method invocation " + binding.getKey());
		}
		
		System.out.println("Left hand expression: " + node.getLeftHandSide().toString());
		System.out.println("Right hand expression: " + node.getRightHandSide().toString());
		return true;
	}
	
	/**
	 * We need this for data dependency slicing.
	 */
	public boolean visit(SimpleName node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveBinding();
		
		/* Make sure this is a variable. */
		if(binding instanceof IVariableBinding) System.out.println("Variable binding: " + binding.getKey());
		
		return true;
	}
	
//	public boolean visit(FieldAccess node){
//		/* If we're here then we're using a field. Return the field's key. 
//		 * We also need to know if one variable is being assigned to another 
//		 * 	for alias analysis... */
//		IVariableBinding binding = node.resolveFieldBinding();
//		System.out.println("FieldAccessBindingKey: " + binding.getKey());
//		return true;
//	}
//	
//	public boolean visit(QualifiedName node){
//		System.out.println("QualifiedNameVisitor: " + node.getFullyQualifiedName() + " - " + node.getParent().getNodeType());
//		return true;
//	}
//	

}
