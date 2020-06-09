package server;

import protocol.Message;

import java.io.*;

public class ChatClientInHandler implements Runnable {
	
	private InputStream inputStream;
	private ChatClientHandler chatClientHandler;
	private BufferedReader reader;
	ByteArrayOutputStream baos;
	InputStream msgStream;
	InputStream fileStream;

	public ChatClientInHandler(InputStream inputStream, ChatClientHandler chatClientHandler) {
		this.inputStream = inputStream;
		this.chatClientHandler = chatClientHandler;
		reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public void run() {
		while (chatClientHandler.isConnected()) {
			if (chatClientHandler.hasException()){
				break;
			}
			Message newMsg = getMessage();
			if (newMsg != null) {
				this.chatClientHandler.chatQueue.add(newMsg);
			} else {
				continue;
			}
		}
	}
	
	public Message getMessage() {
		String line;
		String msg = "";
		Message message = new Message();
		boolean isHeaderEnd = false;
		boolean hasFile = false;
		try {
			if ((line = reader.readLine()) != null) {
				msg = "";
				char[] fileContentChar = null;
				byte[] fileContent = null;
				if (line.equals("<start>")) {
					line = "";
					while (!(line = reader.readLine()).equals("<end>")) {
						if (!isHeaderEnd && line.contains("FILE")) {
							hasFile = true;
						}
						int length;
						if (isHeaderEnd && hasFile) {
							length = Integer.parseInt(line);
							msg += line + "\n"; // length of file
							line = reader.readLine();
							msg += line + "\n"; // Name of file+
							fileContentChar = new char[(int) length];
							System.out.println(line);
							reader.read(fileContentChar, 0, length);
							fileContent =  (new String(fileContentChar)).getBytes();

//							System.out.println(fileContentChar);
							int count = 0;
							try (FileOutputStream fout = new FileOutputStream("/home/nguyendat/Desktop/hello.png")) {
								fout.write(fileContent, 0, fileContent.length);
							}
						}
						if (line.equals("")) {
							isHeaderEnd = true;
						}
						if (fileContent == null) msg += line + "\n";
					}
					if (fileContent != null) message.createNew(msg, fileContent);
					else {
						message.createNew(msg);
//						System.out.println(msg);
					}
				}
			}
			if (message.good()) {
				return message;
			} else {
				this.chatClientHandler.readException();
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.chatClientHandler.readException();
		}
		return null;
	}
}
