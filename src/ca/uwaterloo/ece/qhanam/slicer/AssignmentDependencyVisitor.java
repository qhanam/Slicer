package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;
import java.util.List;

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

/**
 * Checks whether this ASTNode (Statement) contains a data dependency from
 * the given list.
 * @author qhanam
 */
public class AssignmentDependencyVisitor extends DependencyVisitor {
	
	/* The list of all possible variables and their aliases at this point in the CFG. */
	protected LinkedList<String> aliases;
	
	/* The slicer options. */
	private List<Slicer.Options> options;
	
	/**
	 * Create DataDependencyVisitor
	 * @param 
	 */
	public AssignmentDependencyVisitor(LinkedList<String> aliases, List<Slicer.Options> options){
		super();
		this.aliases = aliases;
		this.options = options;
	}
	
	/**
	 * This is a data dependency if this is a declaration and the
	 * variable being declared is in the right hand side of the
	 * seed assignment expression.
	 */
	public boolean visit(SingleVariableDeclaration node){
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			if(this.aliases.contains((node.getName().getFullyQualifiedName())))
				this.result = true;
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
	
	/**
	 * This is a data dependency if this is a declaration and the
	 * variable being declared is in the right hand side of the
	 * seed assignment expression.
	 */
	public boolean visit(VariableDeclarationFragment node){
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			if(this.aliases.contains((node.getName().getFullyQualifiedName())))
				this.result = true;
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
	
	/**
	 * This is a data dependency if this is an assignment and the
	 * variable being assigned is in the right hand side of the
	 * seed assignment expression.
	 */
	public boolean visit(Assignment node){
		Expression lhs = node.getLeftHandSide();
		if(lhs instanceof FieldAccess){
			/* All we really need from this is the variable binding. */
			IBinding binding = ((FieldAccess) lhs).resolveFieldBinding();
			
			/* Make sure this is a variable.
			 * If we are just analyzing one source file,
			 * we won't have binding info... so do our 
			 * best effort at matching variables. */
			if(binding == null){
				NoBindingsVisitor nbv = new NoBindingsVisitor(this.aliases);
				lhs.accept(nbv);
				this.result = nbv.result;
			}
			else if(binding instanceof IVariableBinding){
				/* If this variable is in the alias list, then this statement 
				 * is a data dependency. */
				if(this.aliases.contains(binding.getKey())){
					this.result = true;
				}
			}
		}
		else if(lhs instanceof QualifiedName){
			/* All we really need from this is the variable binding. */
			IBinding binding = ((QualifiedName)lhs).resolveBinding();
			
			/* Make sure this is a variable.
			 * If we are just analyzing one source file,
			 * we won't have binding info... so do our 
			 * best effort at matching variables. */
			if(binding == null){
				NoBindingsVisitor nbv = new NoBindingsVisitor(this.aliases);
				lhs.accept(nbv);
				this.result = nbv.result;
			}
			else if(binding instanceof IVariableBinding){
				/* If this variable is in the alias list, then this statement 
				 * is a data dependency. */
				if(this.aliases.contains(binding.getKey())){
					this.result = true;
				}
			}
		}
		else if(lhs instanceof SimpleName){
			/* All we really need from this is the variable binding. */
			IBinding binding = ((SimpleName)lhs).resolveBinding();
			
			/* Make sure this is a variable.
			 * If we are just analyzing one source file,
			 * we won't have binding info... so do our 
			 * best effort at matching variables. */
			if(binding == null){
				NoBindingsVisitor nbv = new NoBindingsVisitor(this.aliases);
				lhs.accept(nbv);
				this.result = nbv.result;
			}
			else if(binding instanceof IVariableBinding){
				/* If this variable is in the alias list, then this statement 
				 * is a data dependency. */
				if(this.aliases.contains(binding.getKey())){
					this.result = true;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(IfStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	
	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(DoStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	
	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(EnhancedForStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	
	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(ForStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			List<Expression> initializers = node.initializers();
			for(Expression initializer : initializers){
				initializer.accept(this);
			}
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}

	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(SwitchStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	

	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(SynchronizedStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	
	/**
	 * We don't really need anything in particular from this statement,
	 * but since it has an expression and a body, we only want to 
	 * investigate the expression part to determine if it needs to
	 * be in the slice.
	 */
	public boolean visit(WhileStatement node){
		if(this.options.contains(Slicer.Options.CONTROL_EXPRESSIONS)){
			/* Visit the expression part. */
			node.getExpression().accept(this);
			/* Don't visit the children. */
			return false;
		}
		else return true;
	}
	
	/**
	 * A class to find matching fields/variables in expressions when
	 * we don't have field/variable bindings.
	 */
	private class NoBindingsVisitor extends DependencyVisitor {
		
		/* The list of all possible variables and their aliases at this point in the CFG. */
		private LinkedList<String> aliases;
		
		/**
		 * Create DataDependencyVisitor
		 * @param 
		 */
		public NoBindingsVisitor(LinkedList<String> aliases){
			super();
			this.aliases = aliases;
		}
		
		/**
		 * Check if this node is a variable or field name in the alias list.
		 */
		public boolean visit(SimpleName node){
			if(!(node.getParent() instanceof MethodInvocation)){
				if(this.aliases.contains(node.getFullyQualifiedName())){
					this.result = true;	// We found a match. Set the parent's result to true.
					return false; // We no longer need to visit the children.
				}
			}
			return true;
		}
		
		/**
		 * Check if this node is a variable or field name in the alias list.
		 */
		public boolean visit(QualifiedName node){
			if(!(node.getParent() instanceof MethodInvocation)){
				if(this.aliases.contains(node.getFullyQualifiedName())){
					this.result = true;	// We found a match. Set the parent's result to true.
					return false; // We no longer need to visit the children.
				}
			}
			return true;
		}
		
		/**
		 * Check if this node is a variable or field name in the alias list.
		 */
		public boolean visit(FieldAccess node){
			if(this.aliases.contains(node.getName())){
				this.result = true;	// We found a match. Set the parent's result to true.
				return false; // We no longer need to visit the children.
			}
			return true;
		}
	}
}
