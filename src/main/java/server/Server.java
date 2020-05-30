package server;

import database.DatabaseControl;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class Server {
	public static void main (String[] args) throws IOException {
		// Create server socket
		DatabaseControl databaseControl = new DatabaseControl();
		try {
			databaseControl.test();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		int port = 8818;
		try {
			ServerSocket chatServerSocket = new ServerSocket(port);
			
			ChatServer chatServer = new ChatServer(chatServerSocket);
			chatServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
