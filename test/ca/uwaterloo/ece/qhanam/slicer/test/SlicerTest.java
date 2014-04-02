package ca.uwaterloo.ece.qhanam.slicer.test;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.compiler.IProblem;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;

public class SlicerTest {
	
	private static final int SEED_LINE = 74;
	private static final String METHOD = "doStuff";
	private static final Slicer.Direction DIRECTION = Slicer.Direction.BACKWARDS;
	private static final Slicer.Type TYPE = Slicer.Type.DATA;

	/**
	 * Test the slicing tool.
	 * @param args
	 */
	public static void main(String[] args) {
		/* Begin Configurable Options ********** */
		String project = "ca.uwaterloo.ece.qhanam.slicer";
		String path = "/Users/qhanam/Documents/workspace_depanalysis/ca.uwaterloo.ece.qhanam.slicer/HelloWorld.java";
		List<Slicer.Options> options = new LinkedList<Slicer.Options>();
		options.add(Slicer.Options.ASSIGNMENT_ONLY);
		options.add(Slicer.Options.CONTROL_EXPRESSIONS);
		/* End Configurable Options ************ */
		
		String sourceCode;
		
		try{
			/* We need the source code in a string. */
			sourceCode = SlicerTest.getText(new File(path));
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(sourceCode.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		//parser.setProject(null); // Need to do this for bindings
		//parser.setUnitName("/src/path/to/java/file.java"); // Need to do this for bindings
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		/* Print any problems the compiler encountered. */
		IProblem[] problems = cu.getProblems();
		for(int i = 0; i < problems.length; i++){
			System.out.println(problems[i].getMessage());
			System.out.println(problems[i].getSourceLineNumber());
			System.out.println(problems[i].getOriginatingFileName());
		}
		
		MethodVisitor methodVisitor = new MethodVisitor(METHOD, SEED_LINE, DIRECTION, TYPE, options);
		cu.accept(methodVisitor);
	}
	
	/**
	 * Read the source code into a String.
	 * @param file
	 * @return
	 */
	public static String getText(File file) throws Exception
	{		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "";
		int c;

		while((c = reader.read()) != -1) line += (char) c;
		
		line = line.replace(";;", ";"); // Do we really need this?
		return line;
	}
}
