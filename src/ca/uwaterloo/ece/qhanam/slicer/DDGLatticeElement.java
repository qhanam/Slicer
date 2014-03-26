package ca.uwaterloo.ece.qhanam.slicer;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * To fetch data dependencies, we need to keep track of 
 * 	1. All object aliases.
 * 	2. All statements that may have modified the object.
 *
 * We find all aliases iteratively by adding aliases 
 * until we reach a fixed-point (ie.no more aliases are 
 * added to the set).
 * 
 * This class tracks the lattice elements for each node
 * in the AST. For data dependencies, we track the 
 * aliases of variables in the seed statement.
 */
public class DDGLatticeElement {
	private List<String> aliases;
	private List<ASTNode> statements;
	
	public DDGLatticeElement(){
		this.aliases = new LinkedList<String>();
		this.statements = new LinkedList<ASTNode>();
	}
	
	public DDGLatticeElement(List<String> aliases, List<ASTNode> statements){
		this.aliases = new ArrayList<String>(aliases);
		this.statements = new ArrayList<ASTNode>(statements);
	}
	
	public List<String> getAliases() {
		return this.aliases;
	}
	
	public List<ASTNode> getStatements(){
		return this.statements;
	}
}
