package server;

import java.io.IOException;
import java.io.OutputStream;

import protocol.Message;

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
					outputStream.write(client.chatOut.getOutMessage().toText().getBytes());
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
