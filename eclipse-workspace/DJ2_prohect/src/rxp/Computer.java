package rxp;

import java.util.Scanner;

public class Computer extends Player{
	protected Computer(String name) {
		super(name);
	}

	Scanner s = new Scanner(System.in);
	
	@Override
	public String next() {
		
		this.lastBet = this.bet[(int)(Math.random()/3)];
		System.out.println(this.name+">>"+"결정하였습니다.");
		return this.lastBet;
	}
	

}
