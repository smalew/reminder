package db_logic;

import gui.Constants;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Created by Koropenkods on 29.02.16.
 * Класс для работы с БД.
 * Используется SQLite
 */
public class DataBaseClass {

    private static DataBaseClass instance = null;

    private Connection connection = null;
    private Statement statement = null;

    public static String currentUser;
    public static String currentDB;
    private String currentDBURL = Constants.URL;

    /**Приватный конструктор.
     * Инициализирует драйвер для работы с DB.
     * Пробует подключиться к этой базе.
     * Если база пустая, то создаеются новые таблицы.
     */
    private DataBaseClass() throws SQLException{
        initDriver();
        connect();
        createTables();
        close();
    }

    //Singleton
    public static DataBaseClass getInstance() throws SQLException{
        if (instance == null){
            return new DataBaseClass();
        }
        return instance;
    }

    private void initDriver(){
        try {
            Class.forName(Constants.DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void setDBURL(String url){
        System.out.println("URL: "+ url);
        currentDBURL = url;
    }
    public void connect() throws SQLException{
        int lastSlash = currentDBURL.lastIndexOf(File.pathSeparator);
        if (lastSlash != -1)
            currentDB = currentDBURL.substring(lastSlash+1, currentDBURL.length());
        else
            currentDB = currentDBURL.substring(12, currentDBURL.length());
        connection = DriverManager.getConnection(currentDBURL);
        statement = connection.createStatement();
    }
    private void createTables() throws SQLException {
        if(connection != null) {
            statement.execute("CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY AUTOINCREMENT, name char(20), " +
                    "passwd CHAR(30));");

            statement.execute("CREATE TABLE IF NOT EXISTS Master (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                "name char(30), " +
                                                                "count INTEGER, " +
                                                                "author CHAR(30));");

            statement.execute("CREATE TABLE IF NOT EXISTS Tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title CHARACTER(50), body CHARACTER(500), " +
                    "createTime INTEGER, modifyTime INTEGER, finishTime INTEGER, " +
                    "status INTEGER, author CHAR(30) NOT NULL, master CHARACTER(30));");

            if (this.getSize("Users", null, null) == 0){
                statement.execute("INSERT INTO Users VALUES " +
                        "(1, \""+ Constants.DEFAULT_USER +"\", \""+ Constants.DEFAULT_PASSWORD +"\");");
            }
        }
    }
    public void close(){
        try {
            if (!this.databaseIsClosed()){
                statement.close();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean databaseIsClosed() throws SQLException {
        return (statement.isClosed() || connection.isClosed());
    }

    /**
     * Медот добавления данных в БД.
     * @param tableName - имя таблицы в БД
     * @param parametres - список передаваемых параметров.
     * @throws SQLException
     */
    public void add(String tableName, Object ... parametres) throws SQLException{
        StringBuffer prepareQuery = new StringBuffer();
        prepareQuery.append("INSERT INTO "+ tableName);

        switch (tableName){
            case "Users":
                prepareQuery.append(" (name, passwd) ");
                break;
            case "Master":
                prepareQuery.append(" (name, count, author) ");
                break;
            case "Tasks":
                prepareQuery.append(" (title, body, createTime, modifyTime, finishTime, status, author, master) ");
                break;
        }
        prepareQuery.append("VALUES (");

        for (int i = 0; i < parametres.length; i++) {

            if (parametres[i] instanceof String)
                prepareQuery.append("\""+ parametres[i] +"\"");
            else
                prepareQuery.append(parametres[i]);

            if (i != parametres.length-1)
                prepareQuery.append(", ");
        }
        prepareQuery.append(");");

        statement.execute(prepareQuery.toString());
    }

    /**
     * Метод удаляющий строку из БД по ее ключу.
     * @param tableName - имя таблицы в БД
     * @param rowName - ключ строки.
     * @throws SQLException
     */
    private void delete (String tableName, String name, String rowName, String author, String master) throws SQLException{
        StringBuilder buildQuery = new StringBuilder();

        buildQuery.append("DELETE FROM ");
        buildQuery.append(tableName);
        buildQuery.append(" where "+ name +" = \""+ rowName +"\"");

        if (author != null)
            buildQuery.append(" AND author = \""+ author +"\"");
        if (master != null)
            buildQuery.append(" AND master = \""+ master +"\"");

        buildQuery.append(";");

        statement.execute(buildQuery.toString());
    }

    public void deleteFromMaster(String rowName, String author) throws SQLException {
        delete("Master", "name", rowName, author, null);
    }
    public void deleteFromTasks(String rowName, String author, String master) throws SQLException {
        delete("Tasks", "title", rowName, author, master);
    }

    public void clearTask(String author, String master) throws SQLException {
        StringBuilder buildQuery = new StringBuilder();

        buildQuery.append("DELETE FROM Tasks");
        buildQuery.append(" WHERE author = \""+ author +"\"");
        buildQuery.append(" AND master = \""+ master +"\"");

        buildQuery.append(";");

        statement.execute(buildQuery.toString());
    }
    public void clearDBFromUser(String user) throws SQLException {
        statement.execute("DELETE FROM Users where name =\""+ user +"\";");
        statement.execute("DELETE FROM Master where author =\""+ user +"\";");
        statement.execute("DELETE FROM Tasks where author =\""+ user +"\";");
    }
    public void dropTables() throws SQLException {
        statement.execute("DROP TABLE Users;");
        statement.execute("DROP TABLE Master;");
        statement.execute("DROP TABLE Tasks;");
        createTables();
    }

    /**
     * Метод изменяет состояние строки в таблице.
     * @param tableName - имя таблицы
     * @param parametres - новые параметры
     *<br>Для таблицы <b>Users:</b>
     *<br><b>paramentres[0]</b> - имя пользователя.
     *<br><b>paramentres[1]</b> - Условие поиска по id.
     *<br><b>paramentres[1]</b> - Условие поиска по name.
     *<br>
     *<br>Для таблицы <b>Masters:</b>
     *<br><b>paramentres[0]</b> - Элемент, который необходимо заменить (name).
     *<br><b>paramentres[1]</b> - Условие поиска по полю name.
     *<br>
     *<br>Для таблицы <b>Tasks:</b>
     *<br><b>paramentres[0]</b> - Title для замены.
     *<br><b>paramentres[1]</b> - Body для замены.
     *<br><b>paramentres[2]</b> - ModifyTime для замены.
     *<br><b>paramentres[3]</b> - finishTime для замены.
     *<br><b>paramentres[4]</b> - status для замены.
     *<br><b>paramentres[5]</b> - master для поиска.
     *<br><b>paramentres[6]</b> - title для поиска.
     *<br><b>paramentres[7]</b> - author для поиска.
     * @throws SQLException
     */
    public void change (String tableName, Object ... parametres) throws SQLException{
        StringBuilder prepareQuery = new StringBuilder();

        prepareQuery.append("UPDATE ");
        switch (tableName){
            case "Users":
                prepareQuery.append(tableName +" SET ");
                prepareQuery.append("name = \""+ parametres[0] +"\"");
                if (parametres[1] instanceof Integer)
                    prepareQuery.append(" where id = "+ parametres[1] + ";");
                else
                    prepareQuery.append(" where name = \""+ parametres[1] + "\";");
                break;
            case "Master":
                prepareQuery.append(tableName +" SET ");
                prepareQuery.append("name = \""+ parametres[0] +"\"");
                prepareQuery.append(" where name = \""+ parametres[1] + "\";");
                break;
            case "Tasks":
                prepareQuery.append(tableName +" SET ");
                prepareQuery.append("title = \""+ parametres[0] +"\", ");
                prepareQuery.append("body = \""+ parametres[1] +"\", ");
                prepareQuery.append("modifyTime = \""+ parametres[2] +"\", ");
                prepareQuery.append("finishTime = \""+ parametres[3] +"\", ");
                prepareQuery.append("status = \""+ parametres[4] +"\" ");
                prepareQuery.append(" where master = \""+ parametres[5] + "\"");
                prepareQuery.append(" AND title = \""+ parametres[6] + "\" ");
                prepareQuery.append(" AND author = \""+ parametres[7] + "\";");
                break;
        }

        statement.execute(prepareQuery.toString());
    }
    public void changeUserInDB(String newUser, String oldUser) throws SQLException {
        statement.execute("UPDATE Master SET author = \""+ newUser + "\" WHERE author =\""+ oldUser +"\";");
        statement.execute("UPDATE Tasks SET author = \""+ newUser + "\" WHERE author =\""+ oldUser +"\";");
    }
    public void changeUserInformation(String newUser, String oldUser, String passwd) throws SQLException {
        statement.execute("UPDATE Users SET name = \""+ newUser + "\", passwd = \""+ passwd +"\" WHERE name =\""+ oldUser +"\";");
    }

    /**
     * Метод возвращает значение одной строки из таблицы Users
     * @param tbName - Таблица для поиска значений
     * @param rowName - Выбор столбца для возврата
     * @param author - Выбор владельца записей
     * @return - ArrayList<String> Все значения столбца в таблице.
     * @throws SQLException
     */
    private ArrayList<String> getFromTable(String tbName, String rowName, String author, String masterSelected, String title) throws SQLException {
        ArrayList<String> result = new ArrayList<>();

        StringBuilder prepareQuery = new StringBuilder();

        prepareQuery.append("SELECT "+ rowName +" FROM "+ tbName +" ");

        if(author != null)
            prepareQuery.append("WHERE author=\""+ author +"\"");
        if (masterSelected != null)
            prepareQuery.append(" AND master =\""+ masterSelected +"\"");
        if (title != null)
            prepareQuery.append(" AND title =\""+ title +"\"");

        prepareQuery.append(";");

        ResultSet query = statement.executeQuery(prepareQuery.toString());

        while (query.next()){
            result.add(query.getString(rowName));
        }

        return result;
    }

    public ArrayList<String> getFromUsers (String rowName) throws SQLException {
        return this.getFromTable("Users", rowName, null, null, null);
    }
    public ArrayList<String> getFromMaster (String rowName, String author) throws SQLException {
        return this.getFromTable("Master", rowName, author, null, null);
    }
    public ArrayList<String> getFromTasks (String rowName, String author, String masterSelect, String title) throws SQLException {
        return this.getFromTable("Tasks", rowName, author, masterSelect, title);
    }

    /**
     * Метод возвращает размер таблицы. Используется для выяснения
     * пустая база или нет.
     * @param tbName - имя таблиц, размер которой необходимо узнать
     * @return int size - размер таблицы
     * @throws SQLException
     */
    public int getSize (String tbName, String user, String master) throws SQLException {

        StringBuilder query = new StringBuilder();

        query.append("SELECT COUNT(*) FROM ");
        query.append(tbName);

        if(user != null)
            query.append(" WHERE author =\""+ user +"\"");
        if(master != null)
            query.append(" AND master=\""+ master +"\"");

        query.append(";");

        connect();
        ResultSet rs = statement.executeQuery(query.toString());

        return rs.getInt(1);
    }
}
