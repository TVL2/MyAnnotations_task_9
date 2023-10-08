package query_constructor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class QueryConstructor {

    private static Connection connection;
    private static Statement statement;



    public static <T> void createTable(T table) {
        try {
            connection();
            statement.executeUpdate(prepareTable(table));
        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnection();
        }
    }

    public static <T> void createTableAndAddToTable(T table) {
        try {
            connection();
            statement.executeUpdate(prepareTable(table));
            statement.executeUpdate(prepareAddToTable(table));
        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnection();
        }
    }
    public static <T> void addToTable(T table) {
        try {
            connection();
            statement.executeUpdate(prepareAddToTable((table)));
        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnection();
        }
    }


    private static void connection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:test");
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void disconnection() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static <T> String prepareTable(T table) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        if (!table.getClass().isAnnotationPresent(Table.class)) {
            throw new NoSuchElementException("Имя таблицы не задано.");
        }
        builder.append(table.getClass().getAnnotation(Table.class).title());

        builder.append(" (");

        Field[] fields = Arrays.stream(table.getClass().getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(Column.class))
                .toArray(Field[]::new);

        for (int i = 0; i < fields.length; i++) {
            builder.append(fields[i].getName());
            builder.append(" ");
            if (fields[i].getType().toString().contains("String"))
                builder.append("VARCHAR");
            if (fields[i].getType().toString().contains("Integer"))
                builder.append("INTEGER");
            if (i < fields.length - 1)
                builder.append(", ");
        }
        builder.append(");");
        System.out.println(builder.toString());
        return builder.toString();
    }

    public static <T> String prepareAddToTable(T table) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        if (!table.getClass().isAnnotationPresent(Table.class)) {
            throw new NoSuchElementException("Имя таблицы не задано.");
        }
        builder.append(table.getClass().getAnnotation(Table.class).title());

        builder.append(" (");

        Field[] fields = Arrays.stream(table.getClass().getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(Column.class))
                .toArray(Field[]::new);

        for (int i = 0; i < fields.length; i++) {
            builder.append(fields[i].getName());
            if (i < fields.length - 1)
                builder.append(", ");
        }
        builder.append(") VALUES (");

        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].getType().toString().contains("String")) {
                    builder.append("'");
                    builder.append(fields[i].get(table));
                    builder.append("'");
                } else {
                    builder.append(fields[i].get(table));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (i < fields.length - 1)
                builder.append(", ");
        }
        builder.append(");");
        System.out.println(builder.toString());
        return builder.toString();
    }
}
