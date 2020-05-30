package database;

public class MysqlUser {
    private String name = "root";
    private String password = "root";
    private String jdbcURL = "jdbc:mysql://localhost:3306/chatServer";

    String getName() {return name;}
    String getPassword() {return password;}
    String getJdbcURL() {return jdbcURL;}
}
