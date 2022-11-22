import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

class OmokBoard2 extends Canvas{
  	
	// 바둑돌의 디자인을 선택할 수 있도록 한다.
	// 기본 설정은 black1과 white1 이미지이다.
	public static String myblack = OmokSingle.myblack;
	public static String mywhite = OmokSingle.mywhite;
	// true이면 사용자가 돌을 놓을 수 있는 상태를 의미하고,
	// false이면 사용자가 돌을 놓을 수 없는 상태를 의미한다.
	private boolean enable=false;
	private boolean running=false; // 게임이 진행 중인가를 나타내는 변수
	private PrintWriter writer; // 상대편에게 메시지를 전달하기 위한 스트림
	private Graphics gboard, gbuff; // 캔버스와 버퍼를 위한 그래픽스 객체
	private Image buff; // 더블 버퍼링을 위한 버퍼
	
	// 오목판을 구현하는 클래스
	public static final int BLACK = 1,WHITE = -1; // 흑과 백을 나타내는 상수
	private int[][]map; // 오목판 배열
	private int size; // size는 격자의 가로 또는 세로 개수, 15로 정한다.
	private int cell; // 격자의 크기(pixel)
	private int color=BLACK; // 사용자의 돌 색깔
	int a, b;
	Random rs = new Random();
	
	OmokBoard2(int s, int c) { // 오목판의 생성자(s=15, c=30)
		this.size = s; this.cell = c;
		map = new int[size+2][]; // 맵의 크기를 정한다.
		for(int i=0;i < map.length;i++)
			map[i]=new int[size+2];
		setSize(size*(cell+2)+size, size*(cell+2)+size);    // 오목판의 크기를 계산한다.
		/*여기서부터 마우스를 눌렀을 때부터 게임 진행 과정 담당
		 */
		// 오목판의 마우스 이벤트 처리
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me){     // 마우스를 누르면
				if(!enable)return;            // 사용자가 누를 수 없는 상태이면 빠져 나온다.
				// 마우스의 좌표를 map 좌표로 계산한다.
				int x=(int)Math.round(me.getX()/(double)cell);
				int y=(int)Math.round(me.getY()/(double)cell);
				// 돌이 놓일 수 있는 좌표가 아니면 빠져 나온다.
				if(x==0 || y==0 || x==size+1 || y==size+1)return;
				// 해당 좌표에 다른 돌이 놓여져 있으면 빠져 나온다.
				if(map[x][y]==BLACK || map[x][y]==WHITE)return;
				// 상대편에게 놓은 돌의 좌표를 전송한다.
				map[x][y]=color;
				OmokSingle.msgView.append("플레이어가 (" + x + ", " + y + ")에 두었습니다.\n");
				enable=false;
				int resetPoint = 0;
				repaint2();
				// 이겼는지 검사한다.
				// 해당 좌표를 중심으로 돌을 검사해서 승리를 확인한다.
				if(check(new Point(x, y), color)){
					OmokSingle.msgView.append("승리하였습니다.\n");
					OmokSingle.msgView.append("1초 뒤에 재시작합니다.\n");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					endGame();
					resetPoint = 1;
				}
				if(resetPoint != 1)
				{
				AI(x, y);
				OmokSingle.msgView.append("컴퓨터가 (" + a + ", " + b + ")에 두었습니다.\n");
				if(check(new Point(a, b), -color)){
					OmokSingle.msgView.append("패배하였습니다.\n");
					OmokSingle.msgView.append("1초 뒤에 재시작합니다.\n");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					endGame();
					resetPoint = 1;
				}
				else
					putOpponent(a, b);
				}
				enable=true;
				// 상대편이 두면 enable이 true가 되어 사용자가 둘 수 있게 된다.
			}
		});
	}
	
	public void AI(int x, int y) {
		
		// 한 번에 질 수 있는 수를 방어
		int newx;
		int newy;
		for(newx = x - 4; newx < x + 4; newx++)
		{
			for(newy = y - 4; newy < y + 4; newy++)
			{
				if(!(newx <= 0 || newy <= 0 || newx >= size+1 || newy >=size+1))
				{
					if(check(new Point(newx, newy), color) && map[newx][newy] == 0)
					{
						a = newx;
						b = newy;
						if(!(map[a][b]==BLACK || map[a][b]==WHITE))
						{
							map[a][b]=-color;
							return;
						}
					}
				}
			}
		}
		
		// 상대방이 3개를 이어 공격 하는 경우 방어
		// 왼쪽 상단
		if(map[x+1][y+1] == color && map[x+2][y+2] == color && x <= size - 3 && y <= size - 3 && x >= 2 && y >= 2 && map[x + 3][y + 3] == 0 && map[x - 1][y - 1] == 0)
		{
			a = x - 1;
			b = y - 1;
			map[a][b]=-color;
			return;
		}
		// 위쪽
		if(map[x][y + 1] == color && map[x][y + 2] == color && y <= size - 3 && y >= 2 && map[x][y + 3] == 0 && map[x][y - 1]  == 0)
		{
			a = x;
			b = y + 3;
			map[a][b]=-color;
			return;			
		}
		// 오른쪽 상단
		if(map[x - 1][y + 1] == color && map[x - 2][y + 2] == color && x >= 4 &&  x <= size - 1 && y <= size - 4 && y >= 2 && map[x + 1][y - 1] != -color && map[x - 3][y + 3] != -color)
		{
			a = x + 1;
			b = y - 1;
			map[a][b]=-color;
			return;			
		}
		// 오른쪽
		if(map[x - 1][y] == color && map[x - 2][y] == color && x >= 4 && x <= size -1 && map[x - 3][y] != -color && map[x + 1][y] != -color)
		{
			a = x - 3;
			b = y;
			map[a][b]=-color;
			return;			
		}
		// 오른쪽 하단
		if(map[x - 1][y - 1] == color && map[x - 2][y - 2] == color && x <= size + 3 && y <= size + 3 && x >= -2 && y >= -2 && map[x - 3][y - 3] != -color && map[x + 1][y + 1] != -color)
		{
			a = x + 1;
			b = y + 1;
			map[a][b]=-color;
			return;
		}
		// 아래쪽
		if(map[x][y - 1] == color && map[x][y - 2] == color && y <= size + 3 && y >= -2 && map[x][y - 3] != -color && map[x][y + 1] != -color)
		{
			a = x;
			b = y - 3;
			map[a][b]=-color;
			return;			
		}
		// 왼쪽 하단
		if(map[x + 1][y - 1] == color && map[x + 2][y - 2] == color && x >= -4 &&  x <= size + 1 && y <= size + 4 && y >= -2 && map[x - 1][y + 1] != -color && map[x + 3][y - 3] != -color)
		{
			a = x - 1;
			b = y + 1;
			map[a][b]=-color;
			return;			
		}
		// 왼쪽
		if(map[x + 1][y] == color && map[x + 2][y] == color && x >= -4 && x <= size +1 && map[x + 3][y] != -color && map[x - 1][y] != -color)
		{
			a = x + 3;
			b = y;
			map[a][b]=-color;
			return;			
		}
		
		for(int i = 0; i < 5; i ++)
		{
			a = x + rs.nextInt(3) - 1;
			b = y + rs.nextInt(3) - 1;
			if(!(map[a][b]==BLACK || map[a][b]==WHITE))
			{
				if(!(a==0 || b==0 || a==size+1 || b==size+1))
				{
					map[a][b]=-color;
					return;
				}
			}
		}
		while(true)
		{
			a = x + rs.nextInt(5) - 2;
			b = y + rs.nextInt(5) - 2;
			if(!(map[a][b]==BLACK || map[a][b]==WHITE))
			{
				if(!(a==0 || b==0 || a==size+1 || b==size+1))
				{
					map[a][b]=-color;
					return;
				}
			}
		}
	}
	
	private void endGame(){   
		stopGame();
	}
	
	public boolean isRunning(){           // 게임의 진행 상태를 반환한다.
		return running;
	}
	
	public void startGame(String col){     // 게임을 시작한다.
		running=true;
		if(col.equals("BLACK")){              // 흑이 선택되었을 때
			enable=true; color=BLACK;
			OmokSingle.msgView.append("선공입니다.\n");
		}   
		else{                                // 백이 선택되었을 때
			enable=false; color=WHITE;
			OmokSingle.msgView.append("컴퓨터를 기다립니다.\n");
		}
	}

	public void stopGame(){              // 게임을 멈춘다.
		reset();                              // 오목판을 초기화한다.
		enable=false;
		running=false;
	}

	public void repaint2()
	{
		repaint();
	}
	
	public void putOpponent(int x, int y){       // 상대편의 돌을 놓는다.
		map[x][y]=-color;
		repaint();
	}

	public void setEnable(boolean enable){
		this.enable=enable;
	}
	
	public void setWriter(PrintWriter writer){
		this.writer=writer;
	}

	/* 여기서부터 그림에 관한 것
	*/
	public void update(Graphics g){        // repaint를 호출하면 자동으로 호출된다.
		paint(g);                             // paint를 호출한다.
	}
	
	// paint가 모든 그림을 담당
	public void paint(Graphics g){                // 화면을 그린다.
		if(gbuff==null){                             // 버퍼가 없으면 버퍼를 만든다.
			buff=createImage(getWidth(),getHeight());
			gbuff=buff.getGraphics();
		}
		try {
			drawBoard(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    // 오목판을 그린다.
	}

	public void reset(){                         // 오목판을 초기화시킨다.
		for(int i=0;i<map.length;i++)
			for(int j=0;j<map[i].length;j++)
				map[i][j]=0;
		OmokSingle.msgView.append("게임이 새로 시작되었습니다.\n");
		repaint();
	}

	private void drawLine() throws IOException{                     // 오목판에 선을 긋는다.
		gbuff.setColor(Color.black);
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream("board.png"));
		gbuff.drawImage(image, 0,  0, null);
		for(int i=1; i<=size + 1;i++){
			gbuff.drawLine(cell - cell/2, i*cell - cell/2, cell*size + cell/2, i*cell - cell/2);
			gbuff.drawLine(i*cell - cell/2 , cell - cell/2, i*cell- cell/2 , cell*size + cell/2);
		}
	}

	private void drawBlack(int x, int y) throws IOException{         // 흑 돌을 (x, y)에 그린다.
		String myblack = OmokSingle.myblack;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(myblack));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawWhite(int x, int y) throws IOException{         // 백 돌을 (x, y)에 그린다.
		String mywhite = OmokSingle.mywhite;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(mywhite));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawStones() throws IOException{                  // map 놓여진 돌들을 모두 그린다.
		for(int x=1; x<=size;x++)
			for(int y=1; y<=size;y++){
				if(map[x][y]==BLACK)
					drawBlack(x, y);
				else if(map[x][y]==WHITE)
					drawWhite(x, y);
			}
	}

	synchronized private void drawBoard(Graphics g) throws IOException{      // 오목판을 그린다.
		// 버퍼에 먼저 그리고 버퍼의 이미지를 오목판에 그린다.
		gbuff.clearRect(0, 0, getWidth(), getHeight());
		drawLine();
		drawStones();
		gbuff.setColor(Color.red);
		g.drawImage(buff, 0, 0, this);
	}

	/*
	 * 여기서부터 승리 판정에 관한 것
	 */
	private boolean check(Point p, int col){
		if(count(p, 1, 0, col)+count(p, -1, 0, col)==4)
			return true;
		if(count(p, 0, 1, col)+count(p, 0, -1, col)==4)
			return true;
		if(count(p, -1, -1, col)+count(p, 1, 1, col)==4)
			return true;
		if(count(p, 1, -1, col)+count(p, -1, 1, col)==4)
			return true;
		return false;
	}

	private int count(Point p, int dx, int dy, int col){
		int i=0;
		for(; map[p.x+(i+1)*dx][p.y+(i+1)*dy]==col ;i++);
		return i;
	}
}  // OmokBoard 정의 끝

public class OmokSingle extends JFrame {

	private JPanel contentPane;
	public static TextArea msgView=new TextArea("", 1,1,1); // 메시지를 보여주는 영역
	public static String myblack = "black1.png";
	public static String mywhite = "white1.png";
	public static OmokBoard2 board=new OmokBoard2(15,50); // 오목판 객체(이게 거의 모든 것을 담당)
	Panel p3=new Panel();
	public static JLabel battleground = new JLabel();
	
	
	
	public OmokSingle() {
		setTitle("오목 게임");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.add(board);
		contentPane.add(p3);
		board.setBounds(20, 120, 795, 795);
		board.startGame("BLACK");
		p3.setLayout(new BorderLayout());
		p3.add(msgView, BorderLayout.CENTER);
		p3.setBounds(822, 120, 430, 795);
		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		setLocation(size.width/2 - 640, size.height/2 - 500);
		
		battleground.setIcon(new ImageIcon(Main.class.getResource("battleground.png")));
		battleground.setBounds(0, 0, 1280, 1000);
		getContentPane().add(battleground);
		OmokSingle.msgView.append("게임을 시작합니다.\n난이도는 下입니다.\n");
	}

}
