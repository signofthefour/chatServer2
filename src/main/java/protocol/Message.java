package protocol;

//<start>
//<METHOD> <COMMAND>
//<sender> <receiver>
//<blank line>
//(body)...
//<end>

// METHOD:
//		REQUEST: 
//			CMD: LOGIN, LOGOUT
//		SEND: 
//			 CMD: MSG, GROUP
//		RECV:
//			CMD: MSG, FILE

public class Message {
	
	private String msg;
	private String method;
	private String cmd;
	private String sender;
	private String receiver;
	private String body = "";
	
	public Message() {
		msg = "";
		method = "";
		cmd = "";
		sender = "";
		receiver = "";
		body = "";
	}
	public Message(String s) {
		this.msg = s;
		init();
	}
	
	public void createNew(String msg) {
		this.msg = msg;
		init();
	}
	
	public void init() {
		String[] lines = msg.split("\n");
		if (lines.length < 3) return;
		// Get method and command
		this.method = (lines[0].split(" ")[0]).trim();
		this.cmd 	= (lines[0].split(" ")[1]).trim();
		this.sender = (lines[1].split(" ")[0]).trim();
		this.receiver=(lines[1].split(" ")[1]).trim();
		body = "";
		for (int i = 3; i < lines.length; i++) {
			body += lines[i] + '\n';
		}
	}
	
	public String getMethod() { return this.method; }
	
	public String getCommand() { return this.cmd; }
	
	public String getSender() {return this.sender; }
	
	public String getReceiver() {return this.receiver; }
	
	public String getBody() { return this.body; }
	
	public void setReceiveMethod() {
		this.method = "RECV";
	}
	
	public boolean good() {
		return method != "" && cmd != "" && sender != "" && receiver != ""; 
	}
	
	public void clear() {
		msg = "";
		method = "";
		cmd = "";
		sender = "";
		receiver = "";
		body = "";
	}
	
	public String toText() {
		String text = "";
		text += "<start>\n";
		text += method + " " + cmd + "\n";
		text +=  sender + " " + receiver + "\n";
		text += "\n";
		text += body + "\n";
		text += "<end>\n";
		return text;
	}
}
