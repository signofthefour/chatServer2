package server;

import protocol.Message;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

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
							fileContentChar = new char[length];
							reader.read(fileContentChar, 0, length);
							CharBuffer charBuffer = CharBuffer.wrap(fileContentChar);
							ByteBuffer byteBuffer = Charset.forName("ASCII").encode(charBuffer);
							fileContent = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
							Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
//							System.out.println(fileContent);
							System.out.println(fileContentChar);
							try (FileOutputStream fos = new FileOutputStream("/home/nguyendat/Desktop/hello.png")) {
								fos.write(fileContent);
								// There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
							}
						}
						if (line.equals("")) {
							isHeaderEnd = true;
						}
						if (fileContent == null) msg += line + "\n";
					}
					if (fileContent != null) message.createNew(msg, fileContent);
					else message.createNew(msg);
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
