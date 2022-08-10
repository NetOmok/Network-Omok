package stack_array;

/***
  
 배열을 이용한 정수를 담는 스택 구현하기
 
 ***/
interface Stack_Function{
	boolean isEmpty();
	boolean isFull();
	void push(int num);
	int pop();
	int peek();
	int size();
}

public class Stack implements Stack_Function {

	private int top;
	private int stack_arr[];
	private int stackSize;
	
	public Stack(int stackSize) {
		top = -1;
		this.stackSize = stackSize;
		stack_arr = new int[this.stackSize];
	}
	
	@Override
	public boolean isEmpty() {
		// 비어있을 경우 true, 아닐경우 false
		return (top == -1);
	}

	@Override
	public boolean isFull() {
		// 99번 인덱스까지 차있을 경우 true, 아닐경우 false
		return (top == 99);
	}

	@Override
	public void push(int num) {
		if(isFull()) {
			System.out.println("Stack Overflow");
		}
		else {
			stack_arr[++top] = num;
			System.out.println(num+" push 완료");
		}
	}

	@Override
	public int pop() {
		if(isEmpty()) {
			System.out.println("Stack Unerflow");
			return 0;
		}
		else {
			System.out.println(stack_arr[top]+" pop 완료");
			return stack_arr[top--];
		}
	}

	@Override
	public int peek() {
		if(isEmpty()) {
			System.out.println("Stack Unerflow");
			return 0;
		}
		else {
			System.out.println(stack_arr[top] + " peek 완료");
			return stack_arr[top];
		}
	}	
	@Override
	public int size() {
		return (top+1);
	}
	
	public static void main(String[] args) {
		try {
		Stack stack = new Stack(10);
		stack.pop(); // isFull 기능 체크
		stack.push(10); // 10 push
		stack.push(20); // 20 push
		stack.push(30); // 30 push
		stack.pop(); // 30 pop
		stack.peek(); // 20 peek
		System.out.println(stack.size()); // size 기능 체크
		}catch(Exception e) {
			System.out.println(e.getMessage()); 
		}
	}
}
