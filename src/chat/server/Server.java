package chat.server;


import java.awt.TextArea;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import chat.client.DataPanel;
import chat.client.FolderTransmit;
import chat.client.UserClient;
import chat.client.WarningFrame;

/**
 * Created by zsc on 2015/10/3.
 */




public class Server {

    private int port;
    private ServerSocket serverSocket;
    private boolean flag = true;
    private DataPanel dataPanel = null;
    private FolderTransmit folderTransmit = null;

    public Server(DataPanel dataPanel) {
        this.dataPanel =dataPanel;
    }

    public String listen(){
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                while(true){
                    try {
                        port = random.nextInt(65535);
                        serverSocket = new ServerSocket(port);
                        break;
                    } catch (IOException e) {}
                }
                while(flag){
                    try {
                        Socket socket = serverSocket.accept();
                        String cmd = getRequestCmd(socket);
                        if("FolderTransmit".equals(cmd)){
                            if(DataPanel.isRecving){
                                new UserClient(socket).sendMsg("对方接收文件中,请稍候再尝试...\n\n");
                                continue;
                            }
                            if(DataPanel.isSending){
                                new UserClient(socket).sendMsg("对方发送文件中,请稍候再尝试...\n\n");
                                continue;
                            }
                            String warning = "【" + getRequestCmd(socket) + "】 发来文件【" + getRequestCmd(socket) + "】请接收.\n";
//                            dataPanel.getTxtaChatContent().append(warning);
                            dataPanel.appendChatMsg(warning);
                            dataPanel.displayMsg("文件接收", warning);
                            dataPanel.getBtnRecv().setVisible(true);
                            folderTransmit = new FolderTransmit(socket, dataPanel);
                        }else{
                            UserClient client = new UserClient(socket);
                            new ReceiveMsg(client).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

            private String getRequestCmd(Socket socket) {
                String cmd = "";
                try {
                    cmd = new UserClient(socket).receiveMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return cmd;
            }
        };
        new Thread(listener).start();
        String endpoint = "";
        try {
            endpoint = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return endpoint;
    }

    public int getPort() {
        return port;
    }

    private class ReceiveMsg extends Thread{
        private UserClient client;

        public ReceiveMsg(UserClient userClient) {
            this.client = userClient;
        }

        @Override
        public void run() {
            while(true){
                try {
                    String msg = client.receiveMsg();
                    if(msg.startsWith("△imXwc▲")){
                        msg = msg.substring(7);
                        WarningFrame.showMsg(msg);
                    }
                    TextArea textArea = dataPanel.getTxtaChatContent();
                    //textArea.append(msg);
                    //textArea.append("\n");
                    dataPanel.appendChatMsg(msg);
                    dataPanel.displayMsg("私聊消息", msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    client = null;
                    dataPanel.getUserList().setVisible(true);
                    break;
                }
            }
        }

    }

    public void stop() {
        try {
            flag = false;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFolder(String path) {
        folderTransmit.receive(path);
    }

    public void breakRecvFolder() {
        folderTransmit.breakRecvFolder();
    }
}
