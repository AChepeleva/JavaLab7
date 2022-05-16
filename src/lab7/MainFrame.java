package lab7;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

public class MainFrame extends JFrame {
    private boolean turn = true;
    private ButtonGroup radioButtons = new ButtonGroup();
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;

    public MainFrame() {
        super("Клиент мгновенных сообщений");
        this.setMinimumSize(new Dimension(500, 500));
        Toolkit kit = Toolkit.getDefaultToolkit();
        this.setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        this.textAreaIncoming = new JTextArea(10, 0);
        this.textAreaIncoming.setEditable(false);
        JScrollPane scrollPaneIncoming = new JScrollPane(this.textAreaIncoming);
        JLabel labelFrom = new JLabel("Подпись");
        JLabel labelTo = new JLabel("Получатель");
        this.textFieldFrom = new JTextField(10);
        this.textFieldTo = new JTextField(20);
        this.textAreaOutgoing = new JTextArea(5, 0);
        JScrollPane scrollPaneOutgoing = new JScrollPane(this.textAreaOutgoing);
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        final JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.sendMessage();
            }
        });
        ButtonGroup myButtons = new ButtonGroup();
        JRadioButton radio1 = new JRadioButton("Вкл.", true);
        myButtons.add(radio1);
        radio1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!MainFrame.this.turn) {
                    MainFrame.this.turn = true;
                    MainFrame.this.textAreaIncoming.append("Клиент включен\n");
                    sendButton.setEnabled(true);
                }

            }
        });
        JRadioButton radio2 = new JRadioButton("Выкл.", true);
        myButtons.add(radio2);
        radio2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (MainFrame.this.turn) {
                    MainFrame.this.turn = false;
                    MainFrame.this.textAreaIncoming.append("Клиент выключен\n");
                    sendButton.setEnabled(false);
                }

            }
        });
        GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup().addContainerGap().addGroup(layout2.createParallelGroup(Alignment.TRAILING).addGroup(layout2.createSequentialGroup().addComponent(labelFrom).addGap(5).addComponent(this.textFieldFrom).addGap(15).addComponent(labelTo).addGap(5).addComponent(this.textFieldTo)).addComponent(scrollPaneOutgoing).addComponent(radio1).addComponent(radio2).addComponent(sendButton)).addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup().addContainerGap().addGroup(layout2.createParallelGroup(Alignment.BASELINE).addComponent(labelFrom).addComponent(this.textFieldFrom).addComponent(labelTo).addComponent(this.textFieldTo)).addGap(10).addComponent(scrollPaneOutgoing).addGap(10).addComponent(sendButton).addComponent(radio1).addComponent(radio2).addContainerGap());
        GroupLayout layout1 = new GroupLayout(this.getContentPane());
        this.setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup().addContainerGap().addGroup(layout1.createParallelGroup().addComponent(scrollPaneIncoming).addComponent(messagePanel)).addContainerGap());
        layout1.setVerticalGroup(layout1.createSequentialGroup().addContainerGap().addComponent(scrollPaneIncoming).addGap(10).addComponent(messagePanel).addContainerGap());
        (new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(4567);

                    while(!Thread.interrupted()) {
                        Socket socket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        String senderName = in.readUTF();
                        String message = in.readUTF();
                        socket.close();
                        String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
                        MainFrame.this.textAreaIncoming.append(senderName + " (" + address + "): " + message + "\n");
                    }
                } catch (IOException var7) {
                    var7.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера", "Ошибка", 0);
                }

            }
        })).start();
    }

    private void sendMessage() {
        try {
            String senderName = this.textFieldFrom.getText();
            String destinationAddress = this.textFieldTo.getText();
            String message = this.textAreaOutgoing.getText();
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите имя отправителя", "Ошибка", 0);
                return;
            }

            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите адрес узла-получателя", "Ошибка", 0);
                return;
            }

            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст сообщения", "Ошибка", 0);
                return;
            }

            Socket socket = new Socket(destinationAddress, 4567);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(senderName);
            out.writeUTF(message);
            socket.close();
            if (this.turn) {
                this.textAreaIncoming.append("Я -> " + destinationAddress + ": " + message + "\n");
            }

            this.textAreaOutgoing.setText("");
        } catch (UnknownHostException var6) {
            var6.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось отправить сообщение: узел-адресат не найден", "Ошибка", 0);
        } catch (IOException var7) {
            var7.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось отправить сообщение", "Ошибка", 0);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setDefaultCloseOperation(3);
                frame.setVisible(true);
            }
        });
    }
}
