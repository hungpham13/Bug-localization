import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class InitBinding {
  public static void main(String[]argv) {
    new A();
    new B();
    new C();
    X.verifyRun();
  }
}

class A {
@Colored(color="orange") public A() {}
}
class B {
@Colored(color="yellow") public B() {}
}
class C {
@Colored(color="brown") public C() {}
}

aspect X {
	 
	  // Expected color order
	  static String exp[] = new String[]{"orange","yellow","brown"};
	  
	  static int i = 0; // Count of advice executions
	  
	  before(Colored c):  preinitialization(new(..)) && !within(X) && @annotation(c)  {
	    System.err.println(thisJoinPoint+" color="+c.color());
	  	if (!c.color().equals(exp[i])) throw new RuntimeException("not "+exp[i]+"? "+c.color());
	  	i++;
	  }
	  
	  public static void verifyRun() {
	  	if (X.i != exp.length)
	  		throw new RuntimeException("Expected "+exp.length+" advice runs but did "+X.i);
	  }
	}


//aspect X {
//  int i = 0;
//  
//  before(Colored c): preinitialization(new(..)) && !within(X) && @annotation(c) {
//  	i++;
//        System.err.println(thisJoinPoint+" color="+c.color());
//  	if (i==1 && !c.color().equals("orange")) throw new RuntimeException("First time through should be red, but is "+c.color());
//  	if (i==2 && !c.color().equals("yellow")) throw new RuntimeException("Second time through should be green, but is "+c.color());
//  	if (i==3 && !c.color().equals("brown")) throw new RuntimeException("Third time through should be blue, but is "+c.color());
//  	System.err.println(c.color());
//  }
//}
//
