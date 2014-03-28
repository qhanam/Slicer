package ca.uwaterloo.ece.qhanam.slicer;

import java.util.List;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
	private static final int SEED_LINE = 406;
	private static final String METHOD = "hashCode";
	
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
			
			/* First we do the alias analysis. */
			//MayAliasAnalysis aliasAnalysis = new MayAliasAnalysis(this.reporter, this.analysisInput);
			//aliasAnalysis.analyzeMethod(d);
			
			/* How do we find the seed statement? We need to fetch the line 
			 * number somehow. Are the AST nodes associated with a line number?
			 */
			
			/* Do this iteratively until we reach a fixed point. */
			for(int i = 0; i < 1; i++)
			{
				SlicerVisitor visitor = new SlicerVisitor(SEED_LINE, false);
				DDGTransferFunction tf = new DDGTransferFunction();
				flowAnalysis = new FlowAnalysis<TupleLatticeElement<Variable, SingletonLatticeElement>>(tf);
				d.accept(visitor);
				
				/* Fetch the slice statements. */
				LinkedList<ASTNode> statements = visitor.getSliceStatements();
				
				/* Print slice statements. */
				System.out.println("Nodes in slice:");
				for(ASTNode node : statements){
					Slicer.printNode(node);
				}
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
}
