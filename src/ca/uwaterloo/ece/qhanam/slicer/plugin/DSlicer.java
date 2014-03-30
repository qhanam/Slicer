package ca.uwaterloo.ece.qhanam.slicer.plugin;

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

public class DSlicer extends AbstractCrystalMethodAnalysis
{
	private static final int SEED_LINE = 406;
	private static final String METHOD = "hashCode";
	private static final DataSlicer.Direction DIRECTION = DataSlicer.Direction.BACKWARDS;
	
	public DSlicer() { }
	
	@Override
	public String getName() {
		return "DSlicer";
	}
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		/* Check that we are analyzing the correct method. */
		if(d.getName().toString().equals(METHOD)){
			System.out.println("Starting Data Analysis");
			System.out.flush();
			
			DataSlicer slicer = new DataSlicer(DIRECTION);
			List<Statement> statements = slicer.sliceMethod(d, SEED_LINE);
			
			/* Print slice statements. */
			System.out.println("\nNodes in slice:");
			for(ASTNode node : statements){
				System.out.print(DataSlicer.getLineNumber(node) + ": " + node.toString());
			}

			System.out.println("Finished Data Analysis");
			System.out.flush();
		}
		
	}
}
