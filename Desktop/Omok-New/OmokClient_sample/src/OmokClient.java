import java.awt.BorderLayout;
//import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import java.util.ArrayList;

class OmokBoard extends Canvas{
  	
	String myblack = OmokClient.myblack;
	String mywhite = OmokClient.mywhite;
	
	public int auth; // 관전자, 플레이어 구분
	
	// true이면 사용자가 돌을 놓을 수 있는 상태를 의미하고,
	// false이면 사용자가 돌을 놓을 수 없는 상태를 의미한다.
	private boolean enable=false;
	private boolean running=false; // 게임이 진행 중인가를 나타내는 변수
	private PrintWriter writer; // 상대편에게 메시지를 전달하기 위한 스트림
	private Graphics gboard, gbuff; // 캔버스와 버퍼를 위한 그래픽스 객체
	private Image	 buff; // 더블 버퍼링을 위한 버퍼
	
	// 오목판을 구현하는 클래스
	public static final int BLACK = 1,WHITE = -1; // 흑과 백을 나타내는 상수
	public int[][]map; // 오목판 배열
		
	
	private int size; // size는 격자의 가로 또는 세로 개수, 15로 정한다.
	private int cell; // 격자의 크기(pixel)
	private String info="[ 게임이 정지된 상태입니다. ]"; // 게임의 진행 상황을 나타내는 문자열
	private int color=BLACK; // 사용자의 돌 색깔

	private ArrayList<Point> pointHistory = new ArrayList<Point>(); //오목돌의 좌표를 담아두는 Point ArrayList
	
	OmokBoard(int s, int c) { // 오목판의 생성자(s=15, c=30)
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
				// 여기에 조건 하나 추가해서 33?? 
				writer.println("[STONE]" + x + " "+y); /// 체크포인트

				///////////진짜///////////////
				OmokClient.backButton.setEnabled(true);
				
				writer.println("[OBSER]" + color + x + " "+y);   // color값 서버 보내서 그걸 서버가 관전자에게 보내면 그 값을 식별해서 흑돌둘지 백돌둘지 표시
				map[x][y]=color;

				//////////체크/////////
				pointHistory.add(new Point(x, y)); //ArrayList pointHistory에 그리려고 하는 좌표를 담아둔다.
				
				repaint(); // 오목판을 그린다.
				// 이겼는지 검사한다.
				if(check(new Point(x, y), color)){
					OmokClient.msgView.append("승리하였습니다.\n");
					writer.println("[WIN]");
				}
				else OmokClient.msgView.append("돌을 놓았습니다, 기다리세요.\n");
				
				// 사용자가 둘 수 없는 상태로 만든다.
				// 상대편이 두면 enable이 true가 되어 사용자가 둘 수 있게 된다.
				enable=false;
			}
		});
	}
	
	public boolean isRunning(){           // 게임의 진행 상태를 반환한다.
		return running;
	}
	
	public void startGame(String col){     // 게임을 시작한다.
		running=true;
		if(auth==0) { //플레이어
			if(col.equals("BLACK")){              // 흑이 선택되었을 때
				enable=true; color=BLACK;
				OmokClient.msgView.append("선공입니다.\n");
			}   
			else{                                // 백이 선택되었을 때
				enable=false; color=WHITE;
				OmokClient.msgView.append("기다리세요.\n");
			}
		} else { //관전자
			enable = false;
			color = BLACK; // 관전자는 흑돌로 취급
			OmokClient.msgView.append("게임이 시작됐습니다.\n");
		}
	}

	public void stopGame(){              // 게임을 멈춘다.
		reset();                              // 오목판을 초기화한다.
		writer.println("[STOPGAME]");        // 상대편에게 메시지를 보낸다.
		enable=false;
		running=false;
	}

	public void putOpponent(int x, int y){       // 상대편의 돌을 놓는다.
		map[x][y]=-color;
		OmokClient.msgView.append("당신 차례입니다\n");
		pointHistory.add(new Point(x, y));
		//OmokClient.msgView.append("상대가 두었습니다.\n");
		repaint();
	}
	public void putOpponent_observer_white(int x, int y) {/// 관전자보드 백돌
		OmokClient.msgView.append("백돌이 놓였습니다\n");
		map[x][y]=-color; 
		repaint();		
	}
	public void putOpponent_observer_black(int x, int y) {/// 관전자보드 흑돌
		OmokClient.msgView.append("흑돌이 놓였습니다\n");
		map[x][y]=color; 
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
		OmokClient.msgView.append("게임이 정지된 상태입니다.\n");
		repaint();
	}

	private void drawLine() throws IOException{                     // 오목판에 선을 긋는다.
		gbuff.setColor(new Color(0,0,0,0));  // Color.black
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream("hs_board.jpg"));
		gbuff.drawImage(image, 0,  0, null);
		for(int i=1; i<=size + 1;i++){
			gbuff.drawLine(cell - cell/2, i*cell - cell/2, cell*size + cell/2, i*cell - cell/2);
			gbuff.drawLine(i*cell - cell/2 , cell - cell/2, i*cell- cell/2 , cell*size + cell/2);
			
			
		}
	}

	private void drawBlack(int x, int y) throws IOException{         // 흑 돌을 (x, y)에 그린다.
		String myblack = OmokClient.myblack;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(myblack));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawWhite(int x, int y) throws IOException{         // 백 돌을 (x, y)에 그린다.
		String mywhite = OmokClient.mywhite;
		Graphics2D gbuff=(Graphics2D)this.gbuff;
		BufferedImage image;
		image = ImageIO.read(getClass().getResourceAsStream(mywhite));
		gbuff.drawImage(image, x*cell-cell/2,  y*cell-cell/2, null);
	}

	private void drawStones() throws IOException{                  // map 놓여진 돌들을 모두 그린다.
		for(int x=1; x<=size;x++){
			for(int y=1; y<=size;y++){
				if(map[x][y]==BLACK)
					drawBlack(x, y);
				else if(map[x][y]==WHITE)
					drawWhite(x, y);
			}
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
	
	////////체크//////////////
	public void backRequest(){
		Point lastLocation = pointHistory.remove(pointHistory.size() - 1);
		map[(int)lastLocation.getX()][(int)lastLocation.getY()] = 0;
		//checkOrder[(int)lastLocation.getX()][(int)lastLocation.getY()] = 0;
		repaint();
		enable = true;
	}
	
}  // OmokBoard 정의 끝

// 이것이 진짜 클래스이고 OmokBoard를 이용하는 주체가 된다.
public class OmokClient extends JFrame implements Runnable, ActionListener {
	
	public static String myblack = "hs_black2.png";
	public static String mywhite = "hs_white.png";
	/*
	 * 여기서부터 메인 디자인과 관련한 부분
	 */
	public static TextArea msgView=new TextArea("", 1,1,1); // 메시지를 보여주는 영역
	private TextField sendBox=new TextField(""); // 보낼 메시지를 적는 상자
	private TextField roomNameBox = new TextField("");
	public static TextField nameBox=new TextField(); // 사용자 이름 상자
	private TextField roomBox=new TextField("0"); // 방 번호 상자

	// 방에 접속한 인원의 수를 보여주는 레이블
	private Label pInfo=new Label("대기실:  명");
	private java.awt.List pList=new java.awt.List();  // 사용자 명단을 보여주는 리스트
//	private java.awt.List rList = new java.awt.List(); //방 명단을 보여주는 리스트
	JLabel makeRoom = new JLabel("");
//	JLabel enterButton = new JLabel("");
	JLabel exitButton = new JLabel("");
	// 각종 정보를 보여주는 레이블
	private Label infoView=new Label("오목 게임에 온 것을 환영합니다.", 1);
	private OmokBoard board=new OmokBoard(15,30); // 오목판 객체(이게 거의 모든 것을 담당) //private OmokBoard board=new OmokBoard(15,50);
	private BufferedReader reader; // 입력 스트림
	private PrintWriter writer; // 출력 스트림
	private Socket socket; // 소켓
	private int roomNumber=-1; // 방 번호
	private String userName=null; // 사용자 이름
//	private String roomName = null;

	Panel p1=new Panel();
	Panel p2=new Panel();
	
	//////////////
	JFrame f1 = new JFrame("Back Request"); //client 1이 무르기 요청을 하면 client 2는 무르기 승낙 또는 거절 표시를 할 frame을 받는다.
	

	JLabel ready = new JLabel("");
	JLabel quit = new JLabel("");
	JLabel readyback = new JLabel("");
	JLabel battleground = new JLabel("");
	JLabel backInfoView = new JLabel("상대방의 무르기 요청이 들어왔습니다.");
	
	//////체크///////
	JLabel yesButton = new JLabel("");
	JLabel noButton	= new JLabel("");
	static JLabel backButton = new JLabel("");
	
	
	public OmokClient(String title){ // 생성자
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);   
		infoView.setForeground(new Color(255, 255, 255));
		infoView.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		infoView.setBounds(500,281,350,40);
		infoView.setBackground(new Color(51, 51, 255));
		nameBox.setBackground(Color.WHITE);
		nameBox.setForeground(Color.BLACK);
		nameBox.setFont(new Font("문체부 돋음체", Font.BOLD, 30));
		nameBox.setBounds(175, 125, 190, 40); //nameBox.setBounds(165, 161, 214, 58);
		getContentPane().add(nameBox);
		
		roomBox.setFont(new Font("맑은 고딕", Font.BOLD, 26));
		roomBox.setBounds(576, 205, 103, 44);
		getContentPane().add(roomBox);
		getContentPane().add(infoView);
		Toolkit toolkit = getToolkit();
		Dimension size = toolkit.getScreenSize();
		setLocation(size.width/2 - 640, size.height/2 - 200);
		infoView.hide();
		// 바로 밑에 있는 패널
		
		p1.setLayout(new BorderLayout());
		p1.add(pList, BorderLayout.CENTER); 
		p1.setBounds(60,300,385,300); 
		p1.setVisible(true);

		p2.setLayout(new BorderLayout());
		p2.add(msgView, BorderLayout.CENTER);
		// 각종 컴포넌트를 생성하고 배치한다.
		msgView.setEditable(false);
		sendBox.setFont(new Font("맑은 고딕", Font.BOLD, 15)); 
		p2.add(sendBox, "South"); 
		p2.setBounds(450, 300, 385, 300);
		
		makeRoom.setBounds(700, 205, 130, 44);
		getContentPane().add(makeRoom);
		
		
		makeRoom.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				makeRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_make_hover.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
				makeRoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_make.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					msgView.setText("");
					if(Integer.parseInt(roomBox.getText())<1){
						pInfo.setText("방번호가 잘못되었습니다. 1이상");						
						return;
					}
					writer.println("[ROOM]"+Integer.parseInt(roomBox.getText()));
//					EnterRoomShow();
			
				}catch(Exception ie){
					
				}
			}
		});	
		makeRoom.setIcon(new ImageIcon(Main.class.getResource("hs_make.jpg")));
		
		
		exitButton.setBounds(510, 200, 311, 50);
		getContentPane().add(exitButton);
		exitButton.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_exit_hover.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
				exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_exit.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
				msgView.setText("");
				goToWaitRoom();
				roomBox.show();
				board.hide();
				makeRoom.show();
				ready.hide();
				quit.hide();
				nameBox.show();
				readyback.show();
				battleground.hide();
				pInfo.show();
				p2.setBounds(450, 300, 385, 300);
				p1.show();
				infoView.hide();
				exitButton.hide();				
				backButton.hide();
				
			}
		});
		exitButton.setIcon(new ImageIcon(Main.class.getResource("hs_exit.jpg")));
		exitButton.hide();
		

		////////////체크/////////////
		backButton.setBounds(669, 70, 152, 51);
		getContentPane().add(backButton);
		backButton.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
			backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_back_hover.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
			backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_back.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
			try {
				writer.println("[BACKREQUEST]");
//				if(board.auth == 1) { // 관전자
//					infoView.setText("무르기가 진행중입니다.\n");										
//				}
//				else { // 플레이어
				infoView.setText("상대에게 무르기 요청을 합니다....\n");					
//				}
			}catch(Exception e) {}
			}
		});
		backButton.setIcon(new ImageIcon(Main.class.getResource("hs_back.jpg")));
		backButton.hide();

		noButton.setBounds(700, 350, 60, 30);
		//getContentPane().add(noButton);
		noButton.addMouseListener(new MouseAdapter(){
//		@Override
//		public void mouseEntered(MouseEvent arg0) {
//		noButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_ready_hover.jpg")));
//		}
//		public void mouseExited(MouseEvent arg0) {
//		noButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_ready.jpg")));
//		}
		public void mouseClicked(MouseEvent arg0){
		try {
		writer.println("[NO]");
		infoView.setText("상대방의 무르기를 거절했습니다.");
		f1.setVisible(false);
		}catch(Exception e) {}
		}
		});
//		noButton.setIcon(new ImageIcon(Main.class.getResource("hs_ready.jpg")));
		//noButton.hide();

		yesButton.setBounds(650, 350, 60, 30);
		//getContentPane().add(yesButton);
		yesButton.addMouseListener(new MouseAdapter(){
//		@Override
//		public void mouseEntered(MouseEvent arg0) {
//		yesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_back_hover.jpg")));
//		}
//		public void mouseExited(MouseEvent arg0) {
//		yesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_back.jpg")));
//		}
		public void mouseClicked(MouseEvent arg0){
		try {
			writer.println("[YES]");
			infoView.setText("상대방의 무르기를 승낙했습니다.");
			f1.setVisible(false);
			board.setEnable(false);
			backButton.setEnabled(false);
		}catch(Exception e) {}
		}
		});
//		yesButton.setIcon(new ImageIcon(Main.class.getResource("hs_ready.jpg")));
		//yesButton.hide();

		
		ready.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				ready.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_ready_hover.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
				ready.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_ready.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					writer.println("[START]");
					infoView.setText("상대의 결정을 기다립니다.");
				//	ready.setEnabled(false);
				}catch(Exception e){}
			}
		});
		ready.setIcon(new ImageIcon(Main.class.getResource("hs_ready.jpg")));
		ready.setBounds(510, 135, 152, 51);
		getContentPane().add(ready);
		ready.hide();
		
		quit.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {
				quit.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_quit_hover.jpg")));
			}
			public void mouseExited(MouseEvent arg0) {
				quit.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_quit.jpg")));
			}
			public void mouseClicked(MouseEvent arg0){
				try{
					writer.println("[DROPGAME]");
					endGame("기권하였습니다.");
				}catch(Exception e){}
			}
		});
		quit.setIcon(new ImageIcon(Main.class.getResource("hs_quit.jpg")));
		quit.setBounds(669, 135, 152, 51);
		getContentPane().add(quit);
		quit.hide();
		pInfo.setForeground(new Color(255, 255, 255));
		
		pInfo.setBackground(new Color(51, 51, 255)); //pInfo.setBackground(new Color(50, 205, 50));
		pInfo.setFont(new Font("맑은 고딕", Font.BOLD, 30));
		pInfo.setBounds(52, 190, 800, 70); //pInfo.setBounds(55, 250, 1170, 60);
		getContentPane().add(pInfo);
		board.setBounds(5, 135, 476, 476);
		getContentPane().add(board);
		board.hide();
		getContentPane().add(p1);
		getContentPane().add(p2);
		
		readyback.setIcon(new ImageIcon(Main.class.getResource("HS_robby.jpg")));
		readyback.setBounds(0, 0, 900, 703);
		getContentPane().add(readyback);
		
		
		battleground.setIcon(new ImageIcon(Main.class.getResource("hs_battleground.jpg")));
		battleground.setBounds(0, 0, 900, 703);
		getContentPane().add(battleground);
		battleground.hide();
		
		sendBox.addActionListener(this);
		//enterButton.addActionListener(this);
		//enterButton.hide();
		//exitButton.addActionListener(this);
		
		// 윈도우 닫기 처리
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
	}	
	
	/*
	 * 여기서부터 서버로 메시지를 보내는 부분
	 * (방에 들어간다던지 아이디를 입력한다던지 등)
	 */
	// 컴포넌트들의 액션 이벤트 처리
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource()==sendBox){             // 메시지 입력 상자이면
			String msg=sendBox.getText();
			if(msg.length()==0)
				return;
			if(msg.length()>=30)msg=msg.substring(0,30);
			try{  
				writer.println("[MSG]"+msg);
				sendBox.setText("");
			}catch(Exception ie){}
		}
		
		
		/// 삭제 체크리스트 //
		else if(ae.getSource()==exitButton){           // 대기실로 버튼이면
			try{
				goToWaitRoom();
				//startButton.setEnabled(false);
				//stopButton.setEnabled(false);
			}catch(Exception e){}
		}
	}

	/* 대기실로 입장하는 부분임.
		여기서 처리하는 부분은 오직 이름을 입력하고 접속하는 부분(가장 첫 번째 부분)
	*/
	void goToWaitRoom(){                   // 대기실로 버튼을 누르면 호출된다.
		if(userName==null){
			String name=nameBox.getText();
			if(name.length()<=1 || name.length()>10){
				infoView.setText("이름은 2자부터 10자 사이만 가능합니다.");
				nameBox.requestFocus();
				return;
			}
			userName=name;
			writer.println("[NAME]"+userName);    
			nameBox.setText(userName);
			nameBox.setEditable(false);
		}  
		msgView.setText("");
		// 대기실은 ROOM 중에서 0에 해당한다.
		writer.println("[ROOM]0");
		infoView.setText("대기실에 입장하셨습니다.");
		roomBox.setText("0");
		makeRoom.setEnabled(true);
		exitButton.setEnabled(false);
		playersInfo();
	}
	
	public void EnterRoomShow() {
		board.show();
		exitButton.show();
		makeRoom.hide();					
		roomBox.hide();
		nameBox.hide();
		readyback.hide();
		battleground.show();
		pInfo.hide();
		p2.setBounds(500, 320, 350, 300);
		p1.hide();
		infoView.show();
		if(board.auth == 1) { // 관전자 무르기 버튼 비활성화
			backButton.hide();
			infoView.setText("게임을 관전중입니다.");
		}
		else { // 플레이어 무르기 버튼 활성화
			backButton.show();					
		}
		backButton.setEnabled(false);
	}
	

	public void run(){
		String msg;                             // 서버로부터의 메시지
		try{
			while((msg=reader.readLine())!=null){
				/*
				 * 이건 돌의 좌표와 관련해서 승부 판정을 위해 존재하는 
				 */
				if(msg.startsWith("[STONE]")){     // 상대편이 놓은 돌의 좌표
					if(board.auth == 1) {
						// 관전자는 [OBSER] 프로토콜을 이용해 돌을 표시
					}
					else { // 플레이어
					String temp=msg.substring(7);
					int x=Integer.parseInt(temp.substring(0,temp.indexOf(" ")));
					int y=Integer.parseInt(temp.substring(temp.indexOf(" ")+1));
					board.putOpponent(x, y);     // 상대편의 돌을 그린다.
					board.setEnable(true);        // 사용자가  	돌을 놓을 수 있도록 한다.
					backButton.setEnabled(false);
					}
				}
				if(msg.startsWith("[OBSER]")) {
					if(board.auth == 1) { // 관전자						
						if(!msg.substring(7,8).equals("-")) { // 흑돌
							String temp = msg.substring(8);
							String col = msg.substring(7,8);
							int x=Integer.parseInt(temp.substring(0,temp.indexOf(" ")));
							int y=Integer.parseInt(temp.substring(temp.indexOf(" ")+1));							
							board.putOpponent_observer_black(x,y);
						}else { // 백돌
							String temp = msg.substring(9);
							String col = msg.substring(7,9);
							int x=Integer.parseInt(temp.substring(0,temp.indexOf(" ")));
							int y=Integer.parseInt(temp.substring(temp.indexOf(" ")+1));
							board.putOpponent_observer_white(x,y);							
							
						}
						board.setEnable(false);        // 사용자가 돌을 놓을 수 없도록 한다.					
					}
					else {// 플레이어
						// 플레이어는 [STONE] 프로토콜을 이용해 돌을 표시
					}
				}
				
				/*여기는 방에 입장하는 것과 나가는 것에 관련한 것
				 * 
				 */
				
				// 얘를 makeroom 클릭리스너에 넣기?
				// while문 안에서 돌아야해서 안된다???
				// 클릭리스너 안에서 while문 돌다가 프로토콜 들어오면 break; 하는 방식?
				else if(msg.startsWith("[ROOM]")){    // 방에 입장
					if(!msg.equals("[ROOM]0")){          // 대기실이 아닌 방이면
						EnterRoomShow();
						makeRoom.setEnabled(false);
						exitButton.setEnabled(true);
						ready.show();
						quit.show();
						infoView.setText(msg.substring(6)+"번 방에 입장하셨습니다.");
						board.auth = 0; // 플레이어보드
					}
					else { infoView.setText("대기실에 입장하셨습니다."); }
					roomNumber=Integer.parseInt(msg.substring(6));     // 방 번호 지정
					if(board.isRunning()){                    // 게임이 진행중인 상태이면
						board.stopGame();                    // 게임을 중지시킨다.
					}
				}
				else if(msg.startsWith("[FULL]")){       // 방이 찬 상태이면
					ready.hide();
					quit.hide();
					
					EnterRoomShow();
					roomNumber=Integer.parseInt(msg.substring(6));     // 방 번호 지정
					infoView.setText(msg.substring(6)+"방의 관전자입니다.\n");

					exitButton.setEnabled(true);
					board.auth = 1; // 관전자보드
					backButton.setVisible(false);
			
				}
				else if(msg.startsWith("[RUNNING]")) {					
					
					pInfo.setText("해당 방 게임이 진행중입니다.");
					roomBox.setText("0");
				}
				else if(msg.startsWith("[PLAYERS]")){      // 방에 있는 사용자 명단
					nameList(msg.substring(9));
				}
				else if(msg.startsWith("[ENTER]")){        // 유저 입장
					pList.add(msg.substring(7));
					playersInfo();
					msgView.append("["+ msg.substring(7)+"]님이 입장하였습니다.\n");
				}
				else if(msg.startsWith("[EXIT]")){          // 유저 퇴장
					pList.remove(msg.substring(6));// 리스트에서 제거
					playersInfo();                        // 인원수를 다시 계산하여 보여준다.
					msgView.append("["+msg.substring(6)+"]님이 다른 방으로 입장하였습니다.\n");
					endGame("상대가 나갔습니다.");
				}
				else if(msg.startsWith("[DISCONNECT]")){     // 유저 접속 종료
					pList.remove(msg.substring(12));
					playersInfo();
					msgView.append("["+msg.substring(12)+"]님이 접속을 끊었습니다.\n");
					if(roomNumber!=0)
						endGame("상대가 나갔습니다.");
				}
				/*여기는 게임 진행과 관련한 부분
				 * 
				 */
				else if(msg.startsWith("[COLOR]")){          // 돌의 색을 부여받는다.
					String color;
					if(board.auth == 1) { // 관전자는 흑돌
						color = "BLACK";
					}else {
						color = msg.substring(7);
					}
					
					board.startGame(color);                      // 게임을 시작한다.
					if(color.equals("BLACK")) {
						if(board.auth != 1) { // 플레이어만 표시
							infoView.setText("흑돌을 잡았습니다.");
						}
//						infoView.setText("흑돌을 잡았습니다.");
					}
					else {
						infoView.setText("백돌을 잡았습니다.");              // 기권 버튼 활성화
					}
				}
				else if(msg.startsWith("[YES]")) {
					if(board.auth != 1) { // 플레이어만 표시
						infoView.setText("상대방이 무르기 요청을 승낙했습니다.\n");						
					}
//					infoView.setText("상대방이 무르기 요청을 승낙했습니다.\n");
					board.backRequest();
//					board.setEnable(true);
					backButton.setEnabled(false);
					}
					else if(msg.startsWith("[NO]")) {

					if(board.auth != 1) {
						infoView.setText("상대방이 무르기 요청을 거절했습니다.\n");						
					}
					backButton.setEnabled(false);
					}
					else if(msg.startsWith("[BACKREQUESTYES]")) {
					board.backRequest();
					}
					/*else if(msg.startsWith("[BACKREQUESTNO]")) {

					}*/
				else if(msg.startsWith("[DROPGAME]"))      // 상대가 기권하면
					endGame("상대가 기권하였습니다.");
				else if(msg.startsWith("[WIN]"))              // 이겼으면
					endGame("이겼습니다.");
				else if(msg.startsWith("[LOSE]"))            // 졌으면
					endGame("졌습니다.");
				
				////////////체크////////////
				else if(msg.startsWith("[BACKREQUEST1]")) {
					if(board.auth == 1) {
						infoView.setText("무르기가 진행중입니다.\n"); 	
					}
					else {
						f1.add(yesButton);
						f1.add(noButton);
						f1.add(backInfoView);
						f1.setBounds(550, 300, 250, 200);
						noButton.setBounds(155, 100, 60, 30);
						yesButton.setBounds(50, 100, 60, 30);
						backInfoView.setBounds(10, 30, 240, 30);
						backInfoView.setText("상대방의 무르기 요청이 들어왔습니다.");
						yesButton.setText("YES");
						noButton.setText("NO");
						
						f1.setLayout(null);
						f1.setVisible(true);
					}
				}
				// 약속된 메시지가 아니면 메시지 영역에 보여준다.
				else {
					if(msg.startsWith("[STONE]")) { // 돌 좌표 메시지
						msgView.append(msg+" 위치에 돌이 놓였습니다.\n");						
					}
					else {
						msgView.append(msg+"\n");  // 채팅 메시지		
					}
					
					
					
				}
			}
		}catch(IOException ie){
			msgView.append(ie+"\n");
		}
		msgView.append("접속이 끊겼습니다.");
	}
	
	private void endGame(String msg){                // 게임의 종료시키는 메소드
		infoView.setText(msg);
		try{ Thread.sleep(2000); }catch(Exception e){}    // 2초간 대기
		if(board.isRunning())board.stopGame();
		backButton.setEnabled(false);
	}
	
	private void playersInfo(){                 // 방에 있는 접속자의 수를 보여준다.
		int count=pList.getItemCount();
		pInfo.setText("대기실 : " + count + "명");
		// 대국 시작 버`튼의 활성화 상태를 점검한다.
		if(roomNumber==0)
			pInfo.setText("대기실: "+count+"명");
		else pInfo.setText(roomNumber+" 번 방: "+count+"명");
		// 대국 시작 버튼의 활성화 상태를 점검한다.
		if(count==2 && roomNumber!=0)
			makeRoom.setEnabled(true);
//		else makeRoom.setEnabled(false);

	}

	// 사용자 리스트에서 사용자들을 추출하여 pList에 추가한다.
	private void nameList(String msg){
		pList.removeAll();
		StringTokenizer st=new StringTokenizer(msg, "\t");
		while(st.hasMoreElements())
			pList.add(st.nextToken());
		playersInfo();
	}
	
	public void connect(){ // 연결
		try{
			msgView.append("서버에 연결을 요청합니다.\n");
			socket=new Socket("localhost", 9735);
			msgView.append("연결에 성공하였습니다.\n");
			msgView.append("이름을 입력하고 대기실로 입장하세요.\n");
			reader=new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			writer=new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
			board.setWriter(writer);
		}catch(Exception e){
			msgView.append(e+"\n\n연결에 실패하였습니다.\n");  
		}
	}

}