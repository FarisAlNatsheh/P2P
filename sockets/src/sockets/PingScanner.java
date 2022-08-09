package sockets;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PingScanner{
	private int testedIPs;
	private boolean flag;
	public Object[] checkHosts(String subnet){
		ArrayList<String> openIPs = new ArrayList<String>();
		for (int i=4;i<255;i++){
			String host=subnet + "." + i;
			try {
				if(testIP(host)) {
					//System.out.println(host + " is reachable");
					openIPs.add(host);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return openIPs.toArray();
	}

	public boolean testIP(String ip) {
		Process p1 = null;
		try {
			if(System.getProperty("os.name").toLowerCase().contains("linux") || System.getProperty("os.name").toLowerCase().contains("mac"))
				p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 " + ip);
			else if(System.getProperty("os.name").toLowerCase().contains("win")) {
				p1 = java.lang.Runtime.getRuntime().exec("ping -n 1 " + ip);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean returnVal = false;
		try {
			returnVal = p1.waitFor(30, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			flag = true;
		}
		return (returnVal);
	}

	private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
		// Any Open port on other machine
		// openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
		try (Socket soc = new Socket()) {
			soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}


	public short getSubnetMask() {
		InetAddress localHost = null;
		NetworkInterface networkInterface = null;
		String machine = getMachineIPV4();
		try {
			localHost = Inet4Address.getByName(machine);
			networkInterface = NetworkInterface.getByInetAddress(localHost);
		} catch (Exception e1) {
			e1.printStackTrace();
		}


		for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
			String subnet =(address.getAddress() + "/" + address.getNetworkPrefixLength());
			if(subnet.indexOf(machine) != -1) {
				return (address.getNetworkPrefixLength());
			}
		}
		return -1;
	}


	public String getMachineIPV4() {
		String thisMachine = "127.0.0.1";
		try {
			Socket s = new Socket("192.168.1.1", 80);
			thisMachine = (s.getLocalAddress().getHostAddress());
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return thisMachine;
	}

	public ArrayList<String> availableIPs() {

		ArrayList<String> list = new ArrayList<String>();

		short subnetMask = getSubnetMask();
		String machine = getMachineIPV4();

		short[][] octets = new short[4][8];

		for(int i =0; i < octets.length;i++) {
			for(int j =0; j < 8;j++) {
				if(subnetMask > 0)
					octets[i][j] = 1;
				subnetMask--;
			}
		}



		int min = binaryToDecimal(octets[0]);

		for(int i = min; i <= 255; i++) {
			min = binaryToDecimal(octets[1]);
			for(int j = min; j <= 255; j++) {

				min = binaryToDecimal(octets[2]);
				for(int k = min; k <= 255; k++) {

					min = binaryToDecimal(octets[3]);
					for(int l = min; l <= 255; l++) {
						String ip = "";
						String mach = machine;
						if(i != 255)
							ip+=i;
						else {
							ip+= mach.substring(0,mach.indexOf("."));
							mach = mach.substring(mach.indexOf(".")+1);
						}
						ip += ".";
						if(j != 255)
							ip+=j;
						else {
							ip+= mach.substring(0,mach.indexOf("."));
							mach = mach.substring(mach.indexOf(".")+1);
						}
						ip+=".";
						if(k!= 255)
							ip+=k;
						else {
							ip+= mach.substring(0,mach.indexOf("."));
							mach = mach.substring(mach.indexOf(".")+1);
						}
						ip+=".";
						if(l!= 255)
							ip+=l;
						else {
							ip+= mach;
						}
						list.add(ip);						
					}
				}
			}
		}
		list.remove(list.size()-1);
		return list;
	}

	public int binaryToDecimal(short[] binary) {
		int bin=0;
		int power = 0;
		for(int i =binary.length-1 ; i >= 0; i--) {
			if(binary[i] == 1)
				bin += Math.pow(2,power);
			power++;
		}
		return bin;
	}
	public void scanPorts() {
		//PORT SPECIFIC SCANNER
		ArrayList<String> ips = availableIPs();
		for(final String ip : ips)
			new Thread() {
			public void run() {
				if(isReachable(ip,54839,150))
					System.out.println(ip);
			}
		}.start();
	}
	public String[] scanPing() {
		String[] avIPs = new String[0];
		//System.out.println(System.getProperty("os.name"));
		ArrayList<String> ips = availableIPs();
		for(String i: ips) {
			if(flag) {
				return avIPs;
			}
			if(testIP(i)) {
				String[] temp = new String[avIPs.length+1];
				for(int j =0; j < avIPs.length; j++) {
					temp[j] = avIPs[j];
				}
				temp[temp.length-1] = getIPName(i)+" ("+i+")";
				avIPs = temp;
			}
			testedIPs++;

		}

		return avIPs;
	}
	public byte[] getByteArr(String ips) {
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(ips);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		byte[] bytes = ip.getAddress();
		return bytes;
	}
	public String getIPName(String ip) {
		InetAddress ipAdd;
		try {
			ipAdd = InetAddress.getByAddress(getByteArr(ip));
			return (ipAdd.getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip + "Name not found";
	}
	public int getTestedIPs() {
		return testedIPs;
	}
	public boolean getFlag() {
		return flag;
	}
	public void setFlag(boolean n) {
		flag = n;
	}
}



