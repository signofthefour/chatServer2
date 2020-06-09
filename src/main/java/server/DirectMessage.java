package server;

import protocol.Message;

import java.util.ArrayList;

public class DirectMessage implements Runnable {
	private ChatServer chatServer;
	private Message		message;

	public DirectMessage(ChatServer chatServer) {
		this.chatServer = chatServer;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (chatServer.chatQueue.hasNext()) {
				message = chatServer.chatQueue.getNext();
				direct(message);
			}
		}
	}
	
	public void direct(Message msg) {
		if (msg.getCommand().equals("GROUP")) {
			String grName = msg.getReceiver();
			msg.setReceiveMethod();
			boolean rightAccess = false;
			GroupChat gr = findGroup(grName, this.chatServer.getGroupList());
			if (gr != null) {
				ArrayList<String> members = gr.getMemberList();
				for (String member : members) {
					if (member.equals(msg.getSender())) {
						rightAccess = true;
					}
				}
				if (rightAccess) {
					for (String member : members) {
						if (!member.equals(msg.getSender())) {
							ChatClientHandler receiver = findClient(member, this.chatServer.getClientList());
							if (receiver != null) {
								System.out.println(receiver.getClientName() + " " + msg.getBody());
								Message grMsg = new Message(msg);
								receiver.chatQueue.add(grMsg);
							}
						}
					}
				} else {
					return;
				}
			}
			return;
		}
		else {
			String receiverName = msg.getReceiver();
			msg.setReceiveMethod();
			ChatClientHandler receiver = findClient(receiverName, this.chatServer.getClientList());
			if (receiver != null) {
				receiver.chatQueue.add(msg);
			}
		}
	}
	
	public ChatClientHandler findClient(String name, ArrayList<ChatClientHandler> clients) {
	    for (ChatClientHandler client : clients) {
	    	if (client.getClientName().equals(name)) {
	    		return client;
	    	}
	    }
	    return null;
	}

	public GroupChat findGroup(String name, ArrayList<GroupChat> groupList) {
		for (GroupChat gr : groupList) {
			if (gr.getName().equals(name)) {
				return gr;
			}
		}
		return null;
	}
}
