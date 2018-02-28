package com.ardurasolutions.safekiddo.helpers;

import java.io.IOException;
import java.net.ServerSocket;

public class Network {
	
	public static boolean isLocalPortisFree(int port) {
		ServerSocket socket= null;
		try {
			socket = new ServerSocket(port);
			return true;
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}
	
	public static int findFreeLocalPort() {
		ServerSocket socket= null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;		
	}

}
