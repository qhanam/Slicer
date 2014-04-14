package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * For now, we will sandbox MethodInvocation statements for retrieving
 * variables from the seed statements. MethodInvocations are a bit tricky
 * because we don't want to store methods... just object bindings or names.
 * @author qhanam
 *
 */
public class SeedMethodVisitor extends DependencyVisitor{
	
	List<String> seedVariables;
	
	public SeedMethodVisitor(List<String> seedVariables){
		this.seedVariables = seedVariables;
	}
	
	/**
	 * Add the variable to the node aliases.
	 */
	public boolean visit(QualifiedName node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			if(!seedVariables.contains(node.getFullyQualifiedName()))
				seedVariables.add(node.getFullyQualifiedName());
		}
		else if(binding instanceof IVariableBinding){
			if(!seedVariables.contains(binding.getKey()))
				seedVariables.add(binding.getKey());
		}
		
		return false;
	}
	
	/**
	 * Add the variable to the node aliases.
	 */
	public boolean visit(FieldAccess node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveFieldBinding();
		
		if(binding == null){
			if(!seedVariables.contains(node.getName().toString()))
				seedVariables.add(node.getName().toString());
		}
		else if(binding instanceof IVariableBinding){
			if(!seedVariables.contains(binding.getKey()))
				seedVariables.add(binding.getKey());
		}
		
		return false;
	}
	
	/**
	 * Add the variable to the node aliases.
	 */
	public boolean visit(SimpleName node){
		/* All we really need from this is the variable binding. */
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			if(node.getParent() instanceof MethodInvocation){
				MethodInvocation methodInv = (MethodInvocation) node.getParent();
				if(methodInv.getName().equals(node.getFullyQualifiedName())) return false;
			}
			if(!seedVariables.contains(node.getFullyQualifiedName()))
				seedVariables.add(node.getFullyQualifiedName());
		}
		else if(binding instanceof IVariableBinding){
			if(!seedVariables.contains(binding.getKey()))
				seedVariables.add(binding.getKey());
		}
		
		return false;
	}
	

}
