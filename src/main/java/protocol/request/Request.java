package protocol.request;

public interface Request {
	public String getMethod();
	public String getCommand();
	public String getSender();
	public String getReceiver();
	public String getBody();
	
	public String setMethod();
	public String setCommand();
	public String setSender();
	public String setReceiver();
	public String setBody();
}
