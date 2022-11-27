import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Toolkit;

public class Main extends JFrame {

	String myblack = "hs_black.jpg";
	String mywhite = "hs_white.jpg";
	private JPanel contentPane;
	private JTextField nametext;
	private OmokClient client = new OmokClient("오목 게임");

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 300); 
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// 자바 디자인 설정
		setUndecorated(true);
		setLocationRelativeTo(null);

		// 프로그램 종료 버튼
		JLabel close = new JLabel("");
		close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				close.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_close-hover.jpg")));
			}

			public void mouseExited(MouseEvent arg0) {
				close.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_close.jpg")));
			}

			public void mouseClicked(MouseEvent arg0) {
				System.exit(0);
			}
		});
		close.setIcon(new ImageIcon(Main.class.getResource("hs_close.jpg")));
		close.setBounds(360, 10, 28, 30);
		contentPane.add(close);

		///////////////

		nametext = new JTextField();
		nametext.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		nametext.setBounds(28, 160, 175, 35);
		contentPane.add(nametext);
		nametext.setColumns(10);

		// 접속하기 버튼
		JLabel join = new JLabel("");
		join.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				join.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_enter_hover.jpg")));
			}

			public void mouseExited(MouseEvent arg0) {
				join.setIcon(new javax.swing.ImageIcon(getClass().getResource("hs_enter.jpg")));
			}

			public void mouseClicked(MouseEvent arg0) {
				client.setSize(900, 740);
				client.setBounds(300, 50, 900, 740);
				client.setVisible(false);
				client.connect();
				client.nameBox.setText(nametext.getText());
				client.goToWaitRoom();
				client.setVisible(true);
				setVisible(false);
			}
		});
		join.setIcon(new ImageIcon(Main.class.getResource("hs_enter.jpg")));
		join.setBounds(230, 149, 135, 52);
		contentPane.add(join);

		JLabel background_d = new JLabel("");
		background_d.setIcon(new ImageIcon(Main.class.getResource("hs_background.jpg")));
		background_d.setBounds(0, 0, 400, 300);
		contentPane.add(background_d);

		join.setVisible(true);
		nametext.setVisible(true);
		background_d.setVisible(true);
		//////////////////////
	}
}
