package gui;

import db_logic.DataBaseClass;
import listeners.LogInListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * Created by Koropenkods on 03.03.16.
 * <p>GUI для аутентификации пользователя в программе</p>
 * <p>Показывается при наличие нестандартного пользователя<br>
 *     в БД. Требует ввести логин и пароль.</p>
 */
public class LogInWindow extends JFrame {

    private JPanel mainPanel, textPanel, buttonPanel;

    private JTextField userText;
    private JPasswordField passwdText;

    private JButton btnLogIn, btnCancel;

    public LogInWindow(){
        setTitle(Constants.APPNAME);
        setIconImage(Constants.ICON.getImage());
        setSize(200,170);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initTexts();
        initBtn();
        initPanels();

        getContentPane().add(mainPanel);
    }
    public void initTexts(){
        Dimension minSize = new Dimension(160,40);
        Dimension prefSize = new Dimension(170,40);
        Dimension maxSize = new Dimension(180,40);


        userText = new JTextField();
        userText.setMinimumSize(minSize);
        userText.setPreferredSize(prefSize);
        userText.setMaximumSize(maxSize);
        userText.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),"Пользователь"));

        passwdText = new JPasswordField();
        passwdText.setMinimumSize(minSize);
        passwdText.setPreferredSize(prefSize);
        passwdText.setMaximumSize(maxSize);
        passwdText.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),"Пароль"));

    }
    public void initBtn(){
        LogInListener logInListener = new LogInListener(userText, passwdText, this);

        btnLogIn = new JButton("Войти");
        btnCancel = new JButton("Выход");

        Dimension minSize = new Dimension(90,30);
        Dimension prefSize = new Dimension(90,30);
        Dimension maxSize = new Dimension(100,30);

        btnLogIn.setMinimumSize(minSize);
        btnLogIn.setPreferredSize(prefSize);
        btnLogIn.setMaximumSize(maxSize);
        btnLogIn.addActionListener(logInListener);

        btnCancel.setMinimumSize(minSize);
        btnCancel.setPreferredSize(prefSize);
        btnCancel.setMaximumSize(maxSize);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    public void initPanels(){
        BorderLayout mainLayout = new BorderLayout(5,5);
        FlowLayout elementsLayout = new FlowLayout();

        textPanel = new JPanel(elementsLayout);
        buttonPanel = new JPanel(elementsLayout);

        textPanel.add(userText);
        textPanel.add(passwdText);

        buttonPanel.add(btnLogIn);
        buttonPanel.add(btnCancel);

        mainPanel = new JPanel(mainLayout);
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
}
