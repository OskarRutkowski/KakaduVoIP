package tcpMessage;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import protocols.Protocols;

import java.util.List;

public class TcpMessage extends Application {
    private static String HOSTNAME = "localhost";
    private static int PORT = 9123;

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("./views/ClientPanel.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);


        primaryStage.setTitle("KakaduVoIP - komunikator głosowy");
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                primaryStage.close();
            }
        });
    }
    public static void main(String[] args) throws InterruptedException {
        String nick = "wojakzielinski";
        String otherUser = "niechcianyGość";
        String roomName = "Pokój1";

        launch(args);

        List<Protocols.Room> serverRooms = loginToServer(nick);
        if (serverRooms.size() == 0) {
            createRoom(nick, roomName);
        }
        joinRoom(nick, roomName);

        //other users log to room
        serverRooms = loginToServer(otherUser);
        joinRoom(otherUser, serverRooms.get(0).getRoomName());

        //mute user
        muteUser(nick, roomName, otherUser);
        //unmute user
        unmuteUser(nick, roomName, otherUser);
        //kick user from room
        kickUser(nick, roomName, otherUser);
        //leaveroom
        leaveRoom(nick, roomName);
        //deleteRoom
        deleteRoom(nick, roomName);
        //leave server
        leaveServer(nick);
        leaveServer(otherUser);
    }

    private static List<Protocols.Room> loginToServer(String nick) throws InterruptedException {
        Protocols.LoginToServerRequest.Builder request = Protocols.LoginToServerRequest.newBuilder();
        request.setNick(nick);
        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.LoginToServerResponse response = (Protocols.LoginToServerResponse) message;

        System.out.println("LoginToServerResponse = " + response.toString());
        return response.getRoomListList();
    }

    private static void createRoom(String nick, String roomName) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.CREATE_ROOM);
        request.setPassword("passwordToServer");
        request.setAdminPassword("adminPassword");
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void joinRoom(String nick, String roomName) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.JOIN_ROOM);
        request.setPassword("passwordToServer");
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void leaveRoom(String nick, String roomName) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.LEAVE_ROOM);
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void deleteRoom(String nick, String roomName) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.DELETE_ROOM);
        request.setPassword("passwordToServer");
        request.setAdminPassword("adminPassword");
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void muteUser(String nick, String roomName, String userToMute) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.MUTE_USER);
        request.setPassword("passwordToServer");
        request.setRoomName(roomName);
        request.setOtherUserNick(userToMute);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void unmuteUser(String nick, String roomName, String userToUnmute) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.UNMUTE_USER);
        request.setPassword("passwordToServer");
        request.setRoomName(roomName);
        request.setOtherUserNick(userToUnmute);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void kickUser(String nick, String roomName, String unwantedUserNick) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.KICK_USER);
        request.setPassword("passwordToServer");
        request.setAdminPassword("adminPassword");
        request.setRoomName(roomName);
        request.setOtherUserNick(unwantedUserNick);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }

    private static void leaveServer(String nick) throws InterruptedException {
        Protocols.LeaveServerRequest.Builder requestBuilder = Protocols.LeaveServerRequest.newBuilder();
        requestBuilder.setNick(nick);

        Object message = new TcpClient(HOSTNAME, PORT).send(requestBuilder.build());
        Protocols.LeaveServerResponse response = (Protocols.LeaveServerResponse) message;
        System.out.println(response.toString());
    }

}
