package server;

import protocol.Message;

import java.io.IOException;
import java.io.OutputStream;

public class ChatClientOutHandler implements Runnable {
	
	private OutputStream outputStream;
	private ChatClientHandler client;
	private Message message;

	public ChatClientOutHandler(OutputStream outputStream, ChatClientHandler client) {
		this.outputStream  = outputStream;
		this.client = client;
		this.message = this.client.chatOut.getOutMessage();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (client.hasException()) break;
			if (client.chatOut.getOutMessage().good()) {
				try {
					if (client.chatOut.getOutMessage().getCommand().equals("FILE")) {
						outputStream.write(client.chatOut.getOutMessage().toByte());
						System.out.println("File");
					}
					else {
						outputStream.write(client.chatOut.getOutMessage().toText().getBytes());
					}
					this.client.chatOut.clear();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					client.readException();
					break;
				}
			} else {
				continue;
			}
		}
	}
}
