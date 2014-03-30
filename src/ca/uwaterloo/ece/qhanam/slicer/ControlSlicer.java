package ca.uwaterloo.ece.qhanam.slicer;

import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Stack;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;

//import att.grappa.Graph;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;


import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.flow.FlowAnalysis;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.flow.SingletonLatticeElement;

import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFG;
import edu.cmu.cs.crystal.cfg.ICFGNode;
import edu.cmu.cs.crystal.cfg.ICFGEdge;

public class ControlSlicer
{
	private Direction direction;
	private LinkedList<Statement> statements;
	
	public ControlSlicer(Direction direction) { 
		this.statements = new LinkedList<Statement>();
		this.direction = direction;
	}

	public List<Statement> sliceMethod(MethodDeclaration d, int seedLine) {
		HashSet<ICFGNode<ASTNode>> visited = new HashSet<ICFGNode<ASTNode>>();
		Hashtable<Integer,Statement> statementPairs = new Hashtable<Integer,Statement>();
		Stack<ICFGNode<ASTNode>> stack = new Stack<ICFGNode<ASTNode>>();
		
		/* What if we are doing a control dependency analysis? 
		 * -> Use the CFG
		 */
		EclipseCFG cfg = new EclipseCFG(d);
		
		//cfg.getDotGraph()
		
		/* Once we have the start node, we can traverse the graph by finding
		 * the output edges and traversing them. For backwards slicing, we
		 * start at the end node, find the input edges and traverse them.
		 * 
		 * Actually, we don't need to go backwards if we're just searching
		 * for the seed statement... right?
		 */
		ICFGNode<ASTNode> cfgNode = null;
		cfgNode = cfg.getStartNode();
		
		/* So now we need to traverse the graph to find the seed statement. We
		 * need some way to label the seed path and log the ASTNodes. What if
		 * we first do a search for the seed node? Then we can use it as a
		 * starting point.
		 * 
		 * We do a depth-first search so we can see the proper order.
		 */
		stack.add(cfgNode);
		int line = -1;
		while(!stack.empty()){
			cfgNode = stack.pop();
			ASTNode astNode = (ASTNode) cfgNode.getASTNode();
			
			/* Check if this is the seed statement. */
			if(ControlSlicer.getStatement(astNode) != null){	
				line = ControlSlicer.getLineNumber(ControlSlicer.getStatement(astNode));
				//System.out.println("Line " + line + " in the CFG: " + cfgNode.toString());
				if(line == seedLine) break;
			}
			
			Set<ICFGEdge<ASTNode>> neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getOutputs();
			for(ICFGEdge<ASTNode> edge : neighbours){	
				if(!visited.contains(edge.getSink())){
					stack.push(edge.getSink());
					visited.add(edge.getSink());
				}
			}
		}
		
		/* Check that we actually found a seed statement. */
		if(cfgNode == null || line != seedLine){
			System.out.println("Seed statement not found.");
			return null;
		}
		
		/* Build the control dependency slice. */
		visited.clear();
		stack.clear();
		stack.add(cfgNode);
		/* Breadth first search. Add each statement to the list when found. */
		while(!stack.empty()){
			Set<ICFGEdge<ASTNode>> neighbours;
			
			cfgNode = stack.pop();
			ASTNode astNode = (ASTNode) cfgNode.getASTNode();
			Statement statement = getStatement(astNode);
			
			/* Add the statement to the slice if it isn't in yet. */
			if(statement != null && !statementPairs.containsKey(new Integer(statement.getStartPosition()))) 
				statementPairs.put(new Integer(statement.getStartPosition()), statement);
			
			if(this.direction == ControlSlicer.Direction.FORWARDS) neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getOutputs();
			else if(this.direction == ControlSlicer.Direction.BACKWARDS) neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getInputs();
			else return null;
			
			for(ICFGEdge<ASTNode> edge : neighbours){
				if(this.direction == ControlSlicer.Direction.FORWARDS && !visited.contains(edge.getSink())){
					stack.push(edge.getSink());
					visited.add(edge.getSink());
				}
				else if(this.direction == ControlSlicer.Direction.BACKWARDS && !visited.contains(edge.getSource())){
					stack.push(edge.getSource());
					visited.add(edge.getSource());
				}
			}
		}
		
		/* Add the statements to the list. */
		for(Statement statement : statementPairs.values()){
			this.statements.add(statement);
		}

		return this.statements;
	}
	
	/**
	 * Prints an ASTNode according to it's type.
	 */
	public static void printNode(ASTNode node){
		int line = ControlSlicer.getLineNumber(node);
		
		switch(node.getNodeType()){
			case ASTNode.VARIABLE_DECLARATION_STATEMENT:
				System.out.println("Line " +  line + ": " + node.toString());
				List<VariableDeclarationFragment> fragments = ((VariableDeclarationStatement) node).fragments();
				for(VariableDeclarationFragment fragment : fragments){
					System.out.println("\t" + fragment);
					System.out.println("\tVariable declared = " + fragment.getName());
				}
				break;
			default:
				System.out.println("Line " +  line + ": " + node.toString());
		}
	}
	
	/**
	 * Finds the statement that contains the ASTNode. Useful
	 * for finding the statement after we've found a variable.
	 */
	public static Statement getStatement(ASTNode statement){
		/* Visit parents until we get to a statement. Add the statement to the list. */
		if(statement == null) return null;
		while(!(statement instanceof Statement)){
			if(statement.getParent() == null) return null;
			statement = statement.getParent();
		}
		return (Statement) statement;
	}
	
	/**
	 * Finds the compilation unit and retrives the line number
	 * of the given node.
	 * @param node
	 * @return
	 */
	public static int getLineNumber(ASTNode node){
		/* This gives us the starting character position. We need to map
		 * this into the line number using:
		 * int org.eclipse.jdt.core.dom.CompilationUnit.getLineNumber(int position)
		 */
		int characterPosition = node.getStartPosition();
		int line = -1;
		
		/* Search for the compilation unit. */
		ASTNode current = node;
		do{
			current = current.getParent();
		} while(current.getParent() != null && node.getNodeType() != ASTNode.COMPILATION_UNIT);
		
		/* Have we found a compilation unit? */
		if(ASTNode.COMPILATION_UNIT == current.getNodeType()){
			CompilationUnit compUnit = (CompilationUnit) current;
			
			/* Now print the line number. */
			line = compUnit.getLineNumber(characterPosition);
		}
		return line;
	}
	
	public enum Direction {
	    BACKWARDS, FORWARDS, BOTH
	}
}
