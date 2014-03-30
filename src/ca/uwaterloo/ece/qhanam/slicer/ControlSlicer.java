package ca.uwaterloo.ece.qhanam.slicer;

import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;

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

public class ControlSlicer extends AbstractCrystalMethodAnalysis
{
	private static final int SEED_LINE = 410;
	private static final String METHOD = "hashCode";
	private static final Direction DIRECTION = Direction.FORWARDS;
	
	private LinkedList<ASTNode> statements;
	
	public ControlSlicer() { 
		this.statements = new LinkedList<ASTNode>();
	}
	
	@Override
	public String getName() {
		return "CSlicer";
	}
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		HashSet<ICFGNode<ASTNode>> visited = new HashSet<ICFGNode<ASTNode>>();
		Queue<ICFGNode<ASTNode>> queue = new LinkedList<ICFGNode<ASTNode>>();
		
		/* Check that we are analyzing the correct method. */
		if(d.getName().toString().equals(METHOD))
		{
			System.out.println("Analyzing "  + d.getName());
			
			/* What if we are doing a control dependency analysis? 
			 * -> Use the CFG
			 */
			EclipseCFG cfg = new EclipseCFG(d);
			
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
			 */
			queue.add(cfgNode);
			int line = -1;
			while(queue.peek() != null){
				cfgNode = queue.remove();
				ASTNode astNode = (ASTNode) cfgNode.getASTNode();
				if(ControlSlicer.getStatement(astNode) == null) continue;
				line = ControlSlicer.getLineNumber(ControlSlicer.getStatement(astNode));
				System.out.println("Line " + line + " in the CFG.");
				if(line == this.SEED_LINE) break;
				Set<ICFGEdge<ASTNode>> neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getOutputs();
				for(ICFGEdge<ASTNode> edge : neighbours){						
					queue.add(edge.getSink());
				}
			}
			
			/* Check that we actually found a seed statement. */
			if(cfgNode == null) return;
			
			/* Build the control dependency slice. */
			queue.clear();
			queue.add(cfgNode);
			/* Breadth first search. Add each statement to the list when found. */
			while(queue.peek() != null){
				cfgNode = queue.remove();
				ASTNode astNode = (ASTNode) cfgNode.getASTNode();
				if(getStatement(astNode) != null) this.statements.add(ControlSlicer.getStatement(astNode));
				Set<ICFGEdge<ASTNode>> neighbours;
				
				if(this.DIRECTION == ControlSlicer.Direction.FORWARDS) neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getOutputs();
				else if(this.DIRECTION == ControlSlicer.Direction.BACKWARDS) neighbours = (Set<ICFGEdge<ASTNode>>) cfgNode.getInputs();
				else return;
				
				for(ICFGEdge<ASTNode> edge : neighbours){
					if(this.DIRECTION == ControlSlicer.Direction.FORWARDS && !visited.contains(edge.getSink())){
						if(ControlSlicer.getStatement(edge.getSink().getASTNode()) == null) continue;
						queue.add(edge.getSink());
						visited.add(edge.getSink());
					}
					else if(this.DIRECTION == ControlSlicer.Direction.BACKWARDS && !visited.contains(edge.getSource())){
						if(ControlSlicer.getStatement(edge.getSource().getASTNode()) == null) continue;
						queue.add(edge.getSource());
						visited.add(edge.getSource());
					}
				}
			}
		
			/* Print slice statements. */
			System.out.println("\nNodes in slice:");
			for(ASTNode node : statements){
				System.out.print(DataSlicer.getLineNumber(node) + ": " + node.toString());
			}
	
			System.out.println("Finished Analysis");
		}
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