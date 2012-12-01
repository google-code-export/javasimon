package org.javasimon.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample object mapped to a database table
 */
public class Sample {
    private int id;
    private String name;

    public Sample() {
    }

    public Sample(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void createTable(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("create table sample(id int primary key, name varchar(256))");
        } finally {
            H2DbUtil.close(statement);
        }
    }

    public static void dropTable(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("drop table sample");
        } finally {
            H2DbUtil.close(statement);
        }
    }

    public void insert(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("insert into sample(id,name) values (?,?)");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
        } finally {
            H2DbUtil.close(preparedStatement);
        }
    }

    public Sample load(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt(1);
        name = resultSet.getString(2);
        return this;
    }

    public static Sample loadById(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Sample sample = null;
        try {
            preparedStatement = connection.prepareStatement("select id,name from sample where id=?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                sample = new Sample().load(resultSet);
            }
            return sample;
        } finally {
            H2DbUtil.close(preparedStatement,resultSet);
        }
    }

    public static List<Sample> loadAll(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select id,name from sample order by id asc");
            resultSet = preparedStatement.executeQuery();
            return loadAll(resultSet);
        } finally {
            H2DbUtil.close(preparedStatement, resultSet);
        }
    }

    private static List<Sample> loadAll(ResultSet resultSet) throws SQLException {
        List<Sample> samples = new ArrayList<Sample>();
        while (resultSet.next()) {
            samples.add(new Sample().load(resultSet));
        }
        return samples;
    }
}
