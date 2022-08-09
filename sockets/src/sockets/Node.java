package sockets;

import java.net.Socket;

import javax.swing.JOptionPane;

import com.dosse.upnp.UPnP;

import java.net.ServerSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Node {
	private int dataRecieved = 0;
	private DataOutputStream dout;
	private Socket sender;
	private ServerSocket ss;
	private Socket s;
	private DataInputStream dis;
	private String recieved = "";
	private int ready = 0;
	public static int rand(int min, int max) {
		return (int) Math.floor(Math.random()*(max-min+1)+min);
	}
	public boolean isReady() {
		return ready > 1;
	}
	public String getRecievedString() {
		return recieved;
	}
	public int getDataRecieved() {
		return dataRecieved;
	}
	public Node(int sPort, int cPort, String ip) {
		System.out.println("Node started");
		startP2P(sPort, cPort, ip);

	}
	public void sendMessage(String s) {
		try {
			dout.writeUTF(s);
			dout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	public void startSendSocket(final String ip, final int port) {
		new Thread()
		{
			public void run() {

				boolean connected = false;
				while(!connected) {
					try {
						sender=new Socket(ip,port);
						dout=new DataOutputStream(sender.getOutputStream());  
						System.out.println("Client started");
						sendMessage(" connected");
						ready++;
						while(true); //keep socket open
					}
					catch (IOException e) {
						//System.out.println("Failed");
						if(!connected)
							continue;
					}
				}
			}

		}.start(); 	
	}
	public void openPort(int port) {
		while(true) {
			if(!UPnP.isMappedTCP(port)) {
				if(!UPnP.openPortTCP(port)) {
					JOptionPane.showMessageDialog(null, "UPnP error.\nTry again with a different port (Recommended 19500+)","Error", JOptionPane.ERROR_MESSAGE); 
					System.exit(1);
				}
				break;
			}
			else { 
				port++;
				System.out.println("Port changed to " + port);
			}
		}
	}
	public void startServerSocket(final int port) {
		new Thread()
		{
			public void run() {
				try{  
					ss=new ServerSocket(port);  
					s=ss.accept();//establishes connection
					System.out.println("Server started");
					ready++;
					while(true) {
						dis=new DataInputStream(s.getInputStream());  
						String  str=(String)dis.readUTF();  
						recieved = str; 
						dataRecieved++;
						//System.out.println(recieved + " " + port);
					}
					//ss.close();  
				}catch(Exception e){sendMessage("Disconnected");}  
			} 
		}.start();
	}
	public void startP2P(int sPort, int cPort, String ip) {
		startSendSocket(ip, cPort);
		startServerSocket(sPort);
		openPort(sPort);
	}


}  
