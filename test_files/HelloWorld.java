
package ca.uwaterloo.ece.qhanam.slicer.test;


public class HelloWorld {
	private int a;
	public void doStuff(){
		a = 0;
		int b = 6;
		int c;
		for(int i = 0; i < b; i++){
			this.a = this.a + b + i;
			a = a + 7;
			c = b;
			c = b + i;
		}
		if(true){
			a = c;
		}
		else{
			a = b;
		}
	}

}
