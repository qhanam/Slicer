package ca.uwaterloo.ece.qhanam.slicer.plugin;

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

import ca.uwaterloo.ece.qhanam.slicer.ControlSlicer;
import ca.uwaterloo.ece.qhanam.slicer.DataSlicer;


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

public class CSlicer extends AbstractCrystalMethodAnalysis
{
	private static final int SEED_LINE = 406;
	private static final String METHOD = "hashCode";
	private static final ControlSlicer.Direction DIRECTION = ControlSlicer.Direction.FORWARDS;
	
	public CSlicer() { }
	
	@Override
	public String getName() {
		return "CSlicer";
	}
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		/* Check that we are analyzing the correct method. */
		if(d.getName().toString().equals(METHOD)){
			System.out.println("Starting Control Analysis");
			System.out.flush();
			
			ControlSlicer slicer = new ControlSlicer(DIRECTION);
			List<Statement> statements = slicer.sliceMethod(d, SEED_LINE);
		
			/* Print slice statements. */
			System.out.println("\nNodes in slice:");
			for(ASTNode node : statements){
				System.out.print(DataSlicer.getLineNumber(node) + ": " + node.toString());
			}
			
			System.out.println("Finished Control Analysis");	
			System.out.flush();
		}
	}
}
