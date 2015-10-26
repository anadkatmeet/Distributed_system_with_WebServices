package com.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.omg.CORBA.StringHolder;

public class UDPServer extends Thread{
	int port;
	Server server;
	
	public UDPServer(int port,Server server){
		this.server=server;
		this.port=port;
	}
	
	public void run(){
		//System.out.print(Thread.currentThread().getName());
		DatagramSocket aSocket = null;
		
		
		try {
			aSocket=new DatagramSocket(port);
			while(true){
					
					byte[] buffer = new byte[64000];	//kept inside the while loop so its initiated on every request
					DatagramPacket request = new DatagramPacket(buffer,buffer.length);
					aSocket.receive(request);
					
					String temp=new String(request.getData());
					
					temp=temp.trim();
					//when server passes "call", it means call calculate status method
					if (temp.contains("call")) {
						
						buffer=server.calculateStatus().getBytes();
					}
					else if(temp.contains("-")){	//this means request came from transferAccount method
						String[] userInfo=temp.split("-");
						//userinfo 0-unm,1-pwd,2-fnm,3-lnm,4-status,5-age,6-ip
						
						String reply1=server.createPlayerAccount(userInfo[2], userInfo[3], Integer.parseInt(userInfo[5]), userInfo[0], userInfo[1], userInfo[6]);
						
						buffer=reply1.getBytes();
					}else
					{	
						buffer="error somewhere".getBytes();
					}
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
					aSocket.send(reply);
					
			}
		} catch (SocketException e) {
			System.out.println("SocketException : " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException : " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
		}
	}
}
