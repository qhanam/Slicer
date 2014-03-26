package ca.uwaterloo.ece.qhanam.slicer;

import java.util.LinkedList;
import java.util.List;

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

/**
 * The visitor functions for each statement type go here.
 * 
 * How do we only start our analysis when we reach the line number?
 * 	- In every node we visit, check that the line number is >= the seed
 * 	(for forward flow) or <= the seed (for backward flow).
 * 	- But how do we know the initial statement? Initialize SlicerVisitor
 * 	with it maybe?
 * How do we get the initial list of variables?
 * 	- When the statement being visited == the seed line, we start
 * 	our analysis by adding the statement to the slice statement
 * 	list.
 * @author qhanam
 *
 */
public class SlicerVisitor extends ASTVisitor
{
	private int seedLine;		// The line number of the seed statement
	private boolean backwards;	// Indicates we are constructing a backwards slice
	private LinkedList<ASTNode> statements;
	private LinkedList<String> aliases;
	
	public SlicerVisitor(int seedLine, boolean backwards){
		super();
		this.seedLine = seedLine;
		this.statements = new LinkedList<ASTNode>();
		this.aliases = new LinkedList<String>();
	}
	
	/**
	 * Returns the list of statements in the slice.
	 * @return
	 */
	public LinkedList<ASTNode> getSliceStatements(){
		return this.statements;
	}
	
	/**
	 * Returns the list of aliases 
	 * @return
	 */
	public LinkedList<String> getAliasList(){
		return this.aliases;
	}
	
	/** 
	 * Returns the children of the ASTNode. 
	 */
	private Object[] getChildren(ASTNode node) {
	    List list = node.structuralPropertiesForType();
	    for (int i= 0; i < list.size(); i++) {
	        StructuralPropertyDescriptor curr= (StructuralPropertyDescriptor) list.get(i);
	        Object child= node.getStructuralProperty(curr);
	        System.out.println(child.toString());
	        
	        if (child instanceof List) {
	        	System.out.println("LIST IN PROPERTY DESCRIPTOR");
	        	List childList = (List) child;
	        	for(Object o : childList){
	        		if(o instanceof ChildListPropertyDescriptor) System.out.println("CHILD LIST PROPERTY DESCRIPTOR");
	        	}
                return ((List) child).toArray();
	        } else if (child instanceof ASTNode) {
	            return new Object[] { child };
	        }
	        return new Object[0];
	    }
	    return null;
	}
	
	/**
	 * Returns a list of variable names used in the ASTNode
	 * @param node
	 * @return
	 */
	private List<SimpleName> getVariableNames(ASTNode node){
		List<SimpleName> names = new LinkedList<SimpleName>();
		
		switch(node.getNodeType()){
			case ASTNode.ASSIGNMENT:
				Assignment a = (Assignment) node;
				// Need to get all nodes of type Name (QualifiedName and SimpleName)
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT:
				VariableDeclarationStatement vds = (VariableDeclarationStatement) node;
				List<VariableDeclarationFragment> fragments = vds.fragments();
				
				for(VariableDeclarationFragment fragment : fragments){
					/* TODO: If a tracked variable is on the right, add the variable on the left to the tracked list. */
					names.add(fragment.getName());	// Add the declared variable to the alias list. Still need to add the right side...
				}
				break;
			default:
				break;
		}
		return null;
	}
	
	/**
	 * Traverses the children of the ASTNode and finds all the
	 * names of children of type SIMPLE_NAME.
	 * @param node
	 * @return
	 */
	private LinkedList<String> getSimpleNames(ASTNode node)
	{
		LinkedList<String> simpleNames = new LinkedList<String>();
		Object[] children = this.getChildren(node);
		
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof ASTNode){
				ASTNode child = (ASTNode) children[i];
				if(child.getNodeType() == ASTNode.SIMPLE_NAME){
					SimpleName simpleName = (SimpleName) child;
					simpleNames.add(simpleName.getIdentifier());
				}
			}
		}
		return simpleNames;
	}
	
	/**
	 * Adds the statement to the slice if it contains an alias. Also
	 * adds new aliases if necessary.
	 * @param node
	 */
	private void checkStatement(ASTNode node){
		List<String> simpleNames = this.getSimpleNames(node);
		
		/* Check the aliases against the names in this statement. */
		for(String simpleName : simpleNames){
			if(this.aliases.contains(simpleName)){
				/* Add this statement to the statement list. */
				this.statements.add(node);
				
				/* Add the SimpleNames to the alias list. */
				for(String alias : simpleNames){
					if(!this.aliases.contains(alias)){
						this.aliases.add(alias);
					}
				}
			}
		}
	}
	
	/**
	 * Adds the seed statement when first encountered. Initializes
	 * the alias list.
	 * @param node
	 */
	private void addSeedStatement(ASTNode node){
		List<String> simpleNames = this.getSimpleNames(node);
		
		/* Add the seed statement to the statement list. */
		this.statements.add(node);
		
		/* Add the aliases. */
		for(String simpleName : simpleNames){
			this.aliases.add(simpleName);
		}
	}
	
	/**
	 * Visit an assignment node.
	 * eg. name = "Billy";
	 */
	public boolean visit(Assignment node){
		System.out.println("Assignment Node");
		int line = Slicer.getLineNumber(node);
		if(line == this.seedLine){
			/* TODO: Add the line to the slice. */
			this.addSeedStatement(node);
		}
		else if((this.backwards && line < this.seedLine) ||
				(!this.backwards && line > this.seedLine)) {
			/* TODO: Add the line to the slice if it contains
			 * a variable that is an alias of a variable in the seed 
			 * at this point in the AST. No matter if we are doing
			 * a forwards or backwards analysis, we still look at
			 * the forwards alias analysis for determining the list
			 * of aliases. */
			
			this.checkStatement(node);
		}
		return true;
	}
	
	/**
	 * Visit an initializer node.
	 * eg. int i = 0;
	 */
	public boolean visit(Initializer node){
		System.out.println("Initializer Node");
		int line = Slicer.getLineNumber(node);
		if(line == this.seedLine){
			/* TODO: Add the line to the slice. */
			this.addSeedStatement(node);
		}
		else if((this.backwards && line < this.seedLine) ||
				(!this.backwards && line > this.seedLine)) {
			/* TODO: Add the line to the slice if it contains
			 * a variable that is an alias of a variable in the seed 
			 * at this point in the AST. No matter if we are doing
			 * a forwards or backwards analysis, we still look at
			 * the forwards alias analysis for determining the list
			 * of aliases. */
			this.checkStatement(node);
		}
		return true;
	}
	
	/**
	 * Visit a variable declaration statement.
	 */
	public boolean visit(VariableDeclarationStatement node){
		System.out.println("Variable Declaration Node");
		int line = Slicer.getLineNumber(node);
		if(line == this.seedLine){
			/* TODO: Add the line to the slice. */
			this.addSeedStatement(node);
			
			List<VariableDeclarationFragment> fragments = node.fragments();
			for(VariableDeclarationFragment fragment : fragments){
				/* TODO: If a tracked variable is on the right, add the variable on the left to the tracked list. */
			}
		}
		else if((this.backwards && line < this.seedLine) ||
				(!this.backwards && line > this.seedLine)) {
			/* TODO: Add the line to the slice if it contains
			 * a variable that is an alias of a variable in the seed 
			 * at this point in the AST. No matter if we are doing
			 * a forwards or backwards analysis, we still look at
			 * the forwards alias analysis for determining the list
			 * of aliases. */
			this.checkStatement(node);
			
			List<VariableDeclarationFragment> fragments = node.fragments();
			for(VariableDeclarationFragment fragment : fragments){
				/* TODO: If a tracked variable is on the right, add the variable on the left to the tracked list. */
			}
		}
		return true;
	}
	
	public boolean visit(QualifiedName node){
		//Slicer.printNode(node);
		return true;
	}
	
	public boolean visit(SimpleName node){
		//Slicer.printNode(node);
		return true;
	}
	
//	/**
//	 * Visit a method declaration statement.
//	 */
//	public boolean visit(MethodDeclaration node){
//		System.out.print(node);
//		return true;
//	}
	
//	/**
//	 * Visit a variable declaration statement.
//	 */
//	public boolean visit(VariableDeclarationExpression node){
//		System.out.println(node);
//		return true;
//	}
	
//	/**
//	 * Visit a simple name statement.
//	 */
//	public boolean visit(SimpleName node){
//		System.out.println(node);
//		return true;
//	}
	
}