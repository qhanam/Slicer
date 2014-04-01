package ca.uwaterloo.ece.qhanam.slicer.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;

/**
 * Visits the method components of a java file.
 * @author qhanam
 *
 */
public class MethodVisitor extends ASTVisitor
{	
	private String methodName;
	private int seedLine;
	private Slicer.Direction direction;
	private Slicer.Type type;
	
	public MethodVisitor(String methodName, int seedLine, Slicer.Direction direction, Slicer.Type type){
		this.methodName = methodName;
		this.seedLine = seedLine;
		this.direction = direction;
		this.type = type;
	}
	
	@Override
	public boolean visit(CompilationUnit node){
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		/* If this is the method we want to analyze,
		 * call the analysis. */
		if(node.getName().toString().equals(this.methodName)){
			Slicer slicer = new Slicer(direction, type);
			List<Statement> statements = slicer.sliceMethod(node, seedLine);
			
			System.out.println("Slice Results:");
			for(Statement statement : statements){
				System.out.println(statement.toString());
			}
		}
		
		return true;
	}
}
