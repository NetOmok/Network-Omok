package problem_7_2;
import java.util.ArrayList;
import java.util.Scanner;

public class Method {
	
	static Scanner ss = new Scanner(System.in);
	
	public static ArrayList<Character> input(ArrayList<Character> arr, int j) {

		for(int i=0; i<j; i++) {
			char c = ss.next().charAt(0);
			arr.add(c);
		}
		return arr;
	}
	
	public static int sum (ArrayList<Character> arr) {
		int sum = 0;
		
		for(int i=0; i<arr.size(); i++) {
			char c = arr.get(i);
			switch(c) {
			case 'A':
				sum += 4;
				break;
			case 'B':
				sum += 3;
				break;
			case 'C':
				sum += 2;
				break;
			case 'D':
				sum += 1;
				break;
			case 'F':
				sum += 0;
				break;
			default:
				System.out.println(c+" 유효하지 않은 성적");
				
			}
		}
		return sum;
	}

}
