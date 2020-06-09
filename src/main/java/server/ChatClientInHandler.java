package server;

import protocol.Message;

import java.io.*;

public class ChatClientInHandler implements Runnable {
	
	private InputStream inputStream;
	private ChatClientHandler chatClientHandler;
	private BufferedReader reader;
	private byte[] data;
	ByteArrayOutputStream baos;
	InputStream msgStream;
	InputStream fileStream;

	public ChatClientInHandler(InputStream inputStream, ChatClientHandler chatClientHandler) {
		this.inputStream = inputStream;
		this.chatClientHandler = chatClientHandler;
//		reader = new bufferedreader(new inputstreamreader(inputstream)));
	}

	@Override
	public void run() {
		while (chatClientHandler.isConnected()) {
			if (chatClientHandler.hasException()){
				break;
			}
			data = null;
			try {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int count = 0;
				int sumRead = 0;
				byte[] data = new byte[1048576];
				int tmp = 0;
				while (inputStream.available() > 0) {
				    tmp = inputStream.read();
				    data[count] = (byte)tmp;
				    count+=1;
				}
				if (count == 0) continue;
				buffer.write(data, 0, count);
				byte[] res = buffer.toByteArray();
				Message newMsg = getMessage(res);
				if (newMsg != null) {
					this.chatClientHandler.chatQueue.add(newMsg);
				} else {
					continue;
				}
				data = null;
				buffer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}



	public Message getMessage(byte[] data) {
	    byte[] cloneData = new byte[data.length];
		cloneData = data.clone();
		InputStream is = new ByteArrayInputStream(data);
		reader = new BufferedReader(new InputStreamReader(is));
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

							fileContent = new byte[(int) length];
							int startPos = data.length - length - 8;
							System.out.println(data.length);
							for (int i = 0; i < length; i++) {
								fileContent[i] = cloneData[startPos + i];
							}
							System.out.println(fileContent[0]);
							try (FileOutputStream fout = new FileOutputStream("/home/nguyendat/Desktop/hello.png")) {
								fout.write(fileContent, 0, fileContent.length);
							}
							break;
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
