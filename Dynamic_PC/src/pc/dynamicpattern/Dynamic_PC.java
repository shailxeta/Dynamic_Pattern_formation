/*
 
 DYNAMIC PATTERN FORMATION :
 
 This program is responsible for receiving request from each NXT and reply them accordingly.
 
 There are three threads for each NXT running...
 
 FUNCTIONS :
 
 forwards message of one NXT to other.
 Send NXT and food coordinates to all NXT's (depending on there vision)
 
 * */

package pc.dynamicpattern;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;

public class Dynamic_PC {

	//--------------global variables -------------------------
	static String NXT2_Name = "DAS";
	static String NXT2_Address = "00:16:53:06:c6:e1";
	static String NXT3_Name = "AVI";
	static String NXT3_Address = "00:16:53:09:38:cb";
	static String NXT_Name = "SHAIL";
	static String NXT_Address = "00:16:53:09:3b:cb";
	static String IP = "127.0.0.1";
	static int PORT = 6685;
	static NXTConnector conn;
	static DataOutputStream dos;
	static DataInputStream dis;
	static InputStream is;
	static OutputStream os;
	static DataOutputStream dos1;
	static DataInputStream dis1;
	static InputStream is1;
	static OutputStream os1;
	static DataOutputStream dos2;
	static DataInputStream dis2;
	static InputStream is2;
	static OutputStream os2;

	static Scanner sc = new Scanner(System.in);
	static DatagramPacket sendPacket, receivePacket;
	static DatagramSocket socket = null;
	static byte[] receiveData = new byte[1024];
	static String fin = "[[-1, -1], [-1, -1], [-1, -1], [-1, -1]]";
	static double x1, x2, x3, y1, y2, y3, angld;
	static int nxt1_coordx, nxt1_coordy, nxt2_coordx, nxt2_coordy, nxt3_coordx,
			nxt3_coordy, Dest_coordx, Dest_coordy;
	static int tr = 0, leader;
	private static Object lock = new Object();
	static int pointx,pointy;
	static int stop_2,stop_3;
	static int reverse,flag;


	public static void main(String[] args) {

		//------------------connecting with three NXT's ---------------
		BTconnect(NXT_Name, NXT_Address, 1);
		BTconnect(NXT2_Name, NXT2_Address, 2);
		BTconnect(NXT3_Name, NXT3_Address, 3);

		connect_sock(); // ---creating connection with image server

		int done = 0;
		Runnable r1 = new Sender_Thread(1);
		Thread t1 = new Thread(r1);
		Runnable r2 = new Sender_Thread(2);
		Thread t2 = new Thread(r2);
		Runnable r3 = new Sender_Thread(3);
		Thread t3 = new Thread(r3);
	
		//---------------------------Main thread -------------------------
		while (true) {
			//------------------receiving coordinates from image server --
			try {
				receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				socket.receive(receivePacket);
				fin = new String(receivePacket.getData(), 0,
						receivePacket.getLength());
			} catch (Exception e) {
				System.err.println("Exception occured in Recieving coordinates......");
			}

			Get_coordinates();	//-----------------parsing coordinates -----
			
			if ((nxt1_coordx > 0 && nxt1_coordy > 0 && nxt2_coordx > 0
					&& nxt2_coordy > 0 && nxt3_coordx > 0 && nxt3_coordy > 0
					&& Dest_coordx > 0 && Dest_coordy > 0)
					&& done == 0) {

				//---starting threads ---
				t1.start();		
				t2.start();
				t3.start();
				done = 1;
				System.out.println("done");
			}
			try{
			//Thread.sleep(10);		//-----------------sleep Thread -------
			}catch(Exception e){
				System.err.println("problem in sleeping ....");
			}
		}
	}
	
	public static void Get_coordinates() {

		String str;
		synchronized (lock) {
			str = new String(fin); // ------- coordinates
		}
		// ---------parsing recieved coordinates -------
		int indexOfOpenBracket = str.indexOf("[");
		int indexOfLastBracket = str.lastIndexOf("]");
		String sp = " ";
		try {
			sp = str.substring(indexOfOpenBracket + 1, indexOfLastBracket);
		} catch (Exception e) {
			System.out.println("nothing");
		}
		Pattern p = Pattern.compile("\\[(.*?)\\]");
		Matcher m = p.matcher(sp);

		ArrayList list = new ArrayList<String>();
		while (m.find()) {
			list.add(m.group(1));
		}

		// -------setting nxt1 coordinates---------
		String tmp_str[] = list.get(0).toString().split(", ");
		nxt1_coordx = Integer.parseInt(tmp_str[0]);
		nxt1_coordy = Integer.parseInt(tmp_str[1]);

		// -------setting nxt2 coordinates---------
		String tmp_str1[] = list.get(1).toString().split(", ");
		nxt2_coordx = Integer.parseInt(tmp_str1[0]);
		nxt2_coordy = Integer.parseInt(tmp_str1[1]);

		// -------setting nxt3 coordinates---------
		String tmp_str2[] = list.get(2).toString().split(", ");
		nxt3_coordx = Integer.parseInt(tmp_str2[0]);
		nxt3_coordy = Integer.parseInt(tmp_str2[1]);

		// -------setting destination coordinates---------
		String tmp_str3[] = list.get(3).toString().split(", ");
		Dest_coordx = Integer.parseInt(tmp_str3[0]);
		Dest_coordy = Integer.parseInt(tmp_str3[1]);

		
		  System.out.println("RECIEVED COORDINATES  nxt1 : [" + nxt1_coordx +
		  ", " + nxt1_coordy + "] " + "nxt2 : [" + nxt2_coordx + ", " +
		  nxt2_coordy + "] " + "nxt3 : [" + nxt3_coordx + ", " + nxt3_coordy +
		  "] " + "Dest : [" + Dest_coordx + ", " + Dest_coordy + "] ");
		 
	}

	// -----------function for connection with image server------
	public static void connect_sock() {
		try {
			socket = new DatagramSocket();

			String s = new String("Hello");
			byte data[] = s.getBytes();

			sendPacket = new DatagramPacket(data, data.length,
					InetAddress.getByName(IP), PORT);
			socket.send(sendPacket);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	// ---------function for establishing bluetooth connection---
	public static void BTconnect(String NXT_Name, String NXT_Address, int nxt) {
		if (nxt == 1) {
			conn = new NXTConnector(); // -------------- new coonection

			// Connect to NXT over Bluetooth
			boolean connected = conn.connectTo(NXT_Name, NXT_Address,
					NXTCommFactory.BLUETOOTH);

			if (!connected) {
				System.err.println("Failed to connect to any NXT");
				System.exit(1);
			}

			is = conn.getInputStream();
			os = conn.getOutputStream();
			dos = new DataOutputStream(os);
			dis = new DataInputStream(is);

			try {
				System.out.println("waiting for handshaking");
				int kk = dis.readInt();

				dos.writeInt(nxt);
				dos.flush();

				kk = dis.readInt();
				System.out.println("connected to : " + kk);
			} catch (Exception e) {
				System.out.println("error in handshaking");
			}
		} else if (nxt == 2) {
			conn = new NXTConnector(); // -------------- new coonection

			// Connect to NXT over Bluetooth
			boolean connected = conn.connectTo(NXT_Name, NXT_Address,
					NXTCommFactory.BLUETOOTH);

			if (!connected) {
				System.err.println("Failed to connect to any NXT");
				System.exit(1);
			}

			is1 = conn.getInputStream();
			os1 = conn.getOutputStream();
			dos1 = new DataOutputStream(os1);
			dis1 = new DataInputStream(is1);

			try {
				System.out.println("waiting for handshaking");
				int kk = dis1.readInt();

				dos1.writeInt(nxt);
				dos1.flush();

				kk = dis1.readInt();

				System.out.println("connected to : " + kk);
			} catch (Exception e) {
				System.out.println("error in handshaking");
			}

		} else if (nxt == 3) {
			conn = new NXTConnector(); // -------------- new coonection

			// Connect to NXT over Bluetooth
			boolean connected = conn.connectTo(NXT_Name, NXT_Address,
					NXTCommFactory.BLUETOOTH);

			if (!connected) {
				System.err.println("Failed to connect to any NXT");
				System.exit(1);
			}

			is2 = conn.getInputStream();
			os2 = conn.getOutputStream();
			dos2 = new DataOutputStream(os2);
			dis2 = new DataInputStream(is2);

			try {
				System.out.println("waiting for handshaking");
				int kk = dis2.readInt();

				dos2.writeInt(nxt);
				dos2.flush();

				kk = dis2.readInt();
				System.out.println("connected to : " + kk);

			} catch (Exception e) {
				System.out.println("error in handshaking");
			}
		}
	}

	// ------------function for closing bluetooth connection-------
	public static void BTclose() {
		try {
			dis.close();
			dos.close();
			conn.close();
		} catch (IOException ioe) {
			System.out.println("IOException closing connection:");
			System.out.println(ioe.getMessage());
		}
	}
}
