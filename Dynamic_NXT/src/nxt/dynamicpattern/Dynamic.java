/*
 
 DYNAMIC PATTERN FORMATION :
 
 In this project, three robots are searching for food in two rooms seperated by a wall.
 There is a space in wall for going to other room. The space is dynamic i.e. space can be anywhere throughout the wall.
 When they found food they make triangle and try to get the food. If the position of food changes then they again find food into two rooms.
 To go in other room they try to find way(space)in wall. If they found enough space to pass triangle then they directly come to other room without changing there shapes otherwise they come in Line.
 Once they got food they make circle around it.  
 
 This program is same for all three NXT's. Each NXT has a name in variable my_name (1,2,3) and variable leader says who is leader currently.
 
 SHAPES to be formed :
 	1) Triangle
 	2) Line
 	3) Circle
 	
 */

package nxt.dynamicpattern;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.CompassSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.navigation.TachoPilot;

public class Dynamic {

	// ---------------global variables ---------------------
	static BTConnection btc;
	static DataInputStream dis;
	static DataOutputStream dos;
	static int leader, my_name;
	static int nxt1_coordx, nxt1_coordy, nxt2_coordx, nxt2_coordy, nxt3_coordx,
			nxt3_coordy, dest_coordx, dest_coordy, nxtx, nxty;
	static double x1, x2, x3, y1, y2, y3, angld;
	static int move_angle, flag, line = 0;
	static int region, def = 0;
	static int state = 0, finish = 0, repeat;
	static long t0, t1;
	static int two, one, three, check_t, tri = 0;
	static int left = 1, right = 1, straight = 1, all_dist = 2;
	static int reverse, stop_2, stop_3, pointx, pointy, temp = 1, sec = 1;

	// -------------------------sensors ---------------------
	final static CompassSensor compass = new CompassSensor(SensorPort.S4);
	final static UltrasonicSensor ultra = new UltrasonicSensor(SensorPort.S1);

	// -------------------Tacho pilot-- ---------------------
	final static TachoPilot pilot = new TachoPilot(5.5f, 11.2f, Motor.C,
			Motor.B);

	public static void main(String[] args) {

		// -------connecting with PC via bluetooth --------
		BTconnect();
		// ---------setting pilot speed -------------------
		pilot.setSpeed(50);

		// ----------Initial handshaking ------------------
		try {
			int s = dis.readInt();
			LCD.clear();
			LCD.drawString("Recv init" + s, 0, 2);
			LCD.refresh();

		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Initial Ex", 0, 2);
			LCD.refresh();
		}

		sleep(500);

		// -----loop until gets food ---------------------

		while (true) {
			// -----------reset variables -----------------
			repeat = 0;
			finish = 0;
			line = 0;
			def = 0;
			tri = 0;
			leader = 0;

			fetch_coordinates(); // --latest coordinates

			// --------- setting current region ---------
			if (nxty > 195)
				region = 2;
			else
				region = 1;

			// - initializing NXT's for leader selection -
			Initialize();
			pilot.stop();
			sleep(500);

			/*
			 * -Default case runs when none of NXT found food in current room
			 */
			// ---------------Default case ---------------
			if (leader == 0) {
				if (region == 1)
					default_case(195, 124);
				if (region == 2)
					default_case(195, 266);
				def = 1;
			}
			sleep(500);

			// -----forming shape (triangle/line) -------
			Leader_operation();
			sleep(500);

			// -----------moving forward in shape -------
			move_nxt();

			/*
			 * reinitialize system if food position changes
			 */
			// -------------repeat action ----------------
			if (repeat == 1) {
				continue;
			}
			/*
			 * ------close if reached to food -----------
			 */
			if (finish == 1) {
				break;
			}
			sleep(500);

			/*
			 * -reforming triangle if NXT's are in line
			 */
			if (def == 1) {
				line = 0;
				def = 0;
				initialize_reform();
				if (tri == 0) {
					Reform_triangle();
				}
			}
		}

		// -------closing bluetooth connection -----------
		BTclose();
	}

	// -------function for providing sleep ----------------
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			LCD.drawString("Sleep " + time, 0, 2);
			LCD.refresh();
		}
	}

	/*------ function for initial communication 
	 * before forming triangle from line---------*/
	public static void initialize_reform() {
		String msg = 4 + ";" + 1 + ";";
		pilot.stop();
		if (my_name != leader) {
			sleep(4000);
		}
		if (my_name == leader) {
			String s1 = recv_message();
			s1 = recv_message();
			sleep(100);
			if (my_name == 1) {
				send_message(msg, 2);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				send_message(msg, 2);
			}
		} else {
			String s1;
			if (leader == 1) {
				sleep(100);
				send_message(msg, 1);
				s1 = recv_message();
			} else if (leader == 2) {
				sleep(100);
				send_message(msg, 2);
				s1 = recv_message();
			} else if (leader == 3) {
				sleep(100);
				send_message(msg, 3);
				s1 = recv_message();
			}
		}
		sleep(1000);
	}

	/*-----function for checking obstacle while
	 *  moving------*/
	public static int check_all_dist() {
		int dir = 2, check = 2;
		int tt;
		fetch_coordinates();

		if ((region == 1 && nxty < 195) || (region == 2 && nxty > 195)) {
			// ----checking straight--------
			if (ultra.getDistance() < 40) {
				check = 0;
				pilot.travel(1);
				if (ultra.getDistance() < 40) {
					check = 0;
				}
			} else {
				// ----------checking left ------------
				rotate_ultra_left(60);
				if (ultra.getDistance() < 30) {
					check = -1;
					pilot.travel(1);
					if (ultra.getDistance() < 30) {
						check = -1;
					}
				}
				rotate_ultra_right(60);
				if (check != -1) {
					// ---------checking right -----------
					rotate_ultra_right(60);
					if (ultra.getDistance() < 30) {
						check = 1;
						if (ultra.getDistance() < 30) {
							check = 1;
						}
					}
					rotate_ultra_left(60);
				}
			}
		}
		return check;
	}

	// -------supporting function for making line ---------
	public static void initialize_line() {

		find_places(); // ---finding positions of nxts in line
		straight(); // ---moving forward
		fetch_coordinates();
		String msg;

		if (my_name == leader) {
			msg = 20 + ";" + leader + ";" + my_name + ";" + 0 + ";" + 0 + ";"
					+ 0 + ";" + nxtx + ";" + nxty + ";" + 0 + ";";
			send_msg(msg);
		}
		pilot.setSpeed(50);
		// ----------------------------------------
		int an2 = 0;
		if (region == 1)
			an2 = 90;
		if (region == 2)
			an2 = 270;
		// ----------------------------------------

		if (flag == 2) {
			pilot.stop();
			sleep(7500);
			fetch_coordinates();
			int fixx = nxtx, fixy = nxty;
			int check = 0, range = an2;
			while (true) {
				fetch_coordinates();
				if (check == 3) {
					if (get_compass() < range - 8)
						pilot.arc(0, 5);
					if (get_compass() > range + 8)
						pilot.arc(0, -5);
					check = 0;
				} else
					check++;

				pilot.forward();
				sleep(500);
				if (ultra.getDistance() < 10) {
					int sp = finalangle(get_compass(), an2);
					pilot.arc(0, sp);
					break;
				}
				while (stop_2 == 1) {
					pilot.stop();
					msg = 21 + ";" + 0 + ";" + flag + ";";
					send_msg(msg);
				}
			}
		} else if (flag == 3) {
			pilot.stop();
			sleep(8200);
			fetch_coordinates();
			pilot.travel(17);
			int sp = finalangle(get_compass(), an2);
			pilot.arc(0, sp);
			sleep(12200);
			fetch_coordinates();
			int check = 0, range = an2;
			while (true) {
				fetch_coordinates();
				if (check == 3) {
					if (get_compass() < range - 8)
						pilot.arc(0, 5);
					if (get_compass() > range + 8)
						pilot.arc(0, -5);
					check = 0;
				} else
					check++;
				pilot.forward();
				sleep(500);
				msg = 21 + ";" + 0 + ";" + flag + ";";
				while (stop_2 == 1 || stop_3 == 1) {
					pilot.stop();
					sleep(500);
					send_msg(msg);
				}
				if (ultra.getDistance() < 10) {
					sp = finalangle(get_compass(), an2);
					pilot.arc(0, sp);
					break;
				}
				msg = 21 + ";" + 0 + ";" + flag + ";";
				send_msg(msg);
				while (stop_2 == 1) {
					pilot.stop();
					msg = 21 + ";" + 0 + ";" + flag + ";";
					send_msg(msg);
				}
			}
		}
		fetch_coordinates();
		clear_boundary();
	}

	// ------function for moving straight in line ---------
	public static void straight() {
		pilot.setSpeed(50);
		int br = 0;
		String msg;
		fetch_coordinates();
		int check = 0;
		int range = nxtx;
		if (get_compass() < 180) {
			range = 90;
		} else if (get_compass() > 180) {
			range = 270;
		}
		while (true) {
			// --------------------
			fetch_coordinates();
			if (check == 3) {
				if (get_compass() < range - 8)
					pilot.arc(0, 5);
				if (get_compass() > range + 8)
					pilot.arc(0, -5);
				check = 0;
			} else
				check++;
			pilot.forward();
			sleep(500);
			// ----------------------

			// -----stopping when found wall ----------------
			if (ultra.getDistance() < 10)
				br = 1;
			msg = 4 + ";" + br + ";";
			if (leader == my_name) {
				send_message(msg, two);
				send_message(msg, three);
				String s1 = recv_message();
			} else if (flag == 2) {
				String s1 = recv_message();
				String parts[] = split(s1, ';');
				br = Integer.parseInt(parts[0]);
			} else if (flag == 3) {
				String s1 = recv_message();
				String parts[] = split(s1, ';');
				br = Integer.parseInt(parts[0]);
				send_message(msg, leader);
			}
			if (br == 1)
				break;
		}
	}

	// -function for finding position of each NXT in a Line-
	public static void find_places() {
		int br = 0;
		String msg;
		msg = 4 + ";" + br + ";";
		if (my_name == leader) {
			sleep(1000);
			if (my_name == 1) {
				send_message(msg, 2);
				String s1 = recv_message();
				String parts[] = split(s1, ';');
				int a1 = Integer.parseInt(parts[0]);
				int a2 = Integer.parseInt(parts[1]);
				send_message(msg, 3);
				String s2 = recv_message();
				String part[] = split(s2, ';');
				int b1 = Integer.parseInt(part[0]);
				int b2 = Integer.parseInt(part[1]);
				if (a2 == 2 && b2 == 3) {
					two = a1;
					three = b1;
					one = my_name;
				} else if (a2 == 3 && b2 == 2) {
					two = b1;
					three = a1;
					one = my_name;
				}
				send_message(msg, 2);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				String s1 = recv_message();
				String parts[] = split(s1, ';');
				int a1 = Integer.parseInt(parts[0]);
				int a2 = Integer.parseInt(parts[1]);
				send_message(msg, 3);
				String s2 = recv_message();
				String part[] = split(s2, ';');
				int b1 = Integer.parseInt(part[0]);
				int b2 = Integer.parseInt(part[1]);
				if (a2 == 2 && b2 == 3) {
					two = a1;
					three = b1;
					one = my_name;
				} else if (a2 == 3 && b2 == 2) {
					two = b1;
					three = a1;
					one = my_name;
				}
				send_message(msg, 1);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				String s1 = recv_message();
				String parts[] = split(s1, ';');
				int a1 = Integer.parseInt(parts[0]);
				int a2 = Integer.parseInt(parts[1]);
				send_message(msg, 2);
				String s2 = recv_message();
				String part[] = split(s2, ';');
				int b1 = Integer.parseInt(part[0]);
				int b2 = Integer.parseInt(part[1]);
				if (a2 == 2 && b2 == 3) {
					two = a1;
					three = b1;
					one = my_name;
				} else if (a2 == 3 && b2 == 2) {
					two = b1;
					three = a1;
					one = my_name;
				}
				send_message(msg, 1);
				send_message(msg, 2);
			}
		} else {
			msg = 2 + ";" + my_name + ";" + flag + ";";
			String s1 = recv_message();
			send_message(msg, leader);
			String s2 = recv_message();
		}
	}

	// --- function for finding space in a wall ------------
	public static void clear_boundary() {
		state = 1;
		if (leader == my_name) {
			check_left(); // ---checking left first -----
			clear_operation(0); // ---finding space--------
			if (state == 1) {
				flag = 3;
				leader = three;
				rotate_ultra_left(90);
				rotate_right(190); // -----checking right--
				rotate_ultra_left(90);
				pilot.travel(10);
				clear_operation(1);
			}
			fetch_coordinates();
			sleep(500);
			if (state == 3) {
				travel();
			} else {
				rotate_ultra_right(90);
			}
		} else {
			check_left();
			clear_operation(0);
			if (state == 1) {
				if (flag == 3) {
					leader = my_name;
					flag = 0;
				}
				if (flag == 2) {
					if (my_name == 1) {
						if (leader == 2) {
							leader = 3;
						} else
							leader = 2;
					}
					if (my_name == 2) {
						if (leader == 1) {
							leader = 3;
						} else
							leader = 1;
					}
					if (my_name == 3) {
						if (leader == 2) {
							leader = 1;
						} else
							leader = 2;
					}
				}
				rotate_ultra_left(90);
				rotate_right(180);
				rotate_ultra_left(90);
				pilot.travel(10);
				clear_operation(1);
			}
			fetch_coordinates();

			sleep(500);
			if (state == 3) {
				travel(); // -travel forward in line toward other room
			} else {
				rotate_ultra_right(90);
			}
		}
	}

	// -- function for moving towards other region in line--
	public static void travel() {
		int check = 0;
		fetch_coordinates();
		LCD.drawString("flag: " + flag, 0, 2);
		LCD.refresh();
		int range = nxtx;
		int cond = 0;

		if (region == 1)
			region = 2;
		else if (region == 2)
			region = 1;
		// ----------------------------------------
		int b1 = 0, b2 = 0, b3 = 0, r1 = 0, r2 = 0;
		int an2 = 0;
		if (region == 2) {
			an2 = 90;
			b1 = 350;
			b2 = 290;
			b3 = 230;
			r1 = 230;
			r2 = 270;
			range = an2;
		}
		if (region == 1) {
			an2 = 270;
			b1 = 40;
			b2 = 100;
			b3 = 160;
			r1 = 160;
			r2 = 120;
			range = an2;
		}

		pilot.arc(0, finalangle(get_compass(), an2));

		while (true) {
			fetch_coordinates();
			if (check == 3) {
				if (get_compass() < range - 8)
					pilot.arc(0, 5);
				if (get_compass() > range + 8)
					pilot.arc(0, -5);
				check = 0;
			} else
				check++;
			pilot.travel(2);

			if (my_name == leader) {
				if (cond == 0) {
					if (nxty > r1 && nxty < r2) {
						fetch_coordinates();
						String msg = 20 + ";" + leader + ";" + my_name + ";"
								+ 0 + ";" + 0 + ";" + 0 + ";" + nxtx + ";" + 0
								+ ";" + 0 + ";";
						send_msg(msg);
						cond = 1;
					}
					if (nxty < r1 && nxty > r2) {
						fetch_coordinates();
						String msg = 20 + ";" + leader + ";" + my_name + ";"
								+ 0 + ";" + 0 + ";" + 0 + ";" + nxtx + ";" + 0
								+ ";" + 0 + ";";
						send_msg(msg);
						cond = 1;
					}
				}
				if (nxty > b1 && region == 2) {
					break;
				}
				if (nxty < b1 && region == 1) {
					break;
				}
			}
			if (flag == 2) {

				if (cond == 0) {
					if (nxty > 230 && nxty < 270) {
						String msg = 20 + ";" + my_name + ";" + my_name + ";"
								+ 0 + ";" + 0 + ";" + 0 + ";" + 0 + ";" + 0
								+ ";" + 0 + ";";
						send_msg(msg);
						cond = 1;
					} else if (nxty < 160 && nxty > 120) {
						String msg = 20 + ";" + my_name + ";" + my_name + ";"
								+ 0 + ";" + 0 + ";" + 0 + ";" + 0 + ";" + 0
								+ ";" + 0 + ";";
						send_msg(msg);
						cond = 1;
					}
				}
				if (nxty > b2 && region == 2) {
					break;
				}
				if (nxty < b2 && region == 1) {
					break;
				}
			}
			if (flag == 3) {
				if (nxty > b3 && region == 2) {
					break;
				}
				if (nxty < b3 && region == 1) {
					break;
				}
			}
		}
	}

	/*
	 * --function for moving straight in line if found space in front ---
	 */
	public static void travel_straight() {
		int check = 0;
		fetch_coordinates();
		int range = nxtx;
		int itr = 0;

		if (region == 1)
			region = 2;
		else if (region == 2)
			region = 1;

		int an2 = 0;
		if (region == 2)
			an2 = 90;
		else if (region == 1)
			an2 = 270;
		if (get_compass() < 180) {
			range = 90;
		} else if (get_compass() > 180) {
			range = 270;
		}

		// ----------------------------------------
		while (true) {
			fetch_coordinates();
			if (check == 3) {
				if (get_compass() < range - 8)
					pilot.arc(0, 5);
				if (get_compass() > range + 8)
					pilot.arc(0, -5);
				check = 0;
			} else
				check++;

			pilot.travel(2);

			if (itr == 10) {
				fetch_coordinates();
				int sp = finalangle(get_compass(), an2);
				pilot.arc(0, sp);
				itr = 0;
			} else
				itr++;
			if (my_name == leader) {
				if (nxty > 350 && region == 2) {
					pilot.stop();
					break;
				}
				if (nxty < 40 && region == 1) {
					pilot.stop();
					break;
				}
			}
			if (flag == 2) {
				if (nxty > 290 && region == 2) {
					pilot.stop();
					break;
				}
				if (nxty < 100 && region == 1) {
					pilot.stop();
					break;
				}
			}
			if (flag == 3) {
				if (nxty > 230 && region == 2) {
					pilot.stop();
					break;
				}
				if (nxty < 160 && region == 1) {
					pilot.stop();
					break;
				}
			}
		}
	}

	// ----supporting function for finding space in wall ---
	public static void clear_operation(int side) {
		fetch_coordinates();
		int dist;
		String msg;
		pilot.setSpeed(50);

		while (true) {
			dist = check_distance();
			if (state == 1) {
				if (dist > 30) {
					record_time();
					state = 2;
				}
			} else if (state == 2) {
				if (dist < 30) {
					record_time();
					int space = (int) (t1 - t0);
					if (space < 8000) {
						state = 1;
						continue;
					}
				}
				record_time();
				int space = (int) (t1 - t0);
				if (space > 8000) {
					pilot.stop();
					pilot.travel(-3);
					try {
						if (my_name == leader) {
							msg = 20 + ";" + leader + ";" + my_name + ";" + 1
									+ ";" + 1 + ";" + 0 + ";" + 0 + ";" + 0
									+ ";" + 0 + ";";
							send_msg(msg);
						}
						if (flag == 2) {
							msg = 20 + ";" + leader + ";" + my_name + ";" + 1
									+ ";" + 1 + ";" + 0 + ";" + 0 + ";" + 0
									+ ";" + flag + ";";
							send_msg(msg);
						}

						sleep(500);

						if (side == 0) {
							rotate_right(90);
							rotate_ultra_left(90);
						} else if (side == 1) {
							rotate_left(90);
							rotate_ultra_right(90);
						}
					} catch (Exception e) {
						LCD.drawString("clear", 0, 1);
						LCD.refresh();
					}
					state = 3;
					break;
				}
			}
			fetch_coordinates();

			int distance = ultra.getDistance();
			int a = -5, aa = 5;
			if (side == 1) {
				a = 5;
				aa = -5;
			}
			if (distance > 16 && distance < 30) {
				pilot.arc(0, a);
				pilot.setSpeed(50);
				pilot.forward();
				sleep(2000);
			} else if (distance < 11) {
				pilot.arc(0, aa);
				pilot.setSpeed(50);
				pilot.forward();
				sleep(2000);
			} else {
				pilot.forward();
			}
			sleep(100);

			if (side == 0 && check_margin_left() == 1) {
				if (state != 2) {
					state = 1;
				}
				if (my_name == leader) {
					msg = 20 + ";" + leader + ";" + my_name + ";" + 0 + ";" + 0
							+ ";" + sec + ";" + 0 + ";" + 0 + ";" + 0 + ";";
					send_msg(msg);
					temp++;
					sec++;
				}
				sleep(500);
				break;
			} else if (side == 1 && check_margin_right() == 1) {
				if (state != 2) {
					state = 1;
				}
				if (my_name == leader) {
					msg = 20 + ";" + leader + ";" + my_name + ";" + 0 + ";" + 0
							+ ";" + sec + ";" + 0 + ";" + 0 + ";" + 0 + ";";
					send_msg(msg);
				}
				sleep(500);
				break;
			} else if (state == 3) {
				break;
			} else if (flag == 2) {
				msg = 21 + ";" + 0 + ";" + flag + ";";
				send_msg(msg);

				if (reverse == temp) {
					temp++;
					sec++;
					sleep(500);
					break;
				}
				while (stop_2 == 1) {
					pilot.stop();
					sleep(500);
					send_msg(msg);
				}
			} else if (flag == 3) {
				msg = 21 + ";" + 0 + ";" + flag + ";";
				send_msg(msg);

				if (reverse == temp) {
					temp++;
					sec++;
					sleep(500);
					break;
				}
				while (stop_2 == 1 || stop_3 == 1) {
					pilot.stop();
					sleep(500);
					send_msg(msg);
				}
			}
		}
	}

	// ----checking boundry of arena -----------------------
	public static int check_margin_left() {
		int end = 0;
		fetch_coordinates();
		if (nxtx < 20 && region == 1) {
			end = 1;
		} else if (nxtx > 375 && region == 2) {
			end = 1;
		}
		return end;
	}

	// ----checking boundry of arena -----------------------
	public static int check_margin_right() {
		int end = 0;
		fetch_coordinates();
		if (nxtx > 375 && region == 1) {
			end = 1;
		} else if (nxtx < 20 && region == 2) {
			end = 1;
		}
		return end;
	}

	// -------supporting function of finding space ---------
	public static void record_time() {
		if (state == 1) {
			t0 = System.currentTimeMillis();
			LCD.drawString("Time0: " + t0, 0, 2);
			LCD.refresh();
		} else if (state == 2) {
			t1 = System.currentTimeMillis();
			LCD.drawString("Time1: " + t1, 0, 3);
			LCD.refresh();
		}
	}

	/*
	 * functions for rotating NXT and ultrasonic
	 * */
	public static void check_left() {
		int r = 90;
		rotate_left(r);
		rotate_ultra_right(90);
	}

	public static void check_right() {
		int r = 90;
		rotate_right(r);
		rotate_ultra_left(r);
	}

	public static int check_distance() {
		int dist = 3000;
		dist = ultra.getDistance();
		return dist;
	}

	public static void rotate_right(int turn) {
		pilot.arc(0, 0 - turn);
	}

	public static void rotate_left(int turn) {
		pilot.arc(0, turn);
	}

	public static void rotate_ultra_right(int turn) {
		Motor.A.rotate(turn); // turning ultrasonic sensor
	}

	public static void rotate_ultra_left(int turn) {
		Motor.A.rotate(-turn); // turning ultrasonic sensor
	}

	/*-----------------------------------------------------*/
	
	// --------function for forming triangle from line------
	public static void Reform_triangle() {
		fetch_coordinates();
		sleep(500);

		String msg = 4 + ";" + 1 + ";";
		if (flag == 2) {
			pilot.arc(0, 90);
			sleep(200);
			fetch_coordinates();

			sleep(200);

			if (dest_coordx != -1) {
				pilot.travel(30);
			} else {
				pilot.arc(0, -180);
				sleep(200);
				fetch_coordinates();
				sleep(200);
				if (dest_coordx != -1) {
					pilot.travel(30);
				} else {
					pilot.arc(0, 90);
					check_t = 1;
				}
			}
			if (my_name == 1) {
				send_message(msg, 2);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				send_message(msg, 2);
			}
		} else {
			String s1 = recv_message();
		}
		sleep(500);
	}

	// -----function for avoiding collision of two NXT's ---
	public static int avoid_object(int shortx, int shorty, int d) {
		int angle = -2, teq = -2;
		int d1 = (int) Math.sqrt(((nxtx - shortx) * (nxtx - shortx))
				+ ((nxty - shorty) * (nxty - shorty)));
		if (d < (d1 + 5)) {
			int temp = camera_angle(nxtx, nxty, (int) x1, (int) y1);
			int temp1 = camera_angle(nxtx, nxty, shortx, shorty);

			if ((temp1 > (temp - 30)) && (temp1 < (temp + 30))) {
				if (flag == 2) {
					pilot.arc(0, 90);
					pilot.travel(15);

					fetch_coordinates();
					int an = coord_angle();// get_compass();
					teq = camera_angle(nxtx, nxty, shortx, shorty);
					angle = finalangle(an, temp1);
					pilot.arc(0, angle);
					// Button.waitForPress();
				} else if (flag == 3) {
					pilot.arc(0, -90);
					pilot.travel(15);

					fetch_coordinates();
					int an = get_compass();// coord_angle();
					teq = camera_angle(nxtx, nxty, shortx, shorty);
					angle = finalangle(an, temp1);
					pilot.arc(0, angle);
					// Button.waitForPress();
				}
			}
		}
		return teq;
	}

	// -----function for moving forward in shape form-------
	public static void move_nxt() {

		pilot.setSpeed(50);
		sleep(300);
		fetch_coordinates();
		sleep(300);
		int fixx = nxtx, fixy = nxty;
		int angle = 0;
		int ter = 0;
		int len = 100, range = move_angle;
		fetch_coordinates();
		int temp_destx = dest_coordx, temp_desty = dest_coordy;
		while (true) {
			if (my_name == leader) {
				len = Length(nxtx, nxty, dest_coordx, dest_coordy);
				if (len < 50
						|| (nxtx > dest_coordx - 20 && nxtx < dest_coordx + 20
								&& nxty > dest_coordy - 20 && nxty < dest_coordy + 20)) {
					ter = 1;
				}
				if (def == 0) {
					if ((dest_coordx < (temp_destx - 30) || dest_coordx > (temp_destx + 30))
							|| (dest_coordy < (temp_desty - 30) || dest_coordy > (temp_desty + 30))
							|| dest_coordx == -1) {
						ter = 200;
					}
				}
			}

			sleep(300);

			if (my_name == leader && def == 1) {
				all_dist = check_all_dist();
				if (all_dist == 2) {
				} else if (all_dist == 0) {
					ter = 22;
				} else if (all_dist == -1) {
					ter = 51;
				} else if (all_dist == 1) {
					ter = 52;
				}
				all_dist = 2;

				if (nxty > 350) {
					ter = 100;
					tri = 1;
				} else if (nxty < 40) {
					ter = 100;
					tri = 1;
				}
			} else {
				if (ultra.getDistance() < 15) {
					ter = 007;
				}
			}
			String msg = 2 + ";" + ter + ";" + 1 + ";";
			sleep(500);
			// -------------Exchanging messages ----------------
			if (leader == my_name) {
				if (leader == 1) {
					send_message(msg, 2);
					send_message(msg, 3);
				} else if (leader == 2) {
					send_message(msg, 1);
					send_message(msg, 3);
				} else if (leader == 3) {
					send_message(msg, 1);
					send_message(msg, 2);
				}
			} else {
				String recv = recv_message();
				String parts[] = split(recv, ';');
				ter = Integer.parseInt(parts[0]);
			}
			// ----------------checking conditions -------------
			if (ter == 1) {
				finish = 1;
				repeat = 0;
				circle();
				break;
			}
			if (ter == 22) {
				straight_line();
				if (my_name == leader)
					flag = 0;
				initialize_line();
				break;
			}
			if ((ter == 51 || ter == 52)) {
				straight_line();
				if (my_name == leader)
					flag = 0;
				travel_straight();
				break;
			}
			if (ter == 100) {
				if (my_name == leader)
					flag = 0;
				pilot.stop();
				// line = 1;
				// straight_line();
				tri = 1;
				break;
			}
			if (ter == 200) {
				if (my_name == leader)
					flag = 0;
				repeat = 1;
				break;
			}
			// -------------- aligning -------------------------
			if (ter == 007) {
				pilot.stop();
				sleep(500);
			} else
				pilot.travel(10);

			fetch_coordinates();
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}
			angle = camera_angle(fixx, fixy, nxtx, nxty);

			if (angle > (range + 5)) {
				pilot.arc(0, -5);
			} else if (angle < (range - 5)) {
				pilot.arc(0, 5);
			}
			// -------------------------------------------------
		}
	}

	// ------function for forming shape --------------------
	public static void Leader_operation() {

		if (my_name == leader) {
			int decider = 0;
			if (my_name == 1) {
				x1 = nxt1_coordx;
				y1 = nxt1_coordy;
				decider = 2;
			} else if (my_name == 2) {
				x1 = nxt2_coordx;
				y1 = nxt2_coordy;
				decider = 3;
			} else if (my_name == 3) {
				x1 = nxt3_coordx;
				y1 = nxt3_coordy;
				decider = 1;
			}
			// ---------------deciding coordinates of NXT's ---
			if (def == 1 && region == 1 && line == 0) {
				x1 = 195;
				y1 = 124;
				Find_coordinate(192, 195);
				move_angle = 90;
			} else if (def == 1 && region == 2 && line == 0) {
				x1 = 195;
				y1 = 266;
				Find_coordinate(192, 195);
				move_angle = 270;
			} else if (line == 1 && region == 1 && def == 1) {
				fetch_coordinates();
				x1 = nxtx;
				y1 = nxty;
				x2 = x1;
				y2 = y1 - 60;
				x3 = x1;
				y3 = y2 - 60;
				move_angle = 90;
			} else if (line == 1 && region == 2 && def == 1) {
				fetch_coordinates();
				x1 = nxtx;
				y1 = nxty;
				x2 = x1;
				y2 = y1 + 60;
				x3 = x1;
				y3 = y2 + 60;
				move_angle = 270;
			} else {
				Find_coordinate(dest_coordx, dest_coordy);
				Make_space(dest_coordx, dest_coordy);
				move_angle = find_angle(dest_coordx, dest_coordy, (int) x1,
						(int) y1);
			}
			// ------------------------------------------------
			String msg = 3 + ";" + (int) x1 + ";" + (int) y1 + ";" + (int) x2
					+ ";" + (int) y2 + ";" + (int) x3 + ";" + (int) y3 + ";"
					+ move_angle + ";" + decider + ";";

			LCD.clear();
			LCD.drawString("Leader : " + leader + "," + move_angle, 0, 0);
			LCD.drawString((int) x1 + "," + (int) y1, 0, 3);
			LCD.drawString((int) x2 + "," + (int) y2, 0, 4);
			LCD.drawString((int) x3 + "," + (int) y3, 0, 5);
			// Button.waitForPress();
			LCD.refresh();

			if (my_name == 1) {
				send_message(msg, 2);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				send_message(msg, 2);
			}
			// -------------------------
			Make_triangle((int) x1, (int) y1); // ------forming shape
			// -------------------------
			pilot.stop();
			msg = 4 + ";" + 1 + ";";
			String s1 = recv_message();
			String s2 = recv_message();
			sleep(100);
			if (my_name == 1) {
				send_message(msg, 2);
				sleep(100);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				sleep(100);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				sleep(100);
				send_message(msg, 2);
			}
		} else {
			String s1 = recv_message();
			int decider = 0;
			try {
				String parts[] = split(s1, ';');
				x1 = Integer.parseInt(parts[0]);
				y1 = Integer.parseInt(parts[1]);
				x2 = Integer.parseInt(parts[2]);
				y2 = Integer.parseInt(parts[3]);
				x3 = Integer.parseInt(parts[4]);
				y3 = Integer.parseInt(parts[5]);
				move_angle = Integer.parseInt(parts[6]);
				decider = Integer.parseInt(parts[7]);
				LCD.clear();
				LCD.drawString("Leader : " + leader + "," + move_angle, 0, 0);
				LCD.drawString((int) x1 + "," + (int) y1, 0, 3);
				LCD.drawString((int) x2 + "," + (int) y2, 0, 4);
				LCD.drawString((int) x3 + "," + (int) y3, 0, 5);
				LCD.refresh();

			} catch (Exception e) {
				LCD.clear();
				LCD.drawString("parse dest", 0, 2);
				LCD.refresh();
			}
			// -----------------resolve coordinates ---------------
			int shortx = 0, shorty = 0;
			fetch_coordinates();

			int len1 = Length(nxtx, nxty, (int) x2, (int) y2);
			int len2 = Length(nxtx, nxty, (int) x3, (int) y3);
			int j;
			if (len1 < len2) {
				j = 1;
				shortx = (int) x2;
				shorty = (int) y2;
				flag = 2;
			} else {
				shortx = (int) x3;
				shorty = (int) y3;
				flag = 3;
				j = 2;
			}
			String msg = 5 + ";" + my_name + ";" + len1 + ";" + len2 + ";";

			if (decider == my_name) {
				int k;
				String coord = recv_message();
				String parts[] = split(coord, ';');
				int sender = Integer.parseInt(parts[0]);
				int sender_len1 = Integer.parseInt(parts[1]);
				int sender_len2 = Integer.parseInt(parts[2]);

				if (sender_len1 < sender_len2)
					k = 1;
				else
					k = 2;

				if (j == 1 && k == 1) {
					if (len1 < sender_len1) {
						shortx = (int) x3;
						shorty = (int) y3;
						flag = 3;
						msg = 6 + ";" + 2 + ";";
						send_message(msg, sender);
					} else {
						shortx = (int) x2;
						shorty = (int) y2;
						flag = 2;
						msg = 6 + ";" + 3 + ";";
						send_message(msg, sender);
					}
				} else if (j == 2 && k == 2) {
					if (len2 < sender_len2) {
						shortx = (int) x2;
						shorty = (int) y2;
						flag = 2;
						msg = 6 + ";" + 3 + ";";
						send_message(msg, sender);
					} else {
						shortx = (int) x3;
						shorty = (int) y3;
						flag = 3;
						msg = 6 + ";" + 2 + ";";
						send_message(msg, sender);
					}
				} else {
					msg = 6 + ";" + 0 + ";";
					send_message(msg, sender);
				}
			} else {
				if (my_name == 1) {
					if (leader == 2) {
						send_message(msg, 3);
					} else {
						send_message(msg, 2);
					}
				}
				if (my_name == 2) {
					if (leader == 1) {
						send_message(msg, 3);
					} else {
						send_message(msg, 1);
					}
				}
				if (my_name == 3) {
					if (leader == 2) {
						send_message(msg, 1);
					} else {
						send_message(msg, 2);
					}
				}

				String coord = recv_message();
				String parts[] = split(coord, ';');
				int val = Integer.parseInt(parts[0]);
				if (val == 3) {
					shortx = (int) x3;
					shorty = (int) y3;
					flag = 3;
				} else if (val == 2) {
					shortx = (int) x2;
					shorty = (int) y2;
					flag = 2;
				}
			}

			// ----------------------
			// ----------------------
			Make_triangle(shortx, shorty); // ---forming shape--
			// ----------------------
			pilot.stop();

			if (check_t == 1) {
				sleep(10000);
				check_t = 0;
			} else {
				sleep(4000);
				check_t = 0;
			}
			msg = 4 + ";" + 1 + ";";
			if (leader == 1) {
				sleep(100);
				send_message(msg, 1);
				s1 = recv_message();
			} else if (leader == 2) {
				sleep(100);
				send_message(msg, 2);
				s1 = recv_message();
			} else if (leader == 3) {
				sleep(100);
				send_message(msg, 3);
				s1 = recv_message();
			}
		}
	}

	// ------function for making circle at end -------------
	public static void circle() {
		pilot.setSpeed(40);
		pilot.travel(40);
		fetch_coordinates();
		sleep(1000);
		String msg = 5 + ";" + my_name + ";" + nxtx + ";" + nxty + ";";
		int angle = 0, r = 0, c1, c2;
		if (my_name == 1) {
			String s1 = recv_message();
			send_message(msg, 3);
			String s2 = recv_message();

			String parts[] = split(s1, ';');
			int nx1 = Integer.parseInt(parts[1]);
			int ny1 = Integer.parseInt(parts[2]);

			String part[] = split(s2, ';');
			int nx2 = Integer.parseInt(part[1]);
			int ny2 = Integer.parseInt(part[2]);

			c1 = (nx1 + nx2 + nxtx) / 3;
			c2 = (ny1 + ny2 + nxty) / 3;
			r = (int) Math.sqrt(((c1 - nxtx) * (c1 - nxtx))
					+ ((c2 - nxty) * (c2 - nxty)));

			msg = 5 + ";" + r + ";" + c1 + ";" + c2 + ";";
			send_message(msg, 2);
			send_message(msg, 3);
			angle = camera_angle(nxtx, nxty, c1, c2);

		} else if (my_name == 2) {
			send_message(msg, 1);
			String s1 = recv_message();

			String parts[] = split(s1, ';');
			r = Integer.parseInt(parts[0]);
			c1 = Integer.parseInt(parts[1]);
			c2 = Integer.parseInt(parts[2]);
			angle = camera_angle(nxtx, nxty, c1, c2);

		} else if (my_name == 3) {
			String s1 = recv_message();
			send_message(msg, 1);
			String s2 = recv_message();
			String parts[] = split(s2, ';');
			r = Integer.parseInt(parts[0]);
			c1 = Integer.parseInt(parts[1]);
			c2 = Integer.parseInt(parts[2]);
			angle = camera_angle(nxtx, nxty, c1, c2);
		}
		msg = 5 + ";" + my_name + ";" + nxtx + ";" + nxty + ";";
		if (my_name == leader) {

			pilot.arc(0, finalangle(get_compass(), angle));
			pilot.arc(0, 100);

			String s1 = recv_message();
			String s2 = recv_message();
			if (my_name == 1) {
				send_message(msg, 2);
				send_message(msg, 3);
			} else if (my_name == 2) {
				send_message(msg, 1);
				send_message(msg, 3);
			} else if (my_name == 3) {
				send_message(msg, 1);
				send_message(msg, 2);
			}

		} else if (flag == 2) {

			pilot.arc(0, finalangle(get_compass(), angle));
			pilot.arc(0, 100);
			sleep(7000);
			send_message(msg, leader);
			String g = recv_message();

		} else if (flag == 3) {

			pilot.arc(0, finalangle(get_compass(), angle));
			pilot.arc(0, 100);
			sleep(7000);
			send_message(msg, leader);
			String g = recv_message();
		}

		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		pilot.setSpeed(80);
		pilot.arc(-(r - 2), 720);
		finish = 1;
	}

	public static void straight_line() {

		line = 1;
		def = 1;
		Leader_operation();
		pilot.stop();
	}

	// ---function for finding angle between two coordinnates ----
	public static int find_angle(int shortx, int shorty, int nxtx, int nxty) {
		int angle = 0;
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
		return angle;
	}

	// ----function for finding current angle of NXT ----------
	public static int coord_angle() {
		int angle = 0;
		fetch_coordinates();
		int fixx = nxtx, fixy = nxty;
		pilot.travel(10);
		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		fetch_coordinates();
		angle = camera_angle(fixx, fixy, nxtx, nxty);
		pilot.travel(-10);
		return angle;
	}

	// ----function for finding optimized angle for rotation --
	public static int finalangle(int x, int y) {
		int temp = 0, temp1 = 0;
		int fin = 0;
		if (x > y) {
			temp = x - y;
			temp1 = (360 - x) + y;
			if (temp >= temp1) {
				fin = temp1;
			} else {
				fin = -temp;
			}
		} else if (x < y) {
			temp = y - x;
			temp1 = 360 - (y - x);
			if (temp <= temp1) {
				fin = temp;
			} else {
				fin = -temp1;
			}
		}
		return fin;
	}

	public static void maintain_distance() {
		int lx = 0, ly = 0;

		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		fetch_coordinates();
		if (leader == 1) {
			lx = nxt1_coordx;
			ly = nxt1_coordy;
		} else if (leader == 2) {
			lx = nxt2_coordx;
			ly = nxt2_coordy;
		} else if (leader == 3) {
			lx = nxt3_coordx;
			ly = nxt3_coordy;
		}
		LCD.clear();
		while (true) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			fetch_coordinates();

			int len = Length(nxtx, nxty, (int) lx, (int) ly);

			LCD.drawInt(len, 0, 1);
			LCD.refresh();
			if (len > 65 && len < 80) {
				break;
			} else {
				if (len > 80) {
					pilot.travel(1);
				}
				if (len < 65) {
					pilot.travel(-1);
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	// ---------function for shape formation ------------------
	public static void Make_triangle(int shortx, int shorty) {

		if (my_name == leader) {
			int angle = 0;
			int an1, an2;
			fetch_coordinates();
			int len = Length(nxtx, nxty, (int) x1, (int) y1);

			if ((nxtx > (int) x1 - 10 && nxtx < (int) x1 + 10
					&& nxty > (int) y1 - 10 && nxty < (int) y1 + 10)
					|| len < 15) {
			} else {
				angle = coord_angle();
				// -----------------
				int cmp = get_compass();
				int p1 = cmp - 20;
				if (p1 < 0) {
					p1 = 360 + p1;
				}
				int p2 = cmp + 20;
				if (p2 > 360) {
					p2 = p2 - 360;
				}
				if ((angle >= p1 && angle <= p2)) {
					an1 = angle;
				} else
					angle = get_compass();
				// -----------------
				an1 = angle;

				if (line == 1)
					an1 = get_compass();

				sleep(500);
				fetch_coordinates();
				angle = camera_angle(nxtx, nxty, (int) x1, (int) y1);
				an2 = angle;

				int t;
				pilot.arc(0, t = finalangle(an1, an2));

			}

			sleep(500);

			int range = angle;
			int fixx = 0, fixy = 0;

			sleep(500);

			fetch_coordinates();
			fixx = nxtx;
			fixy = nxty;
			while (true) {

				len = Length(nxtx, nxty, (int) x1, (int) y1);
				if ((nxtx > (int) x1 - 8 && nxtx < (int) x1 + 8
						&& nxty > (int) y1 - 8 && nxty < (int) y1 + 8)
						|| len < 15) {
					break;
				}

				pilot.travel(8);
				sleep(300);
				fetch_coordinates();
				sleep(300);
				angle = camera_angle(fixx, fixy, nxtx, nxty);

				if (angle > (range + 5)) {
					pilot.arc(0, -5);
				} else if (angle < (range - 5)) {
					pilot.arc(0, 5);
				}
			}
			sleep(500);
			fetch_coordinates();
			an1 = coord_angle();

			// -----------------
			int cmp = get_compass();
			int p1 = cmp - 20;
			if (p1 < 0) {
				p1 = 360 + p1;
			}
			int p2 = cmp + 20;
			if (p2 > 360) {
				p2 = p2 - 360;
			}
			if ((an1 >= p1 && an1 <= p2)) {
				an1 = an1;
			} else
				an1 = get_compass();
			// -----------------
			if (line == 1)
				an1 = get_compass();

			sleep(500);
			an2 = move_angle;
			int t;
			pilot.arc(0, t = finalangle(an1, an2));

		} else {
			int an1, an2;
			fetch_coordinates();
			int angle = coord_angle();
			// -----------------
			int cmp = get_compass();
			int p1 = cmp - 18;
			if (p1 < 0) {
				p1 = 360 + p1;
			}
			int p2 = cmp + 18;
			if (p2 > 360) {
				p2 = p2 - 360;
			}
			if ((angle >= p1 && angle <= p2)) {
				an1 = angle;
			} else
				angle = get_compass();
			// -----------------
			an1 = angle;
			LCD.drawString("zero Angle : " + angle, 0, 5);
			LCD.refresh();

			if (line == 1)
				an1 = get_compass();

			fetch_coordinates();

			angle = camera_angle(nxtx, nxty, shortx, shorty);
			an2 = angle;
			int t;
			pilot.arc(0, t = finalangle(an1, an2));

			int range = angle, oldangle = angle;
			fetch_coordinates();
			int fixx = nxtx;
			int fixy = nxty;
			int len = 0;
			while (true) {
				while (ultra.getDistance() < 20) {
					sleep(500);
				}
				len = Length(nxtx, nxty, shortx, shorty);
				if (len <= 15
						|| (nxtx > shortx - 8 && nxtx < shortx + 8
								&& nxty > shorty - 8 && nxty < shorty + 8)) {
					pilot.stop();
					break;
				}
				while (ultra.getDistance() < 20) {
					sleep(500);
				}
				// -----------------
				int d = Length(nxtx, nxty, (int) x1, (int) y1);
				if (d < 80) {
					int aa = avoid_object(shortx, shorty, d);
					if (aa != -2) {
						sleep(300);
						fetch_coordinates();
						fixx = nxtx;
						fixy = nxty;
						range = aa;
						oldangle = aa;
					}
				}

				while (ultra.getDistance() < 20) {
					sleep(500);
				}
				pilot.travel(8);
				sleep(300);
				fetch_coordinates();

				sleep(300);
				angle = camera_angle(fixx, fixy, nxtx, nxty);

				if (angle > (range + 5)) {
					pilot.arc(0, -5);
				} else if (angle < (range - 5)) {
					pilot.arc(0, 5);
				}
				oldangle = angle;

				while (ultra.getDistance() < 20) {
					sleep(500);
				}
				sleep(500);
				while (ultra.getDistance() < 20) {
					sleep(500);
				}
			}
			// --------------
			sleep(300);
			fetch_coordinates();
			sleep(300);
			an1 = coord_angle();

			// -----------------
			cmp = get_compass();
			p1 = cmp - 18;
			if (p1 < 0) {
				p1 = 360 + p1;
			}
			p2 = cmp + 18;
			if (p2 > 360) {
				p2 = p2 - 360;
			}
			if ((an1 >= p1 && an1 <= p2)) {
				an1 = an1;
			} else
				an1 = get_compass();
			// -----------------
			LCD.drawString("zero Angle : " + an1, 0, 5);
			LCD.refresh();

			an2 = move_angle;
			if (line == 1)
				an1 = get_compass();
			pilot.arc(0, t = finalangle(an1, an2));

		}
	}

	// ---function for finding angle between two coordinnates ----
	public static int camera_angle(int cur_x, int cur_y, int lat_x, int lat_y) {
		// int cur_x=0,cur_y=0; //fixed coordinate
		// int lat_x=0,lat_y=0; //changing coordinate
		int angle = 0;
		if (lat_y >= cur_y) {
			if (lat_x >= cur_x) {
				angle = (int) Math.toDegrees(Math
						.atan(((double) lat_y - (double) cur_y)
								/ ((double) lat_x - (double) cur_x)));
				// System.out.println("The value is"+angle);
			} else {
				angle = 180 + (int) Math.toDegrees(Math
						.atan(((double) lat_y - (double) cur_y)
								/ ((double) lat_x - (double) cur_x)));
				// System.out.println("The value is"+angle);
			}
		} else if (lat_y < cur_y) {
			if (lat_x <= cur_x) {
				angle = 180 + (int) Math.toDegrees(Math
						.atan(((double) lat_y - (double) cur_y)
								/ ((double) lat_x - (double) cur_x)));
				// System.out.println("The value is"+angle);
			} else {
				angle = 360 + (int) Math.toDegrees(Math
						.atan(((double) lat_y - (double) cur_y)
								/ ((double) lat_x - (double) cur_x)));
				// System.out.println("The value is"+angle);
			}
		}
		return angle;
	}

	//-------function for selecting leader ---------------------
	public static void Initialize() {
		try {
			// -----------------------------
			// -----------------------------
			if (my_name == 1) {
				sleep(5000);
				int ii = 0;
				while (ii < 5) {

					int decision = 0;
					if (dest_coordx != -1) {
						decision = 1;
					}

					String msg;
					int len;
					if (decision == 1)
						len = Length(nxt1_coordx, nxt1_coordy, dest_coordx,
								dest_coordy);
					else
						len = -1;
					msg = 2 + ";" + decision + ";" + len + ";";
					send_message(msg, 3);

					String lead = recv_message(); // ----------------
					String parts[] = split(lead, ';');
					int a1 = Integer.parseInt(parts[0]);
					if (a1 == -1) {
						pilot.arc(0, 100);
						fetch_coordinates(); // ------fetching latest
												// coordinates--
						ii++;
						continue;
					} else {
						leader = a1;
						break;
					}
				}
			}
			// -----------------------------
			// -----------------------------
			else if (my_name == 2) {
				int ii = 0;
				while (ii < 5) {
					int decision = 0;
					if (dest_coordx != -1) {
						decision = 1;
					}

					String msg;
					int len;
					if (decision == 1)
						len = Length(nxt2_coordx, nxt2_coordy, dest_coordx,
								dest_coordy);
					else
						len = -1;
					msg = 2 + ";" + decision + ";" + len + ";";
					String reply_1 = recv_message();
					send_message(msg, 3);

					String lead = recv_message(); // -------------
					String parts[] = split(lead, ';');
					int a1 = Integer.parseInt(parts[0]);
					if (a1 == -1) {
						pilot.arc(0, 100);
						fetch_coordinates(); // ------fetching latest
												// coordinates--
						ii++;
						continue;
					} else {
						leader = a1;
						break;
					}
				}
			}
			// -----------------------------
			// -----------------------------
			else if (my_name == 3) {
				int ii = 0;
				while (ii < 5) {
					int decision = 0;
					if (dest_coordx != -1) {
						decision = 1;
					}
					String msg;
					int len;
					if (decision == 1)
						len = Length(nxt3_coordx, nxt3_coordy, dest_coordx,
								dest_coordy);
					else
						len = -1;
					msg = 2 + ";" + decision + ";" + len + ";";
					String reply_1 = recv_message();
					send_message(msg, 2);
					String reply_2 = recv_message();

					// ------computing -----------

					String parts[] = split(reply_1, ';');
					int a1 = Integer.parseInt(parts[1]);

					String parts1[] = split(reply_2, ';');
					int a2 = Integer.parseInt(parts1[1]);

					if (len == -1 && a1 == -1 && a2 == -1) {
						ii++;
						msg = 2 + ";" + -1 + ";" + len + ";";
						send_message(msg, 1);
						send_message(msg, 2);
						pilot.arc(0, 100);
						fetch_coordinates(); // ------fetching latest
												// coordinates--
						continue;
					} else {
						int min = 1000;
						if (len != -1 && len < min) {
							min = len;
							leader = 3;
						}
						if (a1 != -1 && a1 < min) {
							min = a1;
							leader = 1;
						}
						if (a2 != -1 && a2 < min) {
							min = a2;
							leader = 2;
						}

						msg = 2 + ";" + leader + ";" + leader + ";";
						send_message(msg, 1);
						send_message(msg, 2);
						break;
					}
				}
			}

			LCD.clear();
			LCD.drawString("Leader : " + leader, 0, 1);
			// Button.waitForPress();
			LCD.refresh();
			// ----------------leader form triangle ----------
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Leader Ex", 0, 2);
			LCD.refresh();
		}
	}

	//---------function for selecting leader in default case --
	public static void default_case(int desx, int desy) {
		try {
			// -----------------------------
			// -----------------------------
			if (my_name == 1) {
				String msg;
				int len;
				len = Length(nxt1_coordx, nxt1_coordy, desx, desy);
				msg = 2 + ";" + 0 + ";" + len + ";";
				send_message(msg, 3);

				String lead = recv_message(); // ----------------
				String parts[] = split(lead, ';');
				int a1 = Integer.parseInt(parts[0]);
				leader = a1;
			}
			// -----------------------------
			// -----------------------------
			else if (my_name == 2) {

				String msg;
				int len;
				len = Length(nxt2_coordx, nxt2_coordy, desx, desy);

				msg = 2 + ";" + 0 + ";" + len + ";";
				String reply_1 = recv_message();
				send_message(msg, 3);

				String lead = recv_message(); // -------------
				String parts[] = split(lead, ';');
				int a1 = Integer.parseInt(parts[0]);
				leader = a1;
			}
			// -----------------------------
			// -----------------------------
			else if (my_name == 3) {
				String msg;
				int len;
				len = Length(nxt3_coordx, nxt3_coordy, desx, desy);
				msg = 2 + ";" + 0 + ";" + len + ";";
				String reply_1 = recv_message();
				send_message(msg, 2);
				String reply_2 = recv_message();

				// ------computing -----------

				String parts[] = split(reply_1, ';');
				int a1 = Integer.parseInt(parts[1]);

				String parts1[] = split(reply_2, ';');
				int a2 = Integer.parseInt(parts1[1]);

				int min = 1000;
				if (len < min) {
					min = len;
					leader = 3;
				}
				if (a1 < min) {
					min = a1;
					leader = 1;
				}
				if (a2 < min) {
					min = a2;
					leader = 2;
				}

				msg = 2 + ";" + leader + ";" + leader + ";";
				send_message(msg, 1);
				send_message(msg, 2);
			}

			LCD.clear();
			LCD.drawString("Leader : " + leader, 0, 1);
			// Button.waitForPress();
			LCD.refresh();
			// ----------------leader form triangle ----------
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Leader Ex", 0, 2);
			LCD.refresh();
		}
	}

	//------function for finding length between two coordinates--
	public static int Length(int nxt1x, int nxt1y, int destx, int desty) {
		int len = (int) Math.sqrt((nxt1y - desty) * (nxt1y - desty)
				+ (nxt1x - destx) * (nxt1x - destx));
		return len;
	}

	//------function for sending msg to other nxt's ------------
	public static void send_message(String s, int nxt) {
		String msg = s + nxt + ";" + "00" + ";\n";
		try {
			dos.writeChars(msg);
			dos.flush();

			int recv = dis.readInt();
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Send Ex", 0, 0);
			LCD.refresh();
		}
	}

	public static void send_msg(String s) {
		String msg = s + my_name + ";" + "00" + ";\n";
		try {
			dos.writeChars(msg);
			dos.flush();

			byte[] b = new byte[1024];
			int tmp = dis.read(b);
			String fin = new String(b);
			String parts[] = split(fin, ';');
			stop_2 = Integer.parseInt(parts[0]);
			stop_3 = Integer.parseInt(parts[1]);
			reverse = Integer.parseInt(parts[2]);
			pointx = Integer.parseInt(parts[3]);
			pointy = Integer.parseInt(parts[4]);
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Send Ex", 0, 0);
			LCD.refresh();
		}
	}

	//------function for receiving msg from other nxt----------
	public static String recv_message() {
		String fin = null;
		try {
			byte[] b = new byte[1024];
			int tmp = dis.read(b);
			fin = new String(b);

		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Recieve Ex", 0, 0);
			LCD.refresh();
		}
		return fin;
	}

	//------function for fetching latest coordinates ----------
	public static void fetch_coordinates() {

		try {
			String tmp = 1 + ";" + get_compass() + ";" + "00" + ";\n";
			dos.writeChars(tmp);
			dos.flush();
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("writing", 0, 2);
			LCD.refresh();
		}
		try {
			byte[] b = new byte[1024];
			int tmp = dis.read(b);
			String fin = new String(b); // --recieving coordinates
			String parts[] = split(fin, ';');
			nxt1_coordx = Integer.parseInt(parts[0]);
			nxt1_coordy = Integer.parseInt(parts[1]);
			nxt2_coordx = Integer.parseInt(parts[2]);
			nxt2_coordy = Integer.parseInt(parts[3]);
			nxt3_coordx = Integer.parseInt(parts[4]);
			nxt3_coordy = Integer.parseInt(parts[5]);
			dest_coordx = Integer.parseInt(parts[6]);
			dest_coordy = Integer.parseInt(parts[7]);

			if (my_name == 1) {
				nxtx = nxt1_coordx;
				nxty = nxt1_coordy;
			} else if (my_name == 2) {
				nxtx = nxt2_coordx;
				nxty = nxt2_coordy;
			} else if (my_name == 3) {
				nxtx = nxt3_coordx;
				nxty = nxt3_coordy;
			}
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("Recv Main", 0, 2);
			LCD.refresh();
		}
	}

	//-------get angle from campass --------------------------- 
	public static int get_compass() {
		int my_angle;
		if (compass.getDegrees() <= 190)
			my_angle = 190 - (int) compass.getDegrees();
		else
			my_angle = 360 - ((int) compass.getDegrees() - 190);
		return my_angle;
	}

	//----optimizing coordinates of triangle based on arena ----
	public static void Make_space(int Dest_coordx, int Dest_coordy) {

		double xmin = 30, xmax = 360;
		double ymin = 30, ymax = 360;
		double midx = 195, midy = 195;

		if (region == 1) {
			ymin = 30;
			ymax = 165;
		} else if (region == 2) {
			ymin = 125;
			ymax = 360;
		}
		int leader_mid_len = (int) Math.sqrt((midy - y1) * (midy - y1)
				+ (midx - x1) * (midx - x1));

		double leader_mid_angle_quad1 = Math.toDegrees(Math.atan((y1 - midy)
				/ (x1 - midx)));
		double leader_mid_angle_quad2 = Math.toDegrees(Math.atan((y1 - midy)
				/ (midx - x1)));
		double leader_mid_angle_quad3 = Math.toDegrees(Math.atan((midy - y1)
				/ (midx - x1)));
		double leader_mid_angle_quad4 = Math.toDegrees(Math.atan((midy - y1)
				/ (x1 - midx)));
		int len = leader_mid_len;

		// System.out.println("leader_mid_length : "+leader_mid_len);
		// System.out.println("leader_mid_angle_quad1 : "+leader_mid_angle_quad1);
		// System.out.println("leader_mid_angle_quad2 : "+leader_mid_angle_quad2);
		// System.out.println("leader_mid_angle_quad3 : "+leader_mid_angle_quad3);
		// System.out.println("leader_mid_angle_quad4 : "+leader_mid_angle_quad4);

		while (x2 > xmax || x2 < xmin || y2 > ymax || y2 < ymin || x3 > xmax
				|| x3 < xmin || y3 > ymax || y3 < ymin) {
			len = len - 1;
			if (y1 > midy) {
				if (x1 > midx) {

					// System.out.println("quadrant 1");
					xy_coordinate(leader_mid_angle_quad1, midx, midy, len, 1);

					Find_coordinate(Dest_coordx, Dest_coordy);

				} else {
					// System.out.println("quadrant 2");
					xy_coordinate(leader_mid_angle_quad2, midx, midy, len, 2);

					Find_coordinate(Dest_coordx, Dest_coordy);
				}
			} else {
				if (x1 > midx) {
					// System.out.println("quadrant 4");
					xy_coordinate(leader_mid_angle_quad3, midx, midy, len, 4);

					Find_coordinate(Dest_coordx, Dest_coordy);

				} else {
					// System.out.println("quadrant 3");
					xy_coordinate(leader_mid_angle_quad4, midx, midy, len, 3);

					Find_coordinate(Dest_coordx, Dest_coordy);
				}
			}
			// System.out.println("x1,y1 : "+x1+","+y1 +
			// " x2,y2 : "+x2+","+y2+" x3,y3 : "+x3+","+y3);
		}
		// System.out.println("x1,y1 : "+x1+","+y1 +
		// " x2,y2 : "+x2+","+y2+" x3,y3 : "+x3+","+y3);
	}

	public static void xy_coordinate(double angle, double midx, double midy,
			int len, int quadrant) {
		if (angle == 90)
			angle = 89;

		double tan = Math.tan(Math.toRadians(angle));
		double K = -tan * midx + midy;
		double K1 = K * K + midy * midy + midx * midx - 2 * midy * K - len
				* len;

		double a = 1 + tan * tan;
		double b = 2 * K * tan - 2 * midy * tan - 2 * midx;
		double c = K1;

		// System.out.println("a , b , c : "+a+ " "+b + " " + c);
		double temp1 = Math.sqrt(b * b - 4 * a * c);
		double root1 = (-b + temp1) / (2 * a);
		double root2 = (-b - temp1) / (2 * a);

		double tmp_1 = tan * root1 - tan * midx + midy;
		double tmp_2 = tan * root2 - tan * midx + midy;

		double x = 0.0, y = 0.0;
		if (quadrant == 1) {
			if (root1 > root2)
				x = root1;
			else
				x = root2;
			if (tmp_1 > tmp_2)
				y = tmp_1;
			else
				y = tmp_2;
		} else if (quadrant == 2) {
			if (root1 < root2)
				x = root1;
			else
				x = root2;
			if (tmp_1 > tmp_2)
				y = tmp_1;
			else
				y = tmp_2;
		} else if (quadrant == 3) {
			if (root1 < root2)
				x = root1;
			else
				x = root2;
			if (tmp_1 < tmp_2)
				y = tmp_1;
			else
				y = tmp_2;
		} else if (quadrant == 4) {
			if (root1 > root2)
				x = root1;
			else
				x = root2;
			if (tmp_1 < tmp_2)
				y = tmp_1;
			else
				y = tmp_2;
		}
		x1 = Math.round(x);
		y1 = Math.round(y);
		// System.out.println("x : "+ x1 + " y : " + y1);
		// Scanner sc=new Scanner(System.in);
		// int f=sc.nextInt();
	}

	//----function for finding coordinates of equilateral triangle -
	public static void Find_coordinate(int Dest_coordx, int Dest_coordy) {
		double dx = Dest_coordx, dy = Dest_coordy;
		if (def == 1) {
			dx = 192;
			dy = 195;
		}
		double alpha = 0, beta = 0;
		double d = 60;
		if (((dx > x1) && (dy > y1)) || ((dx < x1) && (dy < y1))) {
			angld = Math.toDegrees(Math.atan((dy - y1) / (dx - x1)));
			alpha = 180 - (180 - angld - 30);
			beta = 180 - (180 - angld + 30);
		} else {
			angld = 180 + Math.toDegrees(Math.atan((dy - y1) / (dx - x1)));
			alpha = 180 - (180 - angld - 30);
			beta = 180 - (180 - angld + 30);
		}

		double temp1x = 0, temp2x = 0, temp1y, temp2y, dis;
		double temp = 1 + Math.tan(Math.toRadians(alpha))
				* Math.tan(Math.toRadians(alpha));

		temp1x = x1 + Math.sqrt((d * d) / temp);
		temp2x = x1 - Math.sqrt((d * d) / temp);

		temp1y = y1 - ((x1 - temp1x) * Math.tan(Math.toRadians(alpha)));
		temp2y = y1 - ((x1 - temp2x) * Math.tan(Math.toRadians(alpha)));

		if (((dx > x1) && (dy > y1)) || ((dx < x1) && (dy < y1))) {
			if ((dx > x1) && (dy > y1)) {
				if (temp1y < y1) {
					x2 = temp1x;
					y2 = temp1y;
				} else {
					x2 = temp2x;
					y2 = temp2y;
				}
			} else {
				if (temp1y > y1) {
					x2 = temp1x;
					y2 = temp1y;
				} else {
					x2 = temp2x;
					y2 = temp2y;
				}
			}
		} else if (((dx < x1) && (dy > y1)) || ((dx > x1) && (dy < y1))) {
			if ((dx < x1) && (dy > y1)) {
				if (temp1x > x1) {
					x2 = temp1x;
					y2 = temp1y;
				} else {
					x2 = temp2x;
					y2 = temp2y;
				}
			} else {
				if (temp1x < x1) {
					x2 = temp1x;
					y2 = temp1y;
				} else {
					x2 = temp2x;
					y2 = temp2y;
				}
			}
		}

		temp = 1 + Math.tan(Math.toRadians(beta))
				* Math.tan(Math.toRadians(beta));
		temp1x = x1 + Math.sqrt((d * d) / temp);
		temp2x = x1 - Math.sqrt((d * d) / temp);
		temp1y = y1 - (x1 - temp1x) * Math.tan(Math.toRadians(beta));
		temp2y = y1 - (x1 - temp2x) * Math.tan(Math.toRadians(beta));
		if (((dx > x1) && (dy > y1)) || ((dx < x1) && (dy < y1))) {
			if ((dx > x1) && (dy > y1)) {
				if (temp1x < x1) {
					x3 = temp1x;
					y3 = temp1y;
				} else {
					x3 = temp2x;
					y3 = temp2y;
				}
			} else {
				if (temp1x > x1) {
					x3 = temp1x;
					y3 = temp1y;
				} else {
					x3 = temp2x;
					y3 = temp2y;
				}
			}
		} else if (((dx < x1) && (dy > y1)) || ((dx > x1) && (dy < y1))) {
			if ((dx < x1) && (dy > y1)) {
				if (temp1y < y1) {
					x3 = temp1x;
					y3 = temp1y;
				} else {
					x3 = temp2x;
					y3 = temp2y;
				}
			} else {
				if (temp1y > y1) {
					x3 = temp1x;
					y3 = temp1y;
				} else {
					x3 = temp2x;
					y3 = temp2y;
				}
			}
		}
	}

	//----------function for parsing received message ----------- 
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

	// ----------------Establishing Bluetooth connection --------
	public static void BTconnect() {

		LCD.drawString("waiting...", 0, 0);
		LCD.refresh();
		try {
			btc = Bluetooth.waitForConnection();
			// ------opening input stream
			dis = btc.openDataInputStream();
			// ------opening output stream
			dos = btc.openDataOutputStream();

			dos.writeInt(1);
			dos.flush();

			int nxt = dis.readInt();

			my_name = nxt;
			dos.writeInt(my_name);
			dos.flush();

			LCD.clear();
			LCD.drawString("conn PC " + my_name, 0, 0);
			LCD.refresh();
		} catch (Exception e) {
			LCD.clear();
			LCD.drawString("IO Exception", 0, 0);
			LCD.refresh();
			System.exit(1);
		}
	}

	// ------------Bluetooth Connection Termination -------------

	public static void BTclose() {
		try {
			dis.close();
			dos.close();
			Thread.sleep(100); // wait for data to drain
			LCD.clear();
			LCD.drawString("closing..", 0, 0);
			LCD.refresh();
			btc.close();
			LCD.clear();
		} catch (Exception e) {
			LCD.drawString("Close Exception", 0, 0);
		}
	}
}
