package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String URL = "jdbc:h2:./data/bank";

    private DatabaseConnection() {

    }

    @FunctionalInterface
    public interface SqlWork{
        void run(Connection connection) throws SQLException;
    }

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, "sa", "");
    }

    public static void initialise(){
        try{
            String script = Files.readString(Path.of("schema.sql"));
            try(Connection c = getConnection(); Statement st = c.createStatement()){
                for (String sql : script.split(";")){
                    if(!sql.isBlank()){
                        st.execute(sql);
                    }
                }
            }
        } catch (IOException | SQLException e){
            throw new DataAccessException("Database initialisation failed. " + e.getMessage(), e);
        }
    }

    public static void transaction(SqlWork work){
        try (Connection c = getConnection()){
            c.setAutoCommit(false);
            try{
                work.run(c);
                c.commit();
            }catch (SQLException | RuntimeException e){
                c.rollback();
                throw e;
            }
        } catch (SQLException e){
            throw new DataAccessException("Database operation failed: " + e.getMessage(), e);
        }
    }
}
