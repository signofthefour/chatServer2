package server;
//TODO: Split into two thread to handle in and out simutanously

import database.DatabaseControl;
import database.User;
import protocol.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class ChatClientHandler extends Thread{
	class ChatQueue {
		private volatile Queue<Message> messageQueue = new LinkedList<Message>();
		
		public Message next() {
			return this.messageQueue.poll();
		}
		
		public void add(Message message) {
			this.messageQueue.add(message);
		}
		
		public boolean hasNext() {
			return this.messageQueue.peek() != null;
		}
		
	}
	
	class Output {
		private volatile Message outMessage = new Message();
		
		public Message getOutMessage() {
			return outMessage;
		}
		
		public void setOutMessage(Message msg) {
			this.outMessage = msg;
		}
		
		public void clear() {
			this.outMessage.clear();
		}
	}
	
	final ChatQueue chatQueue = new ChatQueue();
	final Output chatOut = new Output();
	
	private ChatServer 	chatServer;
	private Socket 		clientSocket;
	private String 		clientName;
	private String 		clientPassword;
	OutputStream 		outputStream;
	InputStream 		inputStream;
	BufferedReader 		reader;
	private boolean 	loginStatus = false;
	private boolean		hasException = false;
	
	private Thread chatClientInHandler;
	private Thread chatClientOutHandler;
	
	public boolean isLogin() { return loginStatus; }
	public void readException() { 
		hasException = true;
		handleLogOut();
       	}
	public boolean hasException() { return this.hasException; }
	public String getClientName() { return clientName; }
	public boolean isConnected() { return clientSocket.isConnected(); }
	
	public ChatClientHandler(Socket clientSocket, ChatServer server) {
		this.clientSocket = clientSocket;
		this.chatServer = server;
	}
	
	@Override
	public void run() {
		try {
			handleSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleSocket() throws IOException {
		outputStream = clientSocket.getOutputStream();
		inputStream = clientSocket.getInputStream();
		
		ChatClientInHandler chatClientInput = new ChatClientInHandler(inputStream, this);
		ChatClientOutHandler chatClientOutput = new ChatClientOutHandler(outputStream, this);
		
		chatClientInHandler = new Thread(chatClientInput);
		chatClientOutHandler = new Thread(chatClientOutput);
		
		chatClientInHandler.start();
		chatClientOutHandler.start();
		
		// TODO: synchronize the chat queue
		while (chatClientInHandler.isAlive() && chatClientOutHandler.isAlive()) {
			if (this.chatQueue.hasNext()) {
				handleMessage(this.chatQueue.next());
			}
		}
	}
	
	public void handleMessage(Message msg) {
		// REQUEST: LOGIN
		if (msg.getMethod().equals("REQUEST")) {
			if (msg.getCommand().equals("LOGIN")) {
				handleLogin(msg);
			}
			if (msg.getCommand().equals("LOGOUT")) {
				handleLogOut();
			}
			if (msg.getCommand().equals("CREATE_GR")) {
				handleNewGroup(msg);
			}
		}
		// NOT LOGIN YET
		else if (!isLogin()) {
			try {
				outputStream.write("You have to login first.".getBytes());
				outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		// HAS BEEN LOGIN 
		} else {
			if (msg.getMethod().equals("SEND")) {
				if (msg.getCommand().equals("MSG")) {
					pushMessage(msg);
					return;
				}
				if (msg.getCommand().equals("GROUP")) {
					pushMessage(msg);
					return;
				}
			}
			if (msg.getMethod().equals("SEND")) {
				if (msg.getCommand().equals("FILE")) {
					pushMessage(msg);
					return;
				}
			}
			if (msg.getMethod().equals("RECV") || msg.getMethod().equals("NOTI")) {
				if (msg.getCommand().equals("MSG")) {
					pullMessage(msg);
					return;
				}
				if (msg.getCommand().equals("FILE")) {
					pullMessage(msg);
					return;
				}
				if (msg.getCommand().equals("ONL")) {
					pullMessage(msg);
					return;
				}
				if (msg.getCommand().equals("OFF")) {
					pullMessage(msg);
					return;
				}
				if (msg.getCommand().equals("NEW_GR")) {
					pullMessage(msg);
					return;
				}
				if (msg.getCommand().equals("GROUP")) {
					pullMessage(msg);
					return;
				}
			} else {
				System.out.println("Not support...");
				System.out.println(msg.toText());
				return;
			}
		}
	}

	private void handleNewGroup(Message msg) {
		InputStream is = new ByteArrayInputStream(msg.getBody().getBytes());
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));
		try {
			String name = bf.readLine();
			String mem = null;
			GroupChat newGr = new GroupChat(name);
			while ((mem = bf.readLine()) != null) {
				newGr.addMember(mem);
				addToNewGroupNotify(name, mem);
				System.out.println(mem);
			}
			newGr.addMember(this.clientName);
			this.chatServer.addGroup(newGr);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void handleLogin(Message msg) {
		String content = msg.getBody();
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String gmail = null , password = null;
		try {
			gmail = reader.readLine();
			password = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String onlineClients;
		if (gmail == null || password == null) {
			String lackInfo = "Please check your connection and login again!";
			lackInfo = "NOTI FAIL\nserver NOTFOUND\n\n" + lackInfo;
			try {
				outputStream.write(("<start>\n" + lackInfo + "\n<end>\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		User loginUser = new User("", password, gmail);
		DatabaseControl databaseControl = new DatabaseControl();
		User user = databaseControl.loginValidate(loginUser);
		if (user == null) {
			String notRegister = "Please register!";
			notRegister = "NOTI FAIL\nserver NOTFOUND\n\n" + notRegister;
			try {
				outputStream.write(("<start>\n" + notRegister + "\n<end>\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		this.clientName = user.getName();
		try {
			ArrayList<ChatClientHandler> onlineList  = this.chatServer.getClientList();
			String loginSuccessMessage = "NOTI 200\nserver " + this.getClientName() + "\n";
			if (onlineList.size() == 0) {
				loginSuccessMessage += "\nLogin successfully\nNoone online\n";
			}
			else {
				onlineClients = "";
				for (ChatClientHandler chatClient : onlineList) {
					onlineClients += chatClient.getClientName() + "\n";
				}
				loginSuccessMessage += "\nOnline: \n" + onlineClients;
			}
			outputStream.write(("<start>\n" + loginSuccessMessage + "\n<end>\n").getBytes());
			System.out.println(user.getName() + " login successfully at " + new Date() + "\n");
			this.onlineNotify(this.clientName + " is online.\n");
			this.chatServer.addClient(this);
			this.loginStatus = true;
			this.outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleLogOut() {
		try {
			outputStream.write("Log out successfully.\n".getBytes());
			System.out.println("Disconnect with " + this.clientName + " at "+  clientSocket + "\n");
			
			this.offlineNotify(this.clientName + " is offline.\n");
			this.chatServer.getClientList().remove(this);
			
			outputStream.close();
			inputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onlineNotify(String loginMsg) {
		if (this.chatServer.getClientList().size() == 0) {
			return;
		} else {
			for (ChatClientHandler client : this.chatServer.getClientList()) {
				client.chatQueue.add( new Message (
						"NOTI ONL\n" +
						"server " + client.getClientName() + "\n\n" +
						this.clientName)
				);
			}
		}
	}
	
	public void offlineNotify(String logoutMsg) {
		try 
		{
			if (this.chatServer.getClientList().size() == 0) {
				return;
			} else {
				for (ChatClientHandler client : this.chatServer.getClientList()) {
					if (client != this) {
						client.chatQueue.add( new Message (
							"NOTI OFF\n" +
							"server " + client.getClientName() + "\n\n" +
							this.clientName )
						);
					}
				}
			}
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addToNewGroupNotify(String grName, String memName) {
		try
		{
			if (this.chatServer.getClientList().size() == 0) {
				return;
			} else {
				for (ChatClientHandler client : this.chatServer.getClientList()) {
					if (client.getClientName().equals(memName)) {
						client.chatQueue.add( new Message (
								"NOTI NEW_GR\n" +
										"server " + client.getClientName() + "\n\n" +
										grName)
						);
					}
				}
			}
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void pushMessage(Message msg) {
		this.chatServer.chatQueue.add(msg);
	}
	
	public void pullMessage(Message msg) {
		this.chatOut.setOutMessage(msg);
	}
}
