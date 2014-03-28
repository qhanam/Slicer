package ca.uwaterloo.ece.qhanam.slicer;

import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;

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

import ca.uwaterloo.ece.qhanam.alias.MayAliasAnalysis;;

public class Slicer extends AbstractCrystalMethodAnalysis
{
	private static final int SEED_LINE = 410;
	private static final String METHOD = "hashCode";
	private static final Direction DIRECTION = Direction.BACKWARDS;
	
	FlowAnalysis<TupleLatticeElement<Variable, SingletonLatticeElement>> flowAnalysis;
	
	public Slicer() { }
	
	@Override
	public String getName() {
		return "DepAnalysis";
	}
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		/* Check that we are analyzing the correct method. */
		if(d.getName().toString().equals(METHOD))
		{
			System.out.println("Analyzing "  + d.getName());
			Hashtable<Integer, LinkedList<String>> aliases = new Hashtable<Integer, LinkedList<String>>();
			LinkedList<String> seedVariables = new LinkedList<String>();
			
			/* First we do the alias analysis. 
			 * NOTE: 
			 * 	This alias analysis has zero context sensitivity. We will improve it
			 * 	in the future by using flow analysis.*/
			
			/* This should populate the list with aliases. Iterate until we reach a fixed point. */
			System.out.println("\nPerforming alias analysis...");
			while(true){
				AliasVisitor aliasVisitor = new AliasVisitor(SEED_LINE, DIRECTION, aliases, seedVariables);
				d.accept(aliasVisitor);
				break;
			}
			
			System.out.println("\nCalculating slice...");
			SlicerVisitor visitor = new SlicerVisitor(SEED_LINE, DIRECTION, aliases, seedVariables);
			d.accept(visitor);
			
			/* Fetch the slice statements. */
			LinkedList<ASTNode> statements = visitor.getSliceStatements();
			
			/* Print slice statements. */
			System.out.println("\nNodes in slice:");
			for(ASTNode node : statements){
				System.out.print(Slicer.getLineNumber(node) + ": " + node.toString());
			}

			System.out.println("Finished Analysis");
		}
	}
	
	/**
	 * Prints an ASTNode according to it's type.
	 */
	public static void printNode(ASTNode node){
		int line = Slicer.getLineNumber(node);
		
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
		while(!(statement instanceof Statement || statement == statement.getRoot())){
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
