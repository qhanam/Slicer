package ca.uwaterloo.ece.qhanam.slicer.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;
import junit.framework.TestCase;

public class TestSlicer extends TestCase {
	
	@Test
	public void testDataDeps(){
		runTest("test_files/Test1.java", "drawLine", 7,
				new int[]{6}, new int[]{4,2});
	}
	
	@Test
	public void testControlDeps(){
		runTest("test_files/GC.java", "drawString", 2112,
				new int[]{2055,2059}, new int[]{2030});
	}
	
	@Test
	public void testIfWhileNestedConditionals(){
		runTest("test_files/Test2.java", "getLaunchConfigurations", 11,
				new int[]{10,6}, new int[]{7,4});
	}
	
	@Test
	public void testWhileLoopSeed(){
		runTest("test_files/Test2.java", "getLaunchConfigurations", 6,
				new int[]{}, new int[]{3});
	}
	
	@Test
	public void testNoExpressionMethod(){
		runTest("test_files/drawString-1.java", "drawString", 10,
				new int[]{9,6}, new int[]{2});
	}
	
	@Test
	public void testAdditionAssignment(){
		runTest("test_files/Scrollable.java", "computeTrim", 7,
				new int[]{}, new int[]{4,5});
	}
	
	/**
	 * Tests the control and data dependency slices.
	 * @param path
	 * @param method
	 * @param seed
	 * @param controlExpected
	 * @param dataExpected
	 */
	public void runTest(String path, String method, int seed, int[] controlExpected, int[] dataExpected){
		List<ASTNode> controlActual = getControlSlice(path, method, seed);
		List<ASTNode> dataActual = getDataSlice(path, method, seed);
		checkSlice(controlActual, controlExpected);
		checkSlice(dataActual, dataExpected);
	}
	
	/**
	 * Checks the slice against the expected output.
	 * @param slice
	 * @param expected
	 */
	public void checkSlice(List<ASTNode> slice, int[] expected){
		int i = 0;
		int[] actual = new int[slice.size()];
		for(ASTNode statement : slice){
			actual[i] = Slicer.getLineNumber(statement);
			i++;
		}
		Arrays.sort(actual);
		Arrays.sort(expected);
		assertArrayEquals(expected, actual);
	}
	
	/**
	 * Generate a control dependency slice.
	 * @param path
	 * @param method
	 * @param seedLine
	 */
	public List<ASTNode> getControlSlice(String path, String method, int seedLine){
		CompilationUnit cu = SampleUse.getAST(path);
		List<Slicer.Options> options;
		MethodVisitor methodVisitor;

		options = new LinkedList<Slicer.Options>();
		options.add(Slicer.Options.OMIT_SEED);
		methodVisitor = new MethodVisitor(method, seedLine, Slicer.Direction.BACKWARDS, Slicer.Type.CONTROL, options);
		cu.accept(methodVisitor);
		
		return methodVisitor.slice;
	}
	
	/**
	 * Generate a data dependency slice.
	 * @param path
	 * @param method
	 * @param seedLine
	 */
	public List<ASTNode> getDataSlice(String path, String method, int seedLine){
		CompilationUnit cu = SampleUse.getAST(path);
		List<Slicer.Options> options;
		MethodVisitor methodVisitor;

		options = new LinkedList<Slicer.Options>();
		options.add(Slicer.Options.CONTROL_EXPRESSIONS);
		options.add(Slicer.Options.OMIT_SEED);
		
		methodVisitor = new MethodVisitor(method, seedLine, Slicer.Direction.BACKWARDS, Slicer.Type.DATA, options);
		cu.accept(methodVisitor);
		
		return methodVisitor.slice;
	}
}
