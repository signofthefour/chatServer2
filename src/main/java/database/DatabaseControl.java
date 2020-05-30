package database;

import java.sql.*;

public class DatabaseControl {

	MysqlUser mysqlUser = new MysqlUser();
	AccountManager accountManager = new AccountManager();

	public static void checkDatabase() throws SQLException, ClassNotFoundException {
		MysqlUser mysqlUser = new MysqlUser();
		String jdbcURL = mysqlUser.getJdbcURL();
		String dbUser =  mysqlUser.getName();
		String dbPassword = mysqlUser.getPassword();
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
			connection.close();
	}

	public Connection connect() throws ClassNotFoundException, SQLException {
		String jdbcURL = mysqlUser.getJdbcURL();
		String dbUser =  mysqlUser.getName();
		String dbPassword = mysqlUser.getPassword();

		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
		return connection;
	}

	public boolean addNewAccount(User newUser) {
		try (Connection conn = connect()) {
			return accountManager.insertUser(newUser, conn);
		} catch (SQLException | ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}

	public void test() throws SQLException, ClassNotFoundException {
		System.out.println("ALo");
		User newUser = new User("tandat", "password", "nguyentandatcs@gmail.com");
		if ( addNewAccount(newUser)) {
			System.out.println("ADDed");
		}
	}
}
