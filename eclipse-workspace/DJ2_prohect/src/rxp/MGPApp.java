package rxp;

import java.util.Scanner;

public class MGPApp {

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
	
		Player[] players = new Player[2];		
		
		System.out.println("묵찌빠 게임을 시작합니다.");
		System.out.print("선수이름 입력>>");
		players[0] = new Human(s.next());
		System.out.print("컴퓨터이름 입력>>");
		players[1] = new Computer(s.next());

		System.out.println("2명의 선수를 생성 완료하였습니다.");
		
		players[0].next();
		players[1].next();
		System.out.println(players[1].next());

	}

}
