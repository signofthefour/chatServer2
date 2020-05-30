package database;

import java.sql.*;

public class AccountManager {
    public boolean insertUser(User newUser, Connection conn) {
        String userGmail = newUser.getEmail();
        String SQL = "INSERT INTO account (name, password, gmail)\n" +
                "SELECT * FROM (SELECT ?, ?, ?) AS tmp\n" +
                "WHERE NOT EXISTS (\n" +
                "SELECT gmail FROM account WHERE gmail=\'" +userGmail+"\'\n" +
                ") LIMIT 1";
        long id = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, newUser.getName());
            pstmt.setString(2, newUser.getPassword());
            pstmt.setString(3, newUser.getEmail());

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    return false;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }
}
