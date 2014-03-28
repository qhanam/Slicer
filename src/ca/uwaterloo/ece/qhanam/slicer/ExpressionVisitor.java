package ca.uwaterloo.ece.qhanam.slicer;

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


public class ExpressionVisitor extends ASTVisitor {
	/**
	 * Visit a generic ASTNode
	 */
	public boolean visit(ASTNode node){
		System.out.println("ExpressionVisitor: " + node.getNodeType());
		return true;
	}
	
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
//	public boolean visit(SimpleName node){
//		
//		/* If this is the child of a method call, don't consider it... maybe if we can get the return type?.
//		 * We can only do so much since we are doing an intra-procedural analysis. */
//		System.out.println("SimpleNameVisitorParent: " + node.getParent().toString());
//		System.out.println("SimpleNameVisitor: " + node.getFullyQualifiedName() + " - " + node.getParent().getNodeType());
//		return true;
//	}
}
