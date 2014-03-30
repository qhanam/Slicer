package ca.uwaterloo.ece.qhanam.slicer;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

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
	private DataSlicer.Direction direction;	// Indicates we are constructing a backwards slice
	private LinkedList<String> seedVariables;
	private LinkedList<ASTNode> statements;
	private Hashtable<Integer, LinkedList<String>> aliases;
	
	/**
	 * We get the seed line number, direction and list of aliases at each line as
	 * input from the Slicer class. The Slicer should have already calculated
	 * aliases in the forward direction for us.
	 * @param seedLine
	 * @param backwards
	 * @param aliases
	 */
	public SlicerVisitor(int seedLine, DataSlicer.Direction direction, Hashtable<Integer, LinkedList<String>> aliases, LinkedList<String> seedVariables){
		super();
		this.seedLine = seedLine;
		this.aliases = aliases;
		this.statements = new LinkedList<ASTNode>();
		this.seedVariables = seedVariables;
		this.direction = direction;
	}
	
	/**
	 * Returns the list of statements in the slice.
	 * @return
	 */
	public LinkedList<ASTNode> getSliceStatements(){
		return this.statements;
	}
	
	/**
	 * We need this for data dependency slicing.
	 * 
	 * TODO: We need to do the alias analysis beforehand in the forward direction.
	 */
	public boolean visitStatement(Statement node){
		/* Add this statement if:
		 * 	1. It is the seed statement.
		 * 	2. It contains a variable that is an alias of a seed statement variable
		 */
		
		/* Get the line number for the statement. */
		int line = DataSlicer.getLineNumber(node);
		
		if(line == this.seedLine){
			/* This is the seed statement. Add the statement to the slice. */
			this.statements.add(node);
		}
		else if(((this.direction == DataSlicer.Direction.FORWARDS & line > this.seedLine) || 
				(this.direction == DataSlicer.Direction.BACKWARDS & line < this.seedLine) ||
				(this.direction == DataSlicer.Direction.BOTH & line != this.seedLine))){
			/* If this statement contains an alias, add it to the list. */
			Integer start = new Integer(node.getStartPosition());

			if(this.aliases.containsKey(start)){
				for(String variable : this.aliases.get(start)){
					if(this.seedVariables.contains(variable))
						this.statements.add(node);
				}
			}
		}
		
		return true;
	}
	
	// TODO: This is annoying... but I can't think of a better way right now
	public boolean visit(AssertStatement node){return this.visitStatement(node);}
	public boolean visit(Block node){return this.visitStatement(node);}
	public boolean visit(BreakStatement node){return this.visitStatement(node);}
	public boolean visit(ConstructorInvocation node){return this.visitStatement(node);}
	public boolean visit(ContinueStatement node){return this.visitStatement(node);}
	public boolean visit(DoStatement node){return this.visitStatement(node);}
	public boolean visit(EmptyStatement node){return this.visitStatement(node);}
	public boolean visit(EnhancedForStatement node){return this.visitStatement(node);}
	public boolean visit(ExpressionStatement node){return this.visitStatement(node);}
	public boolean visit(ForStatement node){return this.visitStatement(node);}
	public boolean visit(IfStatement node){return this.visitStatement(node);}
	public boolean visit(LabeledStatement node){return this.visitStatement(node);}
	public boolean visit(ReturnStatement node){return this.visitStatement(node);}
	public boolean visit(SuperConstructorInvocation node){return this.visitStatement(node);}
	public boolean visit(SwitchCase node){return this.visitStatement(node);}
	public boolean visit(SwitchStatement node){return this.visitStatement(node);}
	public boolean visit(SynchronizedStatement node){return this.visitStatement(node);}
	public boolean visit(ThrowStatement node){return this.visitStatement(node);}
	public boolean visit(TryStatement node){return this.visitStatement(node);}
	public boolean visit(TypeDeclarationStatement node){return this.visitStatement(node);}
	public boolean visit(VariableDeclarationStatement node){return this.visitStatement(node);}
	public boolean visit(WhileStatement node){return this.visitStatement(node);}
}