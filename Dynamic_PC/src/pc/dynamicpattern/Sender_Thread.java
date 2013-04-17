/*
 * 
 * Thread for Each NXT
 * 
 * */

package pc.dynamicpattern;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Sender_Thread implements Runnable {

	int nxt_name;
	DataOutputStream dos_send = null;
	DataInputStream dis_recv = null;

	public Sender_Thread(int nxt) {
		nxt_name = nxt;
	}

	public void run() {
		try {

			if (nxt_name == 1) {
				dos_send = Dynamic_PC.dos;
				dis_recv = Dynamic_PC.dis;

			} else if (nxt_name == 2) {
				dos_send = Dynamic_PC.dos1;
				dis_recv = Dynamic_PC.dis1;
			} else if (nxt_name == 3) {
				dos_send = Dynamic_PC.dos2;
				dis_recv = Dynamic_PC.dis2;
			}

			System.out.println("Thread : " + nxt_name + " Started.");
			dos_send.writeInt(0);
			dos_send.flush();
			System.out.println("Thread : " + nxt_name + "Initial msg sent.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (true) {
			int function = 0;
			String parts[] = null;
			try {
				byte[] b = new byte[1024];

				String fin = dis_recv.readLine();
				fin = Replace(fin);
				System.out.println("Thread : " + nxt_name + "query recieved - "
						+ fin);
				parts = split(fin, ';');
				function = Integer.parseInt(parts[0]);

			} catch (Exception e) {
				System.err.println("problem in receiving query..");
			}

			if (function == 1) {
				int a1 = Integer.parseInt(parts[1]);
				send_coordinates(a1);
			}
			if (function == 2) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				int a3 = Integer.parseInt(parts[3]);
				Deliver_msg_len(a1, a2, a3);
			}
			if (function == 3) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				int a3 = Integer.parseInt(parts[3]);
				int a4 = Integer.parseInt(parts[4]);
				int a5 = Integer.parseInt(parts[5]);
				int a6 = Integer.parseInt(parts[6]);
				int a7 = Integer.parseInt(parts[7]);
				int a8 = Integer.parseInt(parts[8]);
				int a9 = Integer.parseInt(parts[9]);
				Deliver_msg_xy(a1, a2, a3, a4, a5, a6, a7, a8, a9);
			}
			if (function == 4 || function == 6 || function == 8) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				Deliver_msg_move(a1, a2);
			}
			if (function == 5 || function == 7) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				int a3 = Integer.parseInt(parts[3]);
				int a4 = Integer.parseInt(parts[4]);
				Deliver_msg_coord(a1, a2, a3, a4);
			}
			if (function == 10) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				int a3 = Integer.parseInt(parts[3]);
				Deliver_len(a1, a2, a3);
			}
			if (function == 20) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				int a3 = Integer.parseInt(parts[3]);
				int a4 = Integer.parseInt(parts[4]);
				int a5 = Integer.parseInt(parts[5]);
				int a6 = Integer.parseInt(parts[6]);
				int a7 = Integer.parseInt(parts[7]);
				int a8 = Integer.parseInt(parts[8]);
				conditions(a1, a2, a3, a4, a5, a6, a7, a8);
			}
			if (function == 21) {
				int a1 = Integer.parseInt(parts[1]);
				int a2 = Integer.parseInt(parts[2]);
				del(a1, a2);
			}

		}
	}

	// ------------parsing recieved msg -------------------
	public static String Replace(String fin) {
		String ss = "";
		int i = fin.length();
		char[] a = fin.toCharArray();
		String dig = "0123456789;.-*,:";
		char[] digit = dig.toCharArray();
		for (int j = 0; j < i; j++) {
			// System.out.println(a[j]);
			for (int k = 0; k < dig.length(); k++) {
				if (digit[k] == a[j]) {
					ss = ss + a[j];
					break;
				}
			}
		}
		return ss;
	}

	public void Deliver_msg_coord(int a1, int a2, int a3, int a4) {
		String fin = a1 + ";" + a2 + ";" + a3 + ";" + "00" + ";";
		byte[] bit = fin.getBytes();
		if (a4 == 1) {
			try {
				Dynamic_PC.dos.write(bit);
				Dynamic_PC.dos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a4 == 2) {
			try {
				Dynamic_PC.dos1.write(bit);
				Dynamic_PC.dos1.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a4 == 3) {
			try {
				Dynamic_PC.dos2.write(bit);
				Dynamic_PC.dos2.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dos_send.writeInt(0);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Deliver_msg_len(int a1, int a2, int a3) {
		String fin = a1 + ";" + a2 + ";" + "00" + ";";
		byte[] bit = fin.getBytes();
		if (a3 == 1) {
			try {
				Dynamic_PC.dos.write(bit);
				Dynamic_PC.dos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a3 == 2) {
			try {
				Dynamic_PC.dos1.write(bit);
				Dynamic_PC.dos1.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a3 == 3) {
			try {
				Dynamic_PC.dos2.write(bit);
				Dynamic_PC.dos2.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dos_send.writeInt(0);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Deliver_msg_move(int a1, int a2) {
		String fin = a1 + ";" + "00" + ";";
		byte[] bit = fin.getBytes();
		if (a2 == 1) {
			try {
				Dynamic_PC.dos.write(bit);
				Dynamic_PC.dos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a2 == 2) {
			try {
				Dynamic_PC.dos1.write(bit);
				Dynamic_PC.dos1.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a2 == 3) {
			try {
				Dynamic_PC.dos2.write(bit);
				Dynamic_PC.dos2.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dos_send.writeInt(0);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Deliver_msg_xy(int a1, int a2, int a3, int a4, int a5, int a6,
			int a7, int a8, int a9) {
		String fin = a1 + ";" + a2 + ";" + a3 + ";" + a4 + ";" + a5 + ";" + a6
				+ ";" + a7 + ";" + a8 + ";" + "00" + ";";
		byte[] bit = fin.getBytes();
		if (a9 == 1) {
			try {
				Dynamic_PC.dos.write(bit);
				Dynamic_PC.dos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a9 == 2) {
			try {
				Dynamic_PC.dos1.write(bit);
				Dynamic_PC.dos1.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a9 == 3) {
			try {
				Dynamic_PC.dos2.write(bit);
				Dynamic_PC.dos2.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dos_send.writeInt(0);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int Length(int nxt1x, int nxt1y, int destx, int desty) {
		int len = (int) Math.sqrt((nxt1y - desty) * (nxt1y - desty)
				+ (nxt1x - destx) * (nxt1x - destx));
		return len;
	}

	public void Deliver_len(int a1, int a2, int a3) {

		int ss = 100;
		if (a3 == 1) {
			ss = Length(Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy, a1, a2);
		} else if (a3 == 2) {
			ss = Length(Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy, a1, a2);
		} else if (a3 == 3) {
			ss = Length(Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy, a1, a2);
		}
		String fin = ss + ";" + "00" + ";";
		byte[] bit = fin.getBytes();
		if (a3 == 1) {
			try {
				Dynamic_PC.dos.write(bit);
				Dynamic_PC.dos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a3 == 2) {
			try {
				Dynamic_PC.dos1.write(bit);
				Dynamic_PC.dos1.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (a3 == 3) {
			try {
				Dynamic_PC.dos2.write(bit);
				Dynamic_PC.dos2.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dos_send.writeInt(0);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----sending latest coordinates to NXT
	public void send_coordinates(int compass) {

		int nxt1x = 0, nxt1y = 0, nxt2x = 0, nxt2y = 0, nxt3x = 0, nxt3y = 0, destx = 0, desty = 0;
		if (nxt_name == 1) {
			int k = check_range(Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy,
					Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy, compass);
			if (k == 1) {
				nxt2x = Dynamic_PC.nxt2_coordx;
				nxt2y = Dynamic_PC.nxt2_coordy;
			} else {
				nxt2x = -1;
				nxt2y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy,
					Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy, compass);
			if (k == 1) {
				nxt3x = Dynamic_PC.nxt3_coordx;
				nxt3y = Dynamic_PC.nxt3_coordy;
			} else {
				nxt3x = -1;
				nxt3y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.Dest_coordx, Dynamic_PC.Dest_coordy,
					Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy, compass);

			int kk = 0;
			if (Dynamic_PC.nxt1_coordy < 195 && Dynamic_PC.Dest_coordy < 195) {
				kk = 1;
			}
			if (Dynamic_PC.nxt1_coordy > 195 && Dynamic_PC.Dest_coordy > 195) {
				kk = 1;
			}
			if (k == 1 && kk == 1) {
				destx = Dynamic_PC.Dest_coordx;
				desty = Dynamic_PC.Dest_coordy;
			} else {
				destx = -1;
				desty = -1;
			}
			nxt1x = Dynamic_PC.nxt1_coordx;
			nxt1y = Dynamic_PC.nxt1_coordy;
		} else if (nxt_name == 2) {
			int k = check_range(Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy,
					Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy, compass);
			if (k == 1) {
				nxt1x = Dynamic_PC.nxt1_coordx;
				nxt1y = Dynamic_PC.nxt1_coordy;
			} else {
				nxt1x = -1;
				nxt1y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy,
					Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy, compass);
			if (k == 1) {
				nxt3x = Dynamic_PC.nxt3_coordx;
				nxt3y = Dynamic_PC.nxt3_coordy;
			} else {
				nxt3x = -1;
				nxt3y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.Dest_coordx, Dynamic_PC.Dest_coordy,
					Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy, compass);
			int kk = 0;
			if (Dynamic_PC.nxt1_coordy < 195 && Dynamic_PC.Dest_coordy < 195) {
				kk = 1;
			}
			if (Dynamic_PC.nxt1_coordy > 195 && Dynamic_PC.Dest_coordy > 195) {
				kk = 1;
			}
			if (k == 1 && kk == 1) {
				destx = Dynamic_PC.Dest_coordx;
				desty = Dynamic_PC.Dest_coordy;
			} else {
				destx = -1;
				desty = -1;
			}
			nxt2x = Dynamic_PC.nxt2_coordx;
			nxt2y = Dynamic_PC.nxt2_coordy;
		} else if (nxt_name == 3) {
			int k = check_range(Dynamic_PC.nxt2_coordx, Dynamic_PC.nxt2_coordy,
					Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy, compass);
			if (k == 1) {
				nxt2x = Dynamic_PC.nxt2_coordx;
				nxt2y = Dynamic_PC.nxt2_coordy;
			} else {
				nxt2x = -1;
				nxt2y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.nxt1_coordx, Dynamic_PC.nxt1_coordy,
					Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy, compass);
			if (k == 1) {
				nxt1x = Dynamic_PC.nxt1_coordx;
				nxt1y = Dynamic_PC.nxt1_coordy;
			} else {
				nxt1x = -1;
				nxt1y = -1;
			}
			k = 0;
			k = check_range(Dynamic_PC.Dest_coordx, Dynamic_PC.Dest_coordy,
					Dynamic_PC.nxt3_coordx, Dynamic_PC.nxt3_coordy, compass);

			int kk = 0;
			if (Dynamic_PC.nxt1_coordy < 195 && Dynamic_PC.Dest_coordy < 195) {
				kk = 1;
			}
			if (Dynamic_PC.nxt1_coordy > 195 && Dynamic_PC.Dest_coordy > 195) {
				kk = 1;
			}
			if (k == 1 && kk == 1) {
				destx = Dynamic_PC.Dest_coordx;
				desty = Dynamic_PC.Dest_coordy;
			} else {
				destx = -1;
				desty = -1;
			}
			nxt3x = Dynamic_PC.nxt3_coordx;
			nxt3y = Dynamic_PC.nxt3_coordy;
		}
		String final_string = nxt1x + ";" + nxt1y + ";" + nxt2x + ";" + nxt2y
				+ ";" + nxt3x + ";" + nxt3y + ";" + destx + ";" + desty + ";"
				+ "00" + ";";
		// ------------sending via bluetooth--------------------
		byte[] bit = final_string.getBytes();
		System.out.println("Thread " + nxt_name + " : " + final_string);
		try {
			dos_send.write(bit);
			dos_send.flush();
		} catch (Exception e) {
			System.err.println("Error in sending coordinates to : " + nxt_name);
		}
	}

	public int check_range(int shortx, int shorty, int nxtx, int nxty,
			int compass) {

		int angle = 0;
		int val = 0;
		if (shorty >= nxty) {
			if (nxtx <= shortx) {
				angle = (int) Math.round(Math.toDegrees(Math
						.atan(((double) nxty - (double) shorty)
								/ ((double) nxtx - (double) shortx))));
				if (angle < 0) {
					angle = -angle;
				}
			} else {
				angle = (int) Math.round(Math.toDegrees(Math
						.atan(((double) nxty - (double) shorty)
								/ ((double) nxtx - (double) shortx))));
				if (angle < 0) {
					angle = -angle;
				}
				angle = 180 - angle;
			}
		} else {
			if (nxtx <= shortx) {
				angle = (int) Math.round(Math.toDegrees(Math
						.atan(((double) nxty - (double) shorty)
								/ ((double) nxtx - (double) shortx))));
				if (angle < 0) {
					angle = -angle;
				}
				angle = 360 - angle;
			} else {
				angle = (int) Math.round(Math.toDegrees(Math
						.atan(((double) nxty - (double) shorty)
								/ ((double) nxtx - (double) shortx))));
				if (angle < 0) {
					angle = -angle;
				}
				angle = 180 + angle;
			}
		}
		int min = compass - 50;
		int max = compass + 50;
		int q1, q2;
		if (min < 0) {
			min = -min;
			if ((angle >= (360 - min) && angle <= 360)
					|| (angle <= max && angle >= 0)) {
				val = 1;
			}
		} else if (max > 360) {
			// max = -max;
			int tmp = max - 360;
			if ((angle >= min && angle <= 360) || (angle >= 0 && angle <= tmp)) {
				val = 1;
			}
		} else {
			if (angle >= min && angle <= max) {
				val = 1;
			}
		}
		return val;
	}

	public void conditions(int a1, int a2, int a3, int a4, int a5, int a6,
			int a7, int a8) {
		String fin = a8 + ";" + "00" + ";";
		;

		if (a1 == a2) {
			Dynamic_PC.stop_2 = a3;
			Dynamic_PC.stop_3 = a4;
			Dynamic_PC.reverse = a5;
			Dynamic_PC.pointx = a6;
			Dynamic_PC.pointy = a7;

		}
		if (a8 == 2) {
			Dynamic_PC.stop_3 = a4;
			Dynamic_PC.stop_2 = a4;
		}
		fin = Dynamic_PC.stop_2 + ";" + Dynamic_PC.stop_3 + ";"
				+ Dynamic_PC.reverse + ";" + Dynamic_PC.pointx + ";"
				+ Dynamic_PC.pointy + ";" + "00" + ";";
		System.out.println("STOP 2 : " + Dynamic_PC.stop_2 + "    STOP 3 : "
				+ Dynamic_PC.stop_3);
		try {
			byte[] bit = fin.getBytes();
			dos_send.write(bit);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void del(int a1, int a2) {

		String fin = Dynamic_PC.stop_2 + ";" + Dynamic_PC.stop_3 + ";"
				+ Dynamic_PC.reverse + ";" + Dynamic_PC.pointx + ";"
				+ Dynamic_PC.pointy + ";" + "00" + ";";
		System.out.println("STOP 2 : " + Dynamic_PC.stop_2 + "    STOP 3 : "
				+ Dynamic_PC.stop_3);
		try {
			byte[] bit = fin.getBytes();
			dos_send.write(bit);
			dos_send.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String[] split(String recv_data, char c) {
		int i = 0;
		int p = 0;
		int low = 0, high = 0;
		String part[] = new String[100];
		int len = recv_data.length();
		while (true) {
			if (i >= len - 1)
				break;
			while (recv_data.charAt(i) != ';' && i < len - 1) {
				i = i + 1;
			}
			if (i < len - 1) {
				part[p] = recv_data.substring(low, i);
				p = p + 1;
				low = i + 1;
				i = i + 1;
			} else {
				part[p] = recv_data.substring(low, i);
				break;
			}
		}
		return part;
	}

}
