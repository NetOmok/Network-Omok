import java.net.*;
import java.io.*;
import java.util.*;

// 실제로 서버가 동작하게 해주는 클래스입니다.
public class OmokServer implements Runnable{

	private ServerSocket server; // 소켓 생성
	private Messanger Handle=new Messanger(); // 메시지 발송자
	private Random rnd= new Random(); // 흑과 백을 랜덤으로 고름
	
	// 서버를 실행하는 함수
	void startServer(){
		try{
			server=new ServerSocket(8000);
			Main.textArea.append("서버를 동작시킵니다.\n");
			while(true){
				// 클라이언트와 연결된 스레드 획득
				Socket socket=server.accept();
				// 스레드를 만들고 실행
				controller con=new controller(socket);
				con.start();
				// Handle에 스레드를 추가한다.
				Handle.add(con);
				Main.textArea.append("현재 " + Handle.size() + "명이 접속해 있습니다.\n");
      }
    }catch(Exception e){
      System.out.println(e);
    }
  }
  
	// 클라이언트와 통신하는 스레드 클래스
	class controller extends Thread{
		private int roomNumber = -1;        // 방 번호
		private String userName = null;       // 사용자 이름
		private Socket socket;              // 소켓
		// 게임 준비 여부, true이면 게임을 시작할 준비가 되었음을 의미한다.
		private boolean ready=false;
		private BufferedReader reader;     // 입력 스트림
		private PrintWriter writer;           // 출력 스트림
		controller(Socket socket){     // 생성자
			this.socket=socket;
		}
		Socket getSocket(){               // 소켓을 반환한다.
			return socket;
		}
		int getRoomNumber(){             // 방 번호를 반환한다.
			return roomNumber;
		}
		String getUserName(){             // 사용자 이름을 반환한다.
			return userName;
		}
		boolean isReady(){                 // 준비 상태를 반환한다.
			return ready;
		}
		public void run(){
			try{
				reader=new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
				writer=new PrintWriter(socket.getOutputStream(), true);
				String msg;                     // 클라이언트의 메시지
				while((msg=reader.readLine())!=null){
					// msg가 "[NAME]"으로 시작되는 메시지이면
					if(msg.startsWith("[NAME]")){
						userName=msg.substring(6);          // userName을 정한다.
					}
					// msg가 "[ROOM]"으로 시작되면 방 번호를 정한다.
					else if(msg.startsWith("[ROOM]")){
						int roomNum=Integer.parseInt(msg.substring(6));
						
						// 게임이 시작된 상황이면 못들어오게
						if(Handle.isReady(roomNum)) {
							// 입장불가 프로토콜 전송
							writer.println("[RUNNING]");
						}
						else { // 정상입장						
							if( !Handle.isFull(roomNum)){             // 방이 찬 상태가 아니면
								// 현재 방의 다른 사용에게 사용자의 퇴장을 알린다.
								if(roomNumber!=-1)
									Handle.sendToOthers(this, "[EXIT]"+userName);
								// 사용자의 새 방 번호를 지정한다.
								roomNumber=roomNum;
								// 사용자에게 메시지를 그대로 전송하여 입장할 수 있음을 알린다.
								writer.println(msg);
								// 사용자에게 새 방에 있는 사용자 이름 리스트를 전송한다.
								writer.println(Handle.getNamesInRoom(roomNumber));
								// 새 방에 있는 다른 사용자에게 사용자의 입장을 알린다.
								Handle.sendToOthers(this, "[ENTER]"+userName);
							}
							else {							
								roomNumber=roomNum;
								writer.println("[FULL]"+roomNumber);
								writer.println(Handle.getNamesInRoom(roomNumber));
								Handle.sendToOthers(this, "[ENTER]"+userName);
							}
						}
					}
					// "[STONE]" 메시지는 상대편에게 전송한다.
					else if(roomNumber>=1 && msg.startsWith("[STONE]")) {
						//Handle.sendToOthers(this, msg);
						Handle.sendToOthers(this, msg);
					}
					// "[OBSER]" 관전자 전용 돌두기 
					else if(roomNumber>=1 && msg.startsWith("[OBSER]")) {
						Handle.sendToOthers(this, msg);						
					}
	
					// 대화 메시지를 방에 전송한다.
					else if(msg.startsWith("[MSG]")) {
						Handle.sendToRoom(roomNumber, "["+userName+"]: "+msg.substring(5));
					}
					// "[START]" 메시지이면
					else if(msg.startsWith("[START]")){
						ready=true;   // 게임을 시작할 준비가 되었다.
						// 다른 사용자도 게임을 시작한 준비가 되었으면
						if(Handle.isReady(roomNumber)){
							// 흑과 백을 정하고 사용자와 상대편에게 전송한다.
							int a=rnd.nextInt(2);
							if(a==0){
								writer.println("[COLOR]BLACK");
								Handle.sendToOthers(this,"[COLOR]WHITE");
							}
							else{
								writer.println("[COLOR]WHITE");
								Handle.sendToOthers(this,"[COLOR]BLACK");
							}
						}
					}
					
					
					 else if(msg.startsWith("[BACKREQUEST]")) {
		                  Handle.sendToOthers(this, "[BACKREQUEST1]");
		               }
		               else if(msg.startsWith("[YES]")) {
		                  writer.println("[BACKREQUESTYES]");
		                  Handle.sendToOthers(this, "[BACKREQUESTYESOBSER]"); // 관전자한테
		                  Handle.sendToOthers(this, "[YES]");
		               }
		               else if(msg.startsWith("[NO]")) {
		                  Handle.sendToOthers(this, "[NO]");
		               }
					
					
					// 사용자가 게임을 중지하는 메시지를 보내면
					else if(msg.startsWith("[STOPGAME]"))
						ready=false;
					// 사용자가 게임을 기권하는 메시지를 보내면
					else if(msg.startsWith("[DROPGAME]")){
						ready=false;
						// 상대편에게 사용자의 기권을 알린다.
						Handle.sendToOthers(this, "[DROPGAME]");
					}
					// 사용자가 이겼다는 메시지를 보내면
					else if(msg.startsWith("[WIN]")){
						ready=false;
						// 사용자에게 메시지를 보낸다.
						writer.println("[WIN]");
						// 상대편에는 졌음을 알린다.
						Handle.sendToOthers(this, "[LOSE]");
					}  
				}
			}catch(Exception e){
			}finally{
				try{
					Handle.remove(this);
					if(reader!=null) reader.close();
					if(writer!=null) writer.close();
					if(socket!=null) socket.close();
					reader=null; writer=null; socket=null;
					if(userName == null)
						userName = "신원 불명의 사용자";
					Main.textArea.append(userName+"님이 접속을 끊었습니다.\n");
					Main.textArea.append("현재 " + Handle.size() + "명이 접속해 있습니다.\n");
					// 사용자가 접속을 끊었음을 같은 방에 알린다.
					Handle.sendToRoom(roomNumber,"[DISCONNECT]"+userName);
				}catch(Exception e){}
			}
		}
	}

	class Messanger extends Vector{       // 메시지를 전달하는 클래스
		void add(controller con){           // 스레드를 추가한다.
			super.add(con);
		}
		void remove(controller con){        // 스레드를 제거한다.
			super.remove(con);
		}
		controller getOT(int i){            // i번째 스레드를 반환한다.
			return (controller)elementAt(i);
		}
		Socket getSocket(int i){              // i번째 스레드의 소켓을 반환한다.
			return getOT(i).getSocket();
		}
		// i번째 스레드와 연결된 클라이언트에게 메시지를 전송한다.
		void sendTo(int i, String msg){
			try{
				PrintWriter pw= new PrintWriter(getSocket(i).getOutputStream(), true);
				pw.println(msg);
			}catch(Exception e){}  
		}
		int getRoomNumber(int i){            // i번째 스레드의 방 번호를 반환한다.
			return getOT(i).getRoomNumber();
		}
		synchronized boolean isFull(int roomNum){    // 방이 찼는지 알아본다.
			if(roomNum==0)
				return false;                 // 대기실은 차지 않는다.
			// 다른 방은 2명 이상 입장할 수 없다.
			int count=0;
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))count++;
			if(count>=2)
				return true;
			return false;
		}
		
		// roomNum 방에 msg를 전송한다.
		void sendToRoom(int roomNum, String msg){
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))
					sendTo(i, msg);
		}
    
		// ot와 같은 방에 있는 다른 사용자에게 msg를 전달한다.
		void sendToOthers(controller ot, String msg){
			for(int i=0;i<size();i++)
				if(getRoomNumber(i)==ot.getRoomNumber() && getOT(i)!=ot)
					sendTo(i, msg);
		}
    
		// 게임을 시작할 준비가 되었는가를 반환한다.
		// 두 명의 사용자 모두 준비된 상태이면 true를 반환한다.
		synchronized boolean isReady(int roomNum){
			int count=0;
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i) && getOT(i).isReady())
					count++;
			if(count==2)
				return true;
			return false;
		}

		// roomNum방에 있는 사용자들의 이름을 반환한다.
		String getNamesInRoom(int roomNum){
			StringBuffer sb=new StringBuffer("[PLAYERS]");
			for(int i=0;i<size();i++)
				if(roomNum==getRoomNumber(i))
					sb.append(getOT(i).getUserName()+"\t");
			return sb.toString();
		}
	}
	
	@Override
	public void run() {
	    OmokServer server=new OmokServer();
	    server.startServer();
	}
	
	public void out() throws IOException {
		this.server.close();
	}
}