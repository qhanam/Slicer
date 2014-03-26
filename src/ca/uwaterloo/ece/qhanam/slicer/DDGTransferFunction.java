package ca.uwaterloo.ece.qhanam.slicer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationSummary;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.flow.ITransferFunction;
import edu.cmu.cs.crystal.flow.SingletonLatticeElement;
import edu.cmu.cs.crystal.flow.AnalysisDirection;

public class DDGTransferFunction implements ITransferFunction<TupleLatticeElement<Variable, SingletonLatticeElement>> 
{
	public TupleLatticeElement<Variable, SingletonLatticeElement> transfer(
			ASTNode arg0, TupleLatticeElement<Variable, SingletonLatticeElement> arg1) {
		// TODO Auto-generated method stub
		System.out.println("transfer");
		return null;
	}

	/**
	 * We are doing a backwards flow analysis.
	 */
	public AnalysisDirection getAnalysisDirection() {
		//return AnalysisDirection.BACKWARD_ANALYSIS;
		System.out.println("getAnalysisDirection");
		return AnalysisDirection.FORWARD_ANALYSIS;
	}

	public TupleLatticeElement<Variable, SingletonLatticeElement> createEntryValue(
			MethodDeclaration arg0) {
		// TODO Auto-generated method stub
		System.out.println("createEntryValue");
		return null;
	}

	public ILatticeOperations<TupleLatticeElement<Variable, SingletonLatticeElement>> getLatticeOperations() {
		// TODO Auto-generated method stub
		System.out.println("getLatticeOperations");
		return null;
	}
}

