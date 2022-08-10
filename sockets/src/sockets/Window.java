package sockets;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Window implements KeyListener{
	private Node node;
	private int dataRecieved = 0;
	private JFrame frame;
	private JPanel panel;
	private JTextArea chatbox;
	private JTextArea message;
	private JButton sendB;
	private JPanel text;
	private JScrollPane scroll;
	public Window(final int sPort, int cPort, final String ip, boolean local){
		node = new Node(sPort,cPort, ip, local);
		frame = new JFrame("P2P App" + " " + ip + ":" + cPort);
		panel = new JPanel();
		chatbox = new JTextArea();
		message = new JTextArea();
		sendB = new JButton("Send");
		text = new JPanel();
		scroll = new JScrollPane(chatbox); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		chatbox.setEditable(false);
		panel.setLayout(new GridLayout(3,3));
		text.setLayout(new GridLayout(1,1));
		message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chatbox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		text.add(scroll);
		panel.add(text);
		panel.add(message);
		panel.add(sendB);
		frame.setSize(500,700);
		frame.setVisible(true);
		chatbox.setCaretPosition(chatbox.getDocument().getLength());

		WindowListener exitListener = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					node.sendMessage(" left the chat");
				}
				catch(Exception e1) {}
			}
		};
		frame.addWindowListener(exitListener);



		new Thread()
		{
			public void run() {
				while(true) {
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if( dataRecieved < node.getDataRecieved()) {
						chatbox.append("\n"+ip+":"+sPort + ": "+node.getRecievedString().substring(1));
						dataRecieved++;
					}
				}


			}
		}.start();

		sendB.addActionListener(new ActionListener() {


			public void actionPerformed(ActionEvent e) {
				sendMessage();

			}

		});

	}
	public void sendMessage() {
		if(node.isReady()) {
			chatbox.append("\nYou: " + message.getText());
			node.sendMessage("\n"+message.getText());
			message.setText("");
		}
		else {
			chatbox.append("You are not connected\n");
		}
	}

	public void keyPressed(KeyEvent e) {
		sendMessage();
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

}
