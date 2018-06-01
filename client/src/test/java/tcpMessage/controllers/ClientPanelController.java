package tcpMessage.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import protocols.Protocols;
import tcpMessage.TcpClient;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientPanelController implements Initializable {

    private static String HOSTNAME;//localhost
    private static int PORT;//9123
    private List<Protocols.Room> rooms;
    private String username;
    private TempRoom selectedRoom;


    @FXML
    private AnchorPane start_pane,create_room_pane,rooms_pane,room_with_users_pane_admin, room_with_users_pane_user, info_pane;

    @FXML
    public void show_info(ActionEvent event){
        info_pane.toFront();
    }
    @FXML
    public void menu_go_back(MouseEvent event){
        info_pane.toBack();
    }
    @FXML
    public void leave_server(ActionEvent event) throws InterruptedException {
        start_pane.toFront();
        leaveServer(username);
    }

    //Start view
    @FXML
    private TextField v_username;
    @FXML
    private TextField v_host;
    @FXML
    private TextField v_port;
    @FXML
    public void connect_login(MouseEvent event) throws InterruptedException {


        username = v_username.getText();
        HOSTNAME = v_host.getText();
        PORT = Integer.parseInt(v_port.getText());

        rooms = loginToServer(username);
        rooms_pane.toFront();
        reloadRooms();
    }

    //Rooms view
    @FXML
    private TableView<TempRoom> room_tbl = new TableView<>();
    @FXML
    private TableColumn<TempRoom, String> name_col,owner_col;
    @FXML
    public void room_connect(MouseEvent event) throws InterruptedException {

        String room_password = "";


        //Get actual clicked row in table
        selectedRoom = room_tbl.getSelectionModel().getSelectedItem();
        //Join selected room
        if(room_tbl.getSelectionModel().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Brak wybranego pokoju");
            alert.setHeaderText(null);
            alert.setContentText("Aby dołączyć do pokoju, należy najpierw go wybrać z listy.");

            alert.showAndWait();
        }
        else{
            //Text Input dialog
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Podaj hasło");
            dialog.setHeaderText("Aby wejść do pokoju, należy podać hasło zabezpieczające pokój.");
            dialog.setContentText("Podaj hasło:");
            Optional<String> password = dialog.showAndWait();
            if(password.isPresent()){
                room_password = password.get();
            }

            if(username.equals(selectedRoom.owner)){
                //Redirect to room with users view
                if(joinRoom(username,selectedRoom.name,room_password).equals("BAD_CREDENTIALS")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Złe hasło");
                    alert.setHeaderText(null);
                    alert.setContentText("Wprowadź poprawne hasło do pokoju.");

                    alert.showAndWait();
                }
                else {
                    room_lbl_admin.setText(selectedRoom.name);
                    user_lbl_admin.setText(username);
                    room_with_users_pane_admin.toFront();
                }

            }
            else{
                if(joinRoom(username,selectedRoom.name, room_password).equals("BAD_CREDENTIALS")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Złe hasło");
                    alert.setHeaderText(null);
                    alert.setContentText("Wprowadź poprawne hasło do pokoju.");

                    alert.showAndWait();
                }
                else {
                    room_lbl_user.setText(selectedRoom.name);
                    user_lbl_user.setText(username);
                    room_with_users_pane_user.toFront();
                }
            }
        }
    }
    @FXML
    public void room_create(MouseEvent event){
        create_room_pane.toFront();
    }
    @FXML
    public void reload_rooms(MouseEvent event) throws InterruptedException {

        reloadRooms();
    }

    public void reloadRooms() throws InterruptedException {
        //trzeba zmienic na protokol odswiezania
        rooms = loginToServer("");

        //Clear whole table
        room_tbl.getItems().clear();
        //Get items to refreshed table
        for(int i=0;i<rooms.size();i++) {
            room_tbl.getItems().add(new TempRoom(rooms.get(i).getRoomName(), rooms.get(i).getOwner()));
        }
        name_col.setCellValueFactory(new PropertyValueFactory<TempRoom,String>("name"));
        owner_col.setCellValueFactory(new PropertyValueFactory<TempRoom,String>("owner"));
    }

    //Temporary Class TempRoom for rooms properties
    public class TempRoom{
        private String name,owner;
        TempRoom(String name, String owner){
            this.name = name;
            this.owner = owner;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }
    }

    @FXML
    public void choose_row(MouseEvent event){

    }
    //Inside room view

    @FXML
    private  Label room_lbl_user,user_lbl_user,room_lbl_admin,user_lbl_admin;

    @FXML
    private TableView<TempRoom> users_tbl = new TableView<>();
    @FXML
    private TableColumn<TempRoom, String> username_room_col;

    public void reloadUsers(){
        //do zrobienia
    }

    @FXML
    public void leave_room(MouseEvent event) throws InterruptedException {
        leaveRoom(username, selectedRoom.name);
        reloadRooms();
        rooms_pane.toFront();
    }
    @FXML
    private void delete_room(MouseEvent event) throws InterruptedException {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Weryfikacja administratora");
        dialog.setHeaderText("Aby usunąć pokój, należy podać hasło dostępu do pokoju oraz hasło administracyjne.");

        ButtonType submitButton = new ButtonType("Potwierdź", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, cancelButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField password = new PasswordField();
        password.setPromptText("Hasło dostępu");
        PasswordField adminPassword = new PasswordField();
        adminPassword.setPromptText("Hasło administracyjne");

        grid.add(new Label("Podaj hasło dostępu:"), 0, 0);
        grid.add(password, 1, 0);
        grid.add(new Label("Podaj hasło administracyjne:"), 0, 1);
        grid.add(adminPassword, 1, 1);



        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> password.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return new Pair<>(password.getText(), adminPassword.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
        });

        if(result.isPresent()){
            if(deleteRoom(username, selectedRoom.name, result.get().getKey(), result.get().getValue()).equals("BAD_CREDENTIALS")){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Złe hasła");
                alert.setHeaderText(null);
                alert.setContentText("Wprowadź poprawne hasła do pokoju.");

                alert.showAndWait();
            }
            else{
                reloadRooms();
                rooms_pane.toFront();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Złe dane");
            alert.setHeaderText(null);
            alert.setContentText("Wprowadź hasło zabezpieczające pokój oraz hasło administracyjne.");

            alert.showAndWait();
        }
    }
    //Create room view
    @FXML
    private TextField v_room_passadmin,v_room_passwd,v_room_name;
    @FXML
    public void go_back(MouseEvent event){
        rooms_pane.toFront();
    }
    @FXML
    public void create_create_room(MouseEvent event) throws InterruptedException {
        String roomname, roompass, roompassadm;
        roomname = v_room_name.getText();
        roompass = v_room_passwd.getText();
        roompassadm = v_room_passadmin.getText();

        createRoom(username,roomname,roompass,roompassadm);
        reloadRooms();
        rooms_pane.toFront();
    }
    @FXML
    public void help(MouseEvent event){

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private static List<Protocols.Room> loginToServer(String nick) throws InterruptedException {
        Protocols.LoginToServerRequest.Builder request = Protocols.LoginToServerRequest.newBuilder();
        request.setNick(nick);
        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.LoginToServerResponse response = (Protocols.LoginToServerResponse) message;

        System.out.println("LoginToServerResponse = " + response.toString());
        return response.getRoomListList();
    }
    private static void createRoom(String nick, String roomName, String password, String adminPassword) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.CREATE_ROOM);
        request.setPassword(password);
        request.setAdminPassword(adminPassword);
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
    }
    private static String joinRoom(String nick, String roomName, String password) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.JOIN_ROOM);
        request.setPassword(password);
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
        return response.getStatus().toString();
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
    private static String deleteRoom(String nick, String roomName,String password, String adminPassword) throws InterruptedException {
        Protocols.ManageRoomRequest.Builder request = Protocols.ManageRoomRequest.newBuilder();
        request.setNick(nick);
        request.setManageRoomEnum(Protocols.ManageRoomEnum.DELETE_ROOM);
        request.setPassword(password);
        request.setAdminPassword(adminPassword);
        request.setRoomName(roomName);

        Object message = new TcpClient(HOSTNAME, PORT).send(request.build());
        Protocols.ManageRoomResponse response = (Protocols.ManageRoomResponse) message;
        System.out.println(response.toString());
        return response.getStatus().toString();
    }
    private static void leaveServer(String nick) throws InterruptedException {
        Protocols.LeaveServerRequest.Builder requestBuilder = Protocols.LeaveServerRequest.newBuilder();
        requestBuilder.setNick(nick);

        Object message = new TcpClient(HOSTNAME, PORT).send(requestBuilder.build());
        Protocols.LeaveServerResponse response = (Protocols.LeaveServerResponse) message;
        System.out.println(response.toString());
    }
}
