package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import protocol.Message;


public class ChatServer {
	class ChatQueue {
		private volatile Queue<Message> chatQueue = new LinkedList<Message>();
		
		public void add (Message msg) {
			chatQueue.add(msg);
		}
		
		public Message getNext () {
			return chatQueue.poll();
		}
		
		public boolean hasNext() {
			return chatQueue.peek() != null;
		}
	}
	
	final ChatQueue chatQueue = new ChatQueue();
	private ServerSocket serverSocket;
	private ArrayList<ChatClientHandler> clientList = new ArrayList<ChatClientHandler>(0);
	// Add new array to handle Registed client
	
	public ChatServer(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		System.out.println("About to accept client...");
	}
	
	public void start() throws IOException {
		Thread directMessage = new Thread(new DirectMessage(this));
		directMessage.start();
		
		while (true) {
			Socket clientSocket = this.serverSocket.accept();
			System.out.println("New request...");
			ChatClientHandler chatClientHandler = new ChatClientHandler(clientSocket, this);
			chatClientHandler.start();
		}
	}
	
	public ArrayList<ChatClientHandler> getClientList() {
		return clientList;
	}
	
	public void addClient(ChatClientHandler newClient) {
		this.clientList.add(newClient);
	}
}
