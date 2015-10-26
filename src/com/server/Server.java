package com.server;

import java.io.IOException;

public abstract class Server {
	public static void main(String[] args){
		//initiate three different udp instances of three servers
		UDPServer udpNA=new UDPServer(1411, new GameServerNA());
		udpNA.start();
		UDPServer udpEU=new UDPServer(1412, new GameServerEU());
		udpEU.start();
		UDPServer udpAS=new UDPServer(1413, new GameServerAS());
		udpAS.start();
		System.out.print("All UDPs up!!");
	}

	public abstract String calculateStatus() throws IOException;

	public abstract String createPlayerAccount(String string, String string2,
			int parseInt, String string3, String string4, String string5) throws IOException;
	
}
