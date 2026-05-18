package com.ydjs.DAO;

import com.ydjs.Model.Account;
import com.ydjs.Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object (DAO) class for the Account entity.
 * This class encapsulates all database operations related to the Account table.
 * 
 * Architectural Pattern:
 * - Employs the Data Access Object (DAO) pattern to separate persistence logic from business logic.
 */
public class AccountDAO {

    /**
     * Persists a new Account into the database.
     * 
     * Implements specification:
     * - System Doc Section 2.1: User Registration.
     * 
     * @param account The account object containing the username and password to be saved.
     * @return The newly created Account object, complete with the auto-generated account_id, or null if insertion fails.
     */
    public Account createAccount(Account account) {
        Connection connection = ConnectionUtil.getConnection();
        // TODOs: Using a standard try-catch instead of try-with-resources
        // as instructed in todo.md to prevent closing the singleton ConnectionUtil connection.
        try {
            String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
            // Design Pattern: Utilizing PreparedStatement to prevent SQL injection.
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());
            
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
                if (pkeyResultSet.next()) {
                    int generated_account_id = (int) pkeyResultSet.getLong(1);
                    return new Account(generated_account_id, account.getUsername(), account.getPassword());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves an account from the database based on the provided username.
     * 
     * Implements specification:
     * - System Doc Section 2.1 (Helper): Ensures a duplicate username is detected during registration.
     * 
     * @param username The username of the account to retrieve.
     * @return An Account object if a matching record is found, or null if it does not exist.
     */
    public Account getAccountByUsername(String username) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Account account = new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves an account from the database based on the provided account ID.
     * 
     * Implements specification:
     * - System Doc Section 3.1 (Helper): Used to verify if a posted_by user exists when creating a message.
     * 
     * @param accountId The ID of the account to retrieve.
     * @return An Account object if a matching record is found, or null if it does not exist.
     */
    public Account getAccountById(int accountId) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE account_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, accountId);
            
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves an account matching a specific username and password combination.
     * 
     * Implements specification:
     * - System Doc Section 2.2: User Login.
     * 
     * @param username The username of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return The authenticated Account object including the account_id if successful, or null if credentials do not match.
     */
    public Account getAccountByUsernameAndPassword(String username, String password) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
