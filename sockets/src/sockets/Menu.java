package sockets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class Menu extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private JPanel panel = new JPanel();
	private Color color = panel.getBackground();
	private JTextArea serverPort = new JTextArea();
	private JTextArea clientPort = new JTextArea();
	private JTextArea ipAddress = new JTextArea();
	private JLabel labelServer = new JLabel("Server Port: ");
	private JLabel labelIP = new JLabel("Available IPs: ");
	private JLabel loadingLabel = new JLabel("Scan status");
	private JButton con = new JButton("Connect");
	private JButton cancel = new JButton("Cancel scan");
	private JComboBox<String> availableIPs= new JComboBox<String>();
	private volatile String[] scan;
	private PingScanner scanner = new PingScanner();
	private Thread scanThread;
	private JCheckBox samePC = new JCheckBox("Client Port");
	private JCheckBox externalAddress = new JCheckBox("Custom Address");
	private JProgressBar loadingBar = new JProgressBar();
	private volatile double time;
	private Timer timer = new Timer(10, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			loadingBar.setValue((int)(100*(double)scanner.getTestedIPs()/scanner.availableIPs().size()));
			loadingLabel.setText("Progress: "+(int)(100*(double)scanner.getTestedIPs()/scanner.availableIPs().size()) + "%");
			if(scan != null || scanner.getFlag()) {
				try {
					for(String i: scan) {
						loadingBar.setValue(100);
						availableIPs.addItem(i);
						availableIPs.setEditable(false);
						con.setEnabled(true);
						availableIPs.setEnabled(true);
						externalAddress.setEnabled(true);
						loadingLabel.setText("Progress: 100%      Time taken: "+ (int)time + " ms");
						cancel.setText("Restart scan");
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					timer.stop();
				}
				catch(Exception e1) {}
			}
		}
	});
	public Menu() {
		timer.setInitialDelay(100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("P2P Chat");
		setSize(700,300);
		getContentPane().add(panel);
		serverPort.setBorder(BorderFactory.createEtchedBorder());
		clientPort.setBorder(BorderFactory.createEtchedBorder());
		ipAddress.setBorder(BorderFactory.createEtchedBorder());
		clientPort.setEditable(false);
		con.setEnabled(false);
		availableIPs.setEnabled(false);
		ipAddress.setEnabled(false);
		externalAddress.setEnabled(false);
		clientPort.setBackground(color);
		ipAddress.setBackground(color);
		panel.setLayout(new GridLayout(6,2));
		panel.add(labelServer);
		panel.add(serverPort);
		panel.add(samePC);
		panel.add(clientPort);
		panel.add(labelIP);
		panel.add(availableIPs);


		panel.add(externalAddress);
		panel.add(ipAddress);
		panel.add(con);
		panel.add(cancel);

		panel.add(loadingBar);
		panel.add(loadingLabel);

		startScanThread();
		timer.start();
		samePC.addItemListener(new ItemListener() {


			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == 1) {
					clientPort.setEditable(true);
					clientPort.setBackground(Color.WHITE);
				}
				else {
					clientPort.setEditable(false);
					clientPort.setBackground(color);
				}
			}

		});  
		externalAddress.addItemListener(new ItemListener() {


			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == 1) {
					ipAddress.setEnabled(true);
					availableIPs.setEnabled(false);
					ipAddress.setBackground(Color.WHITE);


				}
				else {
					ipAddress.setEnabled(false);
					availableIPs.setEnabled(true);
					ipAddress.setBackground(color);

				}
			}

		});    

		con.addActionListener(this);
		cancel.addActionListener(this);
		setVisible(true);

	}

	public void startScanThread() {
		double temp = System.nanoTime();
		scan = null;
		availableIPs.removeAllItems();
		scanner = new PingScanner();
		scanThread = new Thread() {
			public void run() {
				scan = scanner.scanPing();
				time = (System.nanoTime()-temp)/1000000.0;
			}
		};
		scanThread.start();
	}
	public void actionPerformed(ActionEvent e) {
		//UPnP.openPortTCP(Integer.parseInt(serverPort.getText()));
		if(e.getSource() == cancel) {
			if(scanThread.isAlive()) {
				loadingBar.setValue(0);
				availableIPs.setEditable(true);
				con.setEnabled(true);
				availableIPs.setEnabled(true);
				scanner.setFlag(true);
				
				cancel.setText("Restart scan");
			}
			else {
				timer.start();
				startScanThread();
				availableIPs.setEditable(false);
				con.setEnabled(false);
				availableIPs.setEnabled(false);
				externalAddress.setEnabled(false);
				cancel.setText("Cancel scan");
			}
			return;
		}
		if(!ipAddress.isEditable()) {
			if(!clientPort.isEditable())
				new Window( Integer.parseInt(serverPort.getText()), Integer.parseInt(serverPort.getText()),
						((String)availableIPs.getSelectedItem()).substring( ((String)availableIPs.getSelectedItem()).indexOf('(')+1, ((String)availableIPs.getSelectedItem()).length()-1));
			else
				new Window( Integer.parseInt(serverPort.getText()), Integer.parseInt(clientPort.getText()),
						((String)availableIPs.getSelectedItem()).substring( ((String)availableIPs.getSelectedItem()).indexOf('(')+1, ((String)availableIPs.getSelectedItem()).length()-1));
		}
		else {
			if(!clientPort.isEditable())
				new Window( Integer.parseInt(serverPort.getText()), Integer.parseInt(serverPort.getText()),ipAddress.getText());
			else
				new Window( Integer.parseInt(serverPort.getText()), Integer.parseInt(clientPort.getText()),ipAddress.getText());

		}
		this.dispose();
	}

}
