package problem_1;
import java.io.IOException;

abstract class Beverage {
	abstract void drink();
}

class Coffee extends Beverage {
	private int shot;
	private String name;

/* 필요한 요소를 추가해서 채워주세요 */
	public Coffee() {
	}
	public Coffee(int shot, String name) {
		this.shot = shot;
		this.name = name;
	}
	public void drink() {
		System.out.println("shot이 "+this.shot+"번 추가된 "+this.name+"를 마십니다.");
	}
}
class Tea extends Beverage {
/* 필요한 요소를 추가해서 채워주세요 */
	public Tea() {
	}
	public void drink() {
		System.out.println("Tea를 마십니다.");
	}
}

abstract class Person {
/* 필요한 요소를 추가해서 채워주세요 */
	abstract void setBeverage(Beverage beverage);
	abstract Beverage getBeverage();
	abstract void talk();
}
class Student extends Person {
/* 필요한 요소를 추가해서 채워주세요 */
	private String name;
	private Beverage beverage;
	public Student(String name) {
		this.name = name;
	}
	@Override
	public void setBeverage(Beverage beverage) {
		this.beverage = beverage;
	}
	@Override
	public Beverage getBeverage() {
		return this.beverage;
	}
	@Override
	public void talk() {
		System.out.println("저는 직업이 학생인 "+name+" 입니다.");
	}
}
class Professor extends Person {
/* 필요한 요소를 추가해서 채워주세요 */
	private String name;
	private Beverage beverage;
	public Professor(String name) {
		this.name = name;
	}
	@Override
	void setBeverage(Beverage beverage) {
		this.beverage = beverage;
	}
	@Override
	Beverage getBeverage() {
		return this.beverage;
	}
	@Override
	void talk() {
		System.out.println("저는 직업이 교수인 "+ name +" 입니다.");
	}
}
public class Main {
	
	public static void main(String[] args) throws IOException {
			Person person = new Student("김학생");
			Beverage beverage = new Coffee(2, "아메리카노");
			person.setBeverage(beverage);

			person.talk();  // 저는 직업이 학생인 김학생입니다.
			person.getBeverage().drink(); // shot이 2번 추가된 아메리카노를 마십니다.

			person = new Professor("박교수");
			person.talk(); // 저는 직업이 교수인 박교수입니다.

			beverage = new Tea();
			person.setBeverage(beverage); 
			person.getBeverage().drink(); // Tea를 마십니다.
	}
}