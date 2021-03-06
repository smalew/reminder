package gui;

import db_logic.DataBaseClass;
import listeners.MainMenuListener;
import listeners.MainWindowListener;
import listeners.MainWindowMouseListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Koropenkods on 04.02.16.
 * Класс для построения графического интерфейса.
 * Является главным окном программы.
 */
public class MainWindow extends JFrame {

    private JPanel mainPanel;

    /*
     *Панели для левого окна вывода данных.
     */
    private JPanel btnMasterPanel,
                    mainMasterPanel,
                    infoPanel;
    /*
     *Панель для главного окна вывода данных.
     */
    private JPanel btnTaskPanel,
                    mainTaskPanel;
    /*
     * Переменная для главного меню
     */
    private JMenuBar mainMenu;
    private JMenu fileMenu, prefMenu;
    private JMenuItem newDBMenu, backupDBMenu, exitMenu;
    private JMenuItem usersMenu, helpMenu;

    private MainWindowListener btnListener;
    private MainWindowMouseListener mouseListener;
    private MainMenuListener menuListener;

    //Кнопки для управлением данными в левом окне.
    private JButton btnMasterAdd, btnMasterDelete, btnMasterOptions;
    //Кнопки для управлением данными в главном окне.
    private JButton btnTaskAdd, btnTaskDelete, btnTaskOptions;
    //Кнопки для управлением состоянием задач
    private JButton btnTaskPause, btnTaskStart, btnTaskComplete;

    //Определение активного пользователя
    private JLabel user, dbFile;

    //Переменные управления списком элементов в левом меню
    private JList masterList;

    //Переменные для управления списком элементов в основном меню
    private MyTableModel tableModel;
    private JTable taskTable;
    JScrollPane jscrlp;

    //База данных
    DataBaseClass database = null;

    public MainWindow(){
        setTitle(Constants.APPNAME);
        setSize(700, 450);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Constants.ICON.getImage());

        //Инициализируем UI
        initMenu();
        initData();
        initBtn();
        initInfoContainer();
        initListeners();
        initPanels();
    }

    private void initMenu(){

        mainMenu = new JMenuBar();

        fileMenu = new JMenu("Файл");
        prefMenu = new JMenu("Настройки");
        helpMenu = new JMenu("Помощь");

        newDBMenu = new JMenuItem("Новая БД");
        newDBMenu.setName("newDBMenu");
        newDBMenu.setIcon(Constants.NEW);

        backupDBMenu = new JMenuItem("Резервная копия БД");
        backupDBMenu.setName("backupDBMenu");
        backupDBMenu.setIcon(Constants.SAVE);

        exitMenu = new JMenuItem("Выход");
        exitMenu.setName("exit");
        exitMenu.setIcon(Constants.EXIT);

        usersMenu = new JMenuItem("Пользователи");
        usersMenu.setName("usersMenu");
        usersMenu.setIcon(Constants.USERS);

        helpMenu = new JMenuItem("Помощь");
        helpMenu.setName("helpMenu");
        helpMenu.setIcon(Constants.HELP);

        fileMenu.add(newDBMenu);
        fileMenu.add(backupDBMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenu);

        prefMenu.add(usersMenu);
        prefMenu.add(helpMenu);

        mainMenu.add(fileMenu);
        mainMenu.add(prefMenu);
    }
    private void initData(){
        //******** ЛЕВОЕ МЕНЮ******************
        //Создаем модель для работы со списком.
        masterList = new JList();
        masterList.setDragEnabled(false);

        try {
            database = DataBaseClass.getInstance();
            database.connect();

            ArrayList<String> tasks = database.getFromMaster("name", database.currentUser);

            masterList.setListData(tasks.toArray());
            masterList.setSelectedIndex(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (!database.databaseIsClosed())
                    database.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //********КОНЕЦ ЛЕВОЕ МЕНЮ******************

        //******** ОСНОВНОЕ МЕНЮ******************
        //Создаем модель для работы с таблицей.

        //Создаем таблицу и добавляем ей модель. Оборачиваем все в скроллпаин.
        tableModel = new MyTableModel();

        taskTable = new JTable();
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Устаналиваем размеры прокручиваемой области
        taskTable.setPreferredScrollableViewportSize(new Dimension(250, 100));

        //Заполняем таблицу данными.
        initTableContents();

        //Задаем модель для таблицы.
        taskTable.setModel(tableModel);
        jscrlp = new JScrollPane(taskTable);
        //Выделяем первую строчку в таблице
        if (tableModel.getRowCount() > 0)
           taskTable.setRowSelectionInterval(0,0);

        //А так же размеры столбцов
        taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(30);
        taskTable.getColumnModel().getColumn(1).setMaxWidth(10000);
        taskTable.getColumnModel().getColumn(2).setMaxWidth(70);
        //********КОНЕЦ ПРАВОЕ МЕНЮ******************
    }
    private void initTableContents(){
        try {
            database = DataBaseClass.getInstance();
            database.connect();

            ArrayList<Integer> id = new ArrayList<>();
            ArrayList<String> resultStatus = new ArrayList<>();

            ArrayList<String> title = database.getFromTasks("title", database.currentUser, (String) masterList.getSelectedValue(), null);
            ArrayList<String> status = database.getFromTasks("status", database.currentUser, (String) masterList.getSelectedValue(), null);

            //Заролняем таблицу
            for (int i = 1; i <= title.size(); i++) {
                id.add(i);
                resultStatus.add(Constants.TASK_STATUS[Integer.parseInt(status.get(i - 1))]);
            }
            tableModel.addColumn(Constants.ID, id.toArray());
            tableModel.addColumn(Constants.TASK, title.toArray());
            tableModel.addColumn(Constants.STATUS, resultStatus.toArray());
        }
         catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void initInfoContainer(){
        Dimension prefSize = new Dimension(170,25);

        user = new JLabel();
        user.setBorder(BorderFactory.createLoweredBevelBorder());
        user.setPreferredSize(prefSize);
        user.setText(" Пользователь: "+ DataBaseClass.currentUser);

        dbFile = new JLabel();
        dbFile.setBorder(BorderFactory.createLoweredBevelBorder());
        dbFile.setPreferredSize(prefSize);
        dbFile.setText(" БД: "+ DataBaseClass.currentDB);
    }

    private void initBtn(){
        btnMasterAdd = new JButton();
        btnMasterAdd.setIcon(Constants.ADD);
        btnMasterAdd.setToolTipText("Добавить");
        btnMasterAdd.setName("MasterAdd");

        btnMasterDelete = new JButton();
        btnMasterDelete.setIcon(Constants.DEL);
        btnMasterDelete.setToolTipText("Удалить");
        btnMasterDelete.setName("MasterDel");

        btnMasterOptions = new JButton();
        btnMasterOptions.setIcon(Constants.OPTIONS);
        btnMasterOptions.setToolTipText("Переименовать");
        btnMasterOptions.setName("MasterOptions");

        btnTaskAdd = new JButton();
        btnTaskAdd.setIcon(Constants.ADD);
        btnTaskAdd.setToolTipText("Добавить");
        btnTaskAdd.setName("TaskAdd");

        btnTaskDelete = new JButton();
        btnTaskDelete.setIcon(Constants.DEL);
        btnTaskDelete.setToolTipText("Удалить");
        btnTaskDelete.setName("TaskDel");

        btnTaskOptions = new JButton();
        btnTaskOptions.setIcon(Constants.OPTIONS);
        btnTaskOptions.setToolTipText("Модифицировать");
        btnTaskOptions.setName("TaskOptions");

        btnTaskComplete = new JButton();
        btnTaskComplete.setIcon(Constants.COMPLETE);
        btnTaskComplete.setToolTipText("Завершить");
        btnTaskComplete.setName("TaskComplete");

        btnTaskStart = new JButton();
        btnTaskStart.setIcon(Constants.START);
        btnTaskStart.setToolTipText("Старт задачи");
        btnTaskStart.setName("TaskStart");

        btnTaskPause = new JButton();
        btnTaskPause.setIcon(Constants.PAUSE);
        btnTaskPause.setToolTipText("Приостановить");
        btnTaskPause.setName("TaskPause");
    }
    private void initListeners(){

        menuListener = new MainMenuListener(masterList, tableModel, taskTable, user);
        newDBMenu.addActionListener(menuListener);
        backupDBMenu.addActionListener(menuListener);
        exitMenu.addActionListener(menuListener);
        usersMenu.addActionListener(menuListener);
        helpMenu.addActionListener(menuListener);

        mouseListener = new MainWindowMouseListener(masterList, tableModel, taskTable, user);
        masterList.addMouseListener(mouseListener);
        taskTable.addMouseListener(mouseListener);

        btnListener = new MainWindowListener(masterList, tableModel, taskTable, user);

        btnMasterAdd.addActionListener(btnListener);
        btnMasterDelete.addActionListener(btnListener);
        btnMasterOptions.addActionListener(btnListener);

        btnTaskAdd.addActionListener(btnListener);
        btnTaskDelete.addActionListener(btnListener);
        btnTaskOptions.addActionListener(btnListener);
        btnTaskComplete.addActionListener(btnListener);
        btnTaskStart.addActionListener(btnListener);
        btnTaskPause.addActionListener(btnListener);
    }

    private void initPanels(){
        //*****Создаем слои*****//
        BorderLayout mainLayout = new BorderLayout(2,2);
        BorderLayout leftMainLayout = new BorderLayout(5,5);
        BorderLayout dataMainLayout = new BorderLayout(5,5);

        FlowLayout btnLayout = new FlowLayout();
        btnLayout.setAlignment(FlowLayout.LEFT);
        //***********************//

        //*****Создаем панели*****//
        //Главная панель
        mainPanel = new JPanel(mainLayout);

        //Левая панель для кнопок.
        btnMasterPanel = new JPanel(btnLayout);
        btnMasterPanel.setBorder(new EtchedBorder());
        //Общая левая панель.
        mainMasterPanel = new JPanel(leftMainLayout);
        mainMasterPanel.setBorder(new EtchedBorder());
        //Панель вывода информации
        infoPanel = new JPanel(btnLayout);

        //Центральная панель для кнопок.
        btnTaskPanel = new JPanel(btnLayout);
        btnTaskPanel.setBorder(new EtchedBorder());
        //Общая центральная панель.
        mainTaskPanel = new JPanel(dataMainLayout);
        mainTaskPanel.setBorder(new EtchedBorder());
        //**************************//

        //*****Заполняем панели*****//
        //Заполняем левую панель
        btnMasterPanel.add(btnMasterAdd);
        btnMasterPanel.add(btnMasterDelete);
        btnMasterPanel.add(btnMasterOptions);
        mainMasterPanel.add(btnMasterPanel, BorderLayout.NORTH);
        mainMasterPanel.add(masterList, BorderLayout.CENTER);

        //Заполняем центральную панель
        btnTaskPanel.add(btnTaskAdd);
        btnTaskPanel.add(btnTaskDelete);
        btnTaskPanel.add(btnTaskOptions);
        btnTaskPanel.add(btnTaskPause);
        btnTaskPanel.add(btnTaskStart);
        btnTaskPanel.add(btnTaskComplete);
        mainTaskPanel.add(btnTaskPanel, BorderLayout.NORTH);
        mainTaskPanel.add(jscrlp, BorderLayout.CENTER);

        infoPanel.add(user);
        infoPanel.add(dbFile);

        //Заполняем главную панель данными
        mainPanel.add(mainMasterPanel, BorderLayout.WEST);
        mainPanel.add(mainTaskPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        //*************************//

        //Заполняем Frame
        setJMenuBar(mainMenu);
        getContentPane().add(mainPanel);
    }
}


/**
 * Класс для модели, запрещающей
 * изменение строк в таблице, что бы можно было
 * их проспатривать.
 */
class MyTableModel extends DefaultTableModel{
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
