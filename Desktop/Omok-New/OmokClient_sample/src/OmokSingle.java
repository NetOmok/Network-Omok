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
  	
	// �ٵϵ��� �������� ������ �� �ֵ��� �Ѵ�.
	// �⺻ ������ black1�� white1 �̹����̴�.
	public static String myblack = OmokSingle.myblack;
	public static String mywhite = OmokSingle.mywhite;
	// true�̸� ����ڰ� ���� ���� �� �ִ� ���¸� �ǹ��ϰ�,
	// false�̸� ����ڰ� ���� ���� �� ���� ���¸� �ǹ��Ѵ�.
	private boolean enable=false;
	private boolean running=false; // ������ ���� ���ΰ��� ��Ÿ���� ����
	private PrintWriter writer; // ������� �޽����� �����ϱ� ���� ��Ʈ��
	private Graphics gboard, gbuff; // ĵ������ ���۸� ���� �׷��Ƚ� ��ü
	private Image buff; // ���� ���۸��� ���� ����
	
	// �������� �����ϴ� Ŭ����
	public static final int BLACK = 1,WHITE = -1; // ��� ���� ��Ÿ���� ���
	private int[][]map; // ������ �迭
	private int size; // size�� ������ ���� �Ǵ� ���� ����, 15�� ���Ѵ�.
	private int cell; // ������ ũ��(pixel)
	private int color=BLACK; // ������� �� ����
	int a, b;
	Random rs = new Random();
	
	OmokBoard2(int s, int c) { // �������� ������(s=15, c=30)
		this.size = s; this.cell = c;
		map = new int[size+2][]; // ���� ũ�⸦ ���Ѵ�.
		for(int i=0;i < map.length;i++)
			map[i]=new int[size+2];
		setSize(size*(cell+2)+size, size*(cell+2)+size);    // �������� ũ�⸦ ����Ѵ�.
		/*���⼭���� ���콺�� ������ ������ ���� ���� ���� ���
		 */
		// �������� ���콺 �̺�Ʈ ó��
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me){     // ���콺�� ������
				if(!enable)return;            // ����ڰ� ���� �� ���� �����̸� ���� ���´�.
				// ���콺�� ��ǥ�� map ��ǥ�� ����Ѵ�.
				int x=(int)Math.round(me.getX()/(double)cell);
				int y=(int)Math.round(me.getY()/(double)cell);
				// ���� ���� �� �ִ� ��ǥ�� �ƴϸ� ���� ���´�.
				if(x==0 || y==0 || x==size+1 || y==size+1)return;
				// �ش� ��ǥ�� �ٸ� ���� ������ ������ ���� ���´�.
				if(map[x][y]==BLACK || map[x][y]==WHITE)return;
				// ������� ���� ���� ��ǥ�� �����Ѵ�.
				map[x][y]=color;
				OmokSingle.msgView.append("�÷��̾ (" + x + ", " + y + ")�� �ξ����ϴ�.\n");
				enable=false;
				int resetPoint = 0;
				repaint2();
				// �̰���� �˻��Ѵ�.
				// �ش� ��ǥ�� �߽����� ���� �˻��ؼ� �¸��� Ȯ���Ѵ�.
				if(check(new Point(x, y), color)){
					OmokSingle.msgView.append("�¸��Ͽ����ϴ�.\n");
					OmokSingle.msgView.append("1�� �ڿ� ������մϴ�.\n");
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
				OmokSingle.msgView.append("��ǻ�Ͱ� (" + a + ", " + b + ")�� �ξ����ϴ�.\n");
				if(check(new Point(a, b), -color)){
					OmokSingle.msgView.append("�й��Ͽ����ϴ�.\n");
					OmokSingle.msgView.append("1�� �ڿ� ������մϴ�.\n");
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
				// ������� �θ� enable�� true�� �Ǿ� ����ڰ� �� �� �ְ� �ȴ�.
			}
		});
	}
	
	public void AI(int x, int y) {
		
		// �� ���� �� �� �ִ� ���� ���
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
		
		// ������ 3���� �̾� ���� �ϴ� ��� ���
		// ���� ���
		if(map[x+1][y+1] == color && map[x+2][y+2] == color && x <= size - 3 && y <= size - 3 && x >= 2 && y >= 2 && map[x + 3][y + 3] == 0 && map[x - 1][y - 1] == 0)
		{
			a = x - 1;
			b = y - 1;
			map[a][b]=-color;
			return;
		}
		// ����
		if(map[x][y + 1] == color && map[x][y + 2] == color && y <= size - 3 && y >= 2 && map[x][y + 3] == 0 && map[x][y - 1]  == 0)
		{
			a = x;
			b = y + 3;
			map[a][b]=-color;
			return;			
		}
		// ������ ���
		if(map[x - 1][y + 1] == color && map[x - 2][y + 2] == color && x >= 4 &&  x <= size - 1 && y <= size - 4 && y >= 2 && map[x + 1][y - 1] != -color && map[x - 3][y + 3] != -color)
		{
			a = x + 1;
			b = y - 1;
			map[a][b]=-color;
			return;			
		}
		// ������
		if(map[x - 1][y] == color && map[x - 2][y] == color && x >= 4 && x <= size -1 && map[x - 3][y] != -color && map[x + 1][y] != -color)
		{
			a = x - 3;
			b = y;
			map[a][b]=-color;
			return;			
		}
		// ������ �ϴ�
		if(map[x - 1][y - 1] == color && map[x - 2][y - 2] == color && x <= size + 3 && y <= size + 3 && x >= -2 && y >= -2 && map[x - 3][y - 3] != -color && map[x + 1][y + 1] != -color)
		{
			a = x + 1;
			b = y + 1;
			map[a][b]=-color;
			return;
		}
		// �Ʒ���
		if(map[x][y - 1] == color && map[x][y - 2] == color && y <= size + 3 && y >= -2 && map[x][y - 3] != -color && map[x][y + 1] != -color)
		{
			a = x;
			b = y - 3;
			map[a][b]=-color;
			return;			
		}
		// ���� �ϴ�
		if(map[x + 1][y - 1] == color && map[x + 2][y - 2] == color && x >= -4 &&  x <= size + 1 && y <= size + 4 && y >= -2 && map[x - 1][y + 1] != -color && map[x + 3][y - 3] != -color)
		{
			a = x - 1;
			b = y + 1;
			map[a][b]=-color;
			return;			
		}
		// ����
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
	
	public boolean isRunning(){           // ������ ���� ���¸� ��ȯ�Ѵ�.
		return running;
	}
	
	public void startGame(String col){     // ������ �����Ѵ�.
		running=true;
		if(col.equals("BLACK")){              // ���� ���õǾ��� ��
			enable=true; color=BLACK;
			OmokSingle.msgView.append("�����Դϴ�.\n");
		}   
		else{                                // ���� ���õǾ��� ��
			enable=false; color=WHITE;
			OmokSingle.msgView.append("��ǻ�͸� ��ٸ��ϴ�.\n");
		}
	}

	public void stopGame(){              // ������ �����.
		reset();                              // �������� �ʱ�ȭ�Ѵ�.
		enable=false;
		running=false;
	}

	public void repaint2()
	{
		repaint();
	}
	
	public void putOpponent(int x, int y){       // ������� ���� ���´�.
		map[x][y]=-color;
		repaint();
	}

	public void setEnable(boolean enable){
		this.enable=enable;
	}
	
	public void setWriter(PrintWriter writer){
		this.writer=writer;
	}

	/* ���⼭���� �׸��� ���� ��
	*/
	public void update(Graphics g){        // repaint�� ȣ���ϸ� �ڵ����� ȣ��ȴ�.
		paint(g);                             // paint�� ȣ���Ѵ�.
	}
	
	// paint�� ��� �׸��� ���
	public void paint(Graphics g){                // ȭ���� �׸���.
		if(gbuff==null){                             // ���۰� ������ ���۸� �����.
			buff=createImage(getWidth(),getHeight());
			gbuff=buff.getGraphics();
		}
		try {
			drawBoard(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    // �������� �׸���.
	}

	public void reset(){                         // �������� �ʱ�ȭ��Ų��.
		for(int i=0;i<map.length;i++)
			for(int j=0;j<map[i].length;j++)
				map[i][j]=0;
		OmokSingle.msgView.append("������ ���� ���۵Ǿ����ϴ�.\n");
		repaint();
	}

	private void drawLine() throws IOException{                     // �����ǿ� ���� �ߴ´�.
		gbuff.setColor(Color.black);
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream("board.png"));
		gbuff.drawImage(image, 0,  0, null);
		for(int i=1; i<=size + 1;i++){
			gbuff.drawLine(cell - cell/2, i*cell - cell/2, cell*size + cell/2, i*cell - cell/2);
			gbuff.drawLine(i*cell - cell/2 , cell - cell/2, i*cell- cell/2 , cell*size + cell/2);
		}
	}

	private void drawBlack(int x, int y) throws IOException{         // �� ���� (x, y)�� �׸���.
		String myblack = OmokSingle.myblack;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(myblack));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawWhite(int x, int y) throws IOException{         // �� ���� (x, y)�� �׸���.
		String mywhite = OmokSingle.mywhite;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(mywhite));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawStones() throws IOException{                  // map ������ ������ ��� �׸���.
		for(int x=1; x<=size;x++)
			for(int y=1; y<=size;y++){
				if(map[x][y]==BLACK)
					drawBlack(x, y);
				else if(map[x][y]==WHITE)
					drawWhite(x, y);
			}
	}

	synchronized private void drawBoard(Graphics g) throws IOException{      // �������� �׸���.
		// ���ۿ� ���� �׸��� ������ �̹����� �����ǿ� �׸���.
		gbuff.clearRect(0, 0, getWidth(), getHeight());
		drawLine();
		drawStones();
		gbuff.setColor(Color.red);
		g.drawImage(buff, 0, 0, this);
	}

	/*
	 * ���⼭���� �¸� ������ ���� ��
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
}  // OmokBoard ���� ��

public class OmokSingle extends JFrame {

	private JPanel contentPane;
	public static TextArea msgView=new TextArea("", 1,1,1); // �޽����� �����ִ� ����
	public static String myblack = "black1.png";
	public static String mywhite = "white1.png";
	public static OmokBoard2 board=new OmokBoard2(15,50); // ������ ��ü(�̰� ���� ��� ���� ���)
	Panel p3=new Panel();
	public static JLabel battleground = new JLabel();
	
	
	
	public OmokSingle() {
		setTitle("���� ����");
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
		OmokSingle.msgView.append("������ �����մϴ�.\n���̵��� ���Դϴ�.\n");
	}

}
