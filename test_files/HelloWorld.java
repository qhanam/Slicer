
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
		a = 7;
	}
	
	public void doNothing(){
		a = 5;
		if(a > 0) return;
		a = 0;
		b = 0;
		c = 0;
		b = a + 1;
		c = b + 1;
	}

	public void nestedControl(){
		int a = 100;
		int b = 1;
		if(b > 0 & a > 0){
			while(a > 0){
				for(int i = 0; i < 10; i++){
					a =- b;
				}
			}
		}
	}
	
}
