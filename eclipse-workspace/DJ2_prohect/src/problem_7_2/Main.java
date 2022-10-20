package problem_7_2;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	static Scanner s = new Scanner(System.in);
	
	public static void main(String[] args) {
		
		ArrayList<Character>stack = new ArrayList<>();
		System.out.println("6개의 학점 빈칸으로 분리 입력");

		stack = Method.input(stack, 6);
		int sum = 0;
		sum = Method.sum(stack);
		
		double avr = (double)sum/stack.size();
		System.out.println(avr);
		s.close();
	}
}
