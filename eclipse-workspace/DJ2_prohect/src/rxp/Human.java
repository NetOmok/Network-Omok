package rxp;

import java.util.Scanner;

public class Human extends Player {
	Scanner s = new Scanner(System.in);
	protected Human(String name) {
		super(name);
	}
	private String hbet;
	private void sethbet(String bet) {
		this.hbet = bet;
	}
	private String gethbet() {
		return this.hbet;
	}

	//private String name = super.getName();;
	@Override
	public String next() {
		System.out.print(this.name+">>");
		sethbet(s.next());
		
		if(this.gethbet().equalsIgnoreCase("묵")) {
			return this.lastBet = super.bet[0];
		}
		else if(this.gethbet().equalsIgnoreCase("찌")) {
			return this.lastBet = super.bet[1];
		}
		else if(this.gethbet().equalsIgnoreCase("빠")) {
			return this.lastBet = super.bet[2];
		}
		else {
			return "다시 입력하시오";
		}
	}
}
