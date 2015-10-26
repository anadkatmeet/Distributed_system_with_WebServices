package com.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sun.swing.internal.plaf.synth.resources.synth;

public class GameServerAS extends Server{
	final int portNA=1411;
	final int portEU=1412;
	final int portAS=1413;
	
	public final String logfilenameclient="Assignment3_data/log/client/";
	public final String logfilenameadmin="Assignment3_data/log/admin/admin.log";
	public final String logfilenameserver="Assignment3_data/log/server/"; 
	public final String filenameserver="Assignment3_data/serverfiles/serverAS.txt";
	//userinfo 0-unm,1-pwd,2-fnm,3-lnm,4-status,5-age
	
	public BufferedReader initializeBR() throws FileNotFoundException{
		
		try {
			return new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(filenameserver))));
		}catch(FileNotFoundException e){
			File file = new File(filenameserver);
			if (!file.exists()){
				try{
				file.createNewFile();}catch(Exception ioe){ioe.printStackTrace();}
			}
			return new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(filenameserver))));
		}
		catch (Exception e) {
			
			System.out.println("errorrrr");
			e.printStackTrace();
		}
		return null;
	}
	public String calculateStatus() throws IOException {
		int online = 0, offline = 0;
			BufferedReader br=initializeBR();
			
			String temp;
			
			//while loop to iterate through the file and find whether the username exists or not
			while ((temp=br.readLine())!=null) {
				if (!temp.equalsIgnoreCase("")){
					String[] t1=temp.split("-");
					if (t1[4].equalsIgnoreCase("true")) {
						online++;
					}else
					{	offline++;
					}				
				}
				
			}
			br.close();
			
		return online+" online & "+offline+" offline";
	}
	
	public String createPlayerAccount(String firstName, String lastName, int age,
			String userName, String password, String ipAdd) throws IOException {
		
		if (!(userName.length() >= 6 && userName.length() <= 15)) {
			return "Username should be between 6 to 15 characters";
		}
		if (!(password.length() >= 6)) {
			return "Password should be at least 6 characters long";
		}
		String filename1;
		//server's log
		filename1 = logfilenameserver+findRegion(ipAdd) + "-server.log";
		doLog(filename1,"Player Creation Request with username: "+userName);
		
		boolean isCorrect = true;
		
		BufferedReader br=initializeBR();
		String temp;
		
		//while loop to iterate through the file and find whether the username exists or not
		while ((temp=br.readLine())!=null) {
			String[] t1=temp.split("-");
			if (userName.equals(t1[0])) {
				isCorrect=false;
				return "username already exists!";
			}
			
		}
		br.close();
		
		String filename;
		//if the isCorrect flag is set,then insert into file
		if (isCorrect) {
			FileWriter fw = null;
			try {
				String newUserInfo=userName+"-"+password+"-"+firstName+"-"+lastName+"-"+"false"+"-"+age;
				
				fw = new FileWriter(filenameserver,true);
				PrintWriter print=new PrintWriter(fw);
				print.println(newUserInfo);
				print.close();
				fw.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//player's log
			filename = logfilenameclient+userName + findRegion(ipAdd) + ".log";
			doLog(filename,"Account Created ");

			//server's log
			filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
			doLog(filename,"Status: Account Created with username: "+userName);
			
			return "successful";
			
		}else{
			//server's log
			filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
			doLog(filename,"Status: player creation failed");
			return "creation failed";
		}
		
		
	}
	
	public String processSignIn(String userName, String password, String ipAdd) throws IOException {
		
		//server's log
		String filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
		doLog(filename,"SignIn request from username:"+userName);
		Boolean flag=true;
		
		String temp;
		BufferedReader br=initializeBR();
		int length = 0;
		while ((temp=br.readLine())!=null) {
			length++;
		}
		String[] append = new String[length];
		length=0;
		br=initializeBR();
		
		int isDone=0,isNotDone=0;

		//while loop to iterate through the file and find whether the username exists or not
				while ((temp=br.readLine())!=null) {
					String[] t1=temp.split("-");
					if (userName.equals(t1[0])&& password.equals(t1[1])) {
						//flag=false;//unm & pwd matches
						
							if (t1[4].equals("false")) {
								//sign in
								temp=t1[0]+"-"+t1[1]+"-"+t1[2]+"-"+t1[3]+"-"+"true"+"-"+t1[5];
								isDone=1;
							}
							else{//already signed in 
								isNotDone=1;
							}
					}
					
				append[length++]=temp;
				}
				FileWriter fw=new FileWriter(filenameserver,false);
				PrintWriter print=new PrintWriter(fw);
				for (int i = 0; i < append.length; i++) {
					print.println(append[i]);
				}
				print.close();
				fw.close();
				br.close();
				
				fw.close();
				
				//setting logs
				if (isDone == 1) {
					
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:Successfully signed in");
					//player's log
					filename = logfilenameclient+userName + findRegion(ipAdd) + ".log";
					doLog(filename,"Successfully signed in");
					return "Signed In successfully";
				
				} else if (isNotDone == 1) {
					
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:already signed in");
					//player's log
					filename = logfilenameclient+userName + findRegion(ipAdd) + ".log";
					doLog(filename,"already signed in");
					return "Already Signed In";
				
				}
				else{
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:User is invalid");
					return "Username or password is invalid";
				}
		
	}
	
	public String processSignOut(String userName, String ipAdd) throws IOException {
		int result=0;
		//server's log
		String filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
		doLog(filename,"Signout request from username:"+userName);
		
		Boolean flag=true;
		
		//String location="NA";
		String temp;
		BufferedReader br=initializeBR();
		int length = 0;
		while ((temp=br.readLine())!=null) {
			length++;
		}
		String[] append = new String[length];
		length=0;
		br=initializeBR();
		
		int isDone=0,isNotDone=0;
		//while loop to iterate through the file and find whether the username exists or not
				while ((temp=br.readLine())!=null) {
					String[] t1=temp.split("-");
					if (userName.equals(t1[0])) {
						//unm matches,check for status
						if (t1[4].equals("true")) {//sign out
								temp=t1[0]+"-"+t1[1]+"-"+t1[2]+"-"+t1[3]+"-"+"false"+"-"+t1[5];
								isDone=1;
							}else{//not signed in
								isNotDone=1;
							}
						
					}append[length++]=temp;
				}
				FileWriter fw=new FileWriter(filenameserver,false);//false means it will overwrite the file
				PrintWriter print=new PrintWriter(fw);
				for (int i = 0; i < append.length; i++) {
					print.println(append[i]);
				}
				print.close();
				fw.close();
				br.close();
				
				fw.close();
				
				//setting logs
				if (isDone == 1) {
					
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:Signed out successfully");
					//player's log
					filename = logfilenameclient+userName + findRegion(ipAdd) + ".log";
					doLog(filename,"Successfully signed out");
					return "Signed Out successfully";
					
				} else if (isNotDone==1) {
					
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:already signed out");
					//player's log
					filename = logfilenameclient+userName + findRegion(ipAdd) + ".log";
					doLog(filename,"already signed out");
					return "Already Signed Out!";
				}
				else{
					//server's log
					filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
					doLog(filename,"Staus:user is invalid");
					return "User is invalid";
				}
	}
		
	public String getPlayerStatus(String adminunm,String adminpwd,String ipAdd) throws IOException {
		
		if (!(adminunm.equals("admin")&&adminpwd.equals("admin"))) {
			return "wrong admin username or password";
		}
		String filename;
		
		//server's log
		filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
		doLog(filename,"Player status request from:"+findRegion(ipAdd)+"region");
		
		int port1=0,port2=0;
		String localSatus=calculateStatus();
		String replyfromUDP1="",replyfromUDP2="";
		int serverPort=0;
		
		//it will check the region & it will set the other two region's port nos into port1 & port2 variables
		
		//call na,as
		port1=portNA;port2=portEU;
		localSatus="AS: "+localSatus;
		replyfromUDP1="NA: ";
		replyfromUDP2="EU: ";
		
		DatagramSocket aSocket = null;
		
		//running loop twice for other two servers, i.e. calling other two servers via UDP
		for (int i = 0; i < 2; i++) {
			
			if (i==0) {
				serverPort = port1;
			}
			else if(i==1)
				serverPort = port2;
			
			String replytemp=callUDP(aSocket,serverPort,"call");
			replytemp.trim();
			if (i==0) {
				replyfromUDP1=replyfromUDP1 + replytemp;
			}else if (i==1) {
				replyfromUDP2=replyfromUDP2 + replytemp;
			}
		}
		String temp=localSatus+" "+replyfromUDP1+" "+replyfromUDP2;
		
		
		//server's log
				filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
				doLog(filename,"From : " + findRegion(ipAdd) + " region, result:"
						+ temp);
				
		//admin's log
				filename = logfilenameadmin;
				doLog(filename,"From : " + findRegion(ipAdd) + " region, result:"
						+ temp);
		return temp;
		
	}
	private String callUDP(DatagramSocket aSocket,int serverPort,String msg){
		try {
			aSocket = new DatagramSocket();
			byte[] m = msg.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			
			DatagramPacket request = new DatagramPacket(m,
					m.length, aHost, serverPort);
			System.out.println("in callUDP...data...\n" + msg);
			aSocket.send(request);

			byte[] buffer = new byte[100000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			return new String(reply.getData(), 0, reply.getLength());
			//return new String(reply.getData());
			
		} catch (SocketException e) {
			
			System.out.println("Error in SocketException : " + e.getMessage());
		} catch (IOException e) {
			
			System.out.println("Error in IOException : " + e.getMessage());
		} catch (Exception e) {
			
			System.out.println("Error in Exception : " + e.getMessage());
		} finally {

			if (aSocket != null)
				aSocket.close();

			
		}
		return "";
	}
	
	public String suspendAccount(String adminUnm, String adminPwd, String ipAdd,
			String username) throws IOException {
		
		if (!(adminUnm.equals("admin")&&adminPwd.equals("admin"))) {
			return "wrong admin username or password";
		}
		
		String filename;
		//server's log
		filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
		doLog(filename,"Suspend account request from:"+findRegion(ipAdd)+" region to remove "+username);
		
		Boolean flag=true;
		
		//String location="NA";
		String temp;
		BufferedReader br=initializeBR();
		int length = 0;
		while ((temp=br.readLine())!=null) {
			String[] t1=temp.split("-");
			if (username.equals(t1[0])) {
				
			}else 
				length++;
		}
		String[] append = new String[length];
		length=0;
		br=initializeBR();
		
		//while loop to iterate through the file and find whether the username exists or not
		while ((temp=br.readLine())!=null) {
			String[] t1=temp.split("-");
			if (username.equals(t1[0])) {
				//unm matches
				//dont add it to append array
				flag=false;
			}else
				append[length++]=temp;
		}
		
		FileWriter fw=new FileWriter(filenameserver,false);
		PrintWriter print=new PrintWriter(fw);
		for (int i = 0; i < append.length; i++) {
			print.println(append[i]);
		}
		print.close();
		fw.close();
		br.close();
		if (flag==false) {
			//server's log
			filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
			doLog(filename,"Status: Removed successully");
			
			//player's log
			filename = logfilenameclient+username + findRegion(ipAdd) + ".log";
			doLog(filename,"account suspended");
							
			//admin's log
			filename = logfilenameadmin;
			doLog(filename,"account suspended for "+username+ " from "+findRegion(ipAdd)+" region");
			return "successful deletion";
		}else
			//server's log
			filename = logfilenameserver+findRegion(ipAdd) + "-server.log";
			doLog(filename,"Status: wrong username");
			return "wrong username";
		
	}
	//used for logging
	private synchronized void doLog(String filename, String string) {
		Logger logger = Logger.getLogger("", null);
		logger.setUseParentHandlers(false); // wont print to console
		FileHandler fileHandler = null;
	
		try {
			
			File file = new File(filename);
			if (file.exists()) {
				// if file already exists,it will append the data
				fileHandler = new FileHandler(filename, true);
			} else {
				// will create new file
				fileHandler = new FileHandler(filename);
			}

			logger.addHandler(fileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			logger.info(string);
		} catch (SecurityException e) {
			logger.info("SecurityException : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			fileHandler.close();
		}

	}
		
	//finds region when ipadd is passed
	private String findRegion(String ip) {
			String pre = ip.substring(0, ip.indexOf("."));
		  if (pre.equals("132")){
		    return "NA";
		  }else if (pre.equals("93")){
			return "EU";
		  }else if (pre.equals("182")){
		    return "AS";
		  }
		return "";
		}
	
	public String transferAccount(String userName, String password,
			String oldipApp, String newipAdd) throws IOException {
		int result=0;
		int serverPort=0;
		String filename;
		//server's log
		filename = logfilenameserver+findRegion(oldipApp) + "-server.log";
		doLog(filename,"Transfer request from "+userName);
		
		String oldRegion=findRegion(oldipApp);
		String newRegion=findRegion(newipAdd);
		
		if (newRegion.equalsIgnoreCase("NA")) {
			serverPort = portNA;
		}
		else if(newRegion.equalsIgnoreCase("EU"))
		{	
			serverPort = portEU;
		}
		else if(newRegion.equalsIgnoreCase("AS"))
		{	
			serverPort = portAS;
		}
		
		BufferedReader br=initializeBR();
		String temp;
		
		//while loop to iterate through the file and find whether the username exists or not
		while ((temp=br.readLine())!=null) {
			//System.out.println(temp);
			if (temp.equals("")) {
				
			}else{
				String[] t1=temp.split("-");
				if (userName.equals(t1[0])) {
					//unm found so send udp to insert
					DatagramSocket aSocket = null;
					String userInfo=temp+"-"+newipAdd;
					//it will call udp(of the region to which transfer is needed) by passing all the user info
					System.out.println("USER INTO....\n" + userInfo);
					String replytemp=callUDP(aSocket, serverPort,userInfo);
					//if added successful,then suspend the local one
					if (replytemp.contains("successful")) {
						
						
						//server's log
						filename = logfilenameserver+findRegion(oldipApp) + "-server.log";
						doLog(filename,"Status: transfer successful to "+findRegion(newipAdd)+" region");
						
						//client's log
						filename = logfilenameclient+userName + findRegion(oldipApp) + ".log";
						doLog(filename,"Account transfered to "+findRegion(newipAdd)+" region");
						//return "transfer done";
						result=1;
					}else
						result=4;//cant get created in new server
				}
			}
			
		}
		br.close();
		
		
		if (result==0) {
			//unm didnt found in local server so no transfer possible
			//server's log
			filename = logfilenameserver+findRegion(oldipApp) + "-server.log";
			doLog(filename,"Status: username not found in this server");
			return "unm not found in this server";
		}
		else if (result==1) {
			String r=suspendAccount("admin", "admin", oldipApp, userName);
			return "transfered!!";
		}else if (result==4) {
			//server's log
			filename = logfilenameserver+findRegion(oldipApp) + "-server.log";
			doLog(filename,"Status: transfer couln't be completed");
			return "unm found in new server so cant transfer";
		}
		return "";
	}//transfer method ends
}
