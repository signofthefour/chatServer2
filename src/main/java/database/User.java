package database;

public class User {
	private String name;
	private String password;
	private String email;

	private void setName(String _name) { name = _name;}
	private void setPassword(String _password) { password = _password;}
	private void setEmail(String _email) { email = _email;}

	public String getName() {return name;}
	public String getPassword() {return password;}
	public String getEmail() {return email;}

	public boolean verifyAccount (String _name, String _password) {
		return _name == name && _password == password;
	}

	public User(String _name, String _password, String _email) {
		setName(_name);
		setPassword(_password);
		setEmail(_email);
	}
}
