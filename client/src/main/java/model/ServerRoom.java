package model;

import protocols.Protocols;

import java.util.ArrayList;
import java.util.List;

public class ServerRoom {
    private String name;
    private List<User> usersInRoom = new ArrayList<User>();
    private User owner;
    private String password;
    private String adminPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsersInRoom() {
        return usersInRoom;
    }

    public void setUsersInRoom(List<User> usersInRoom) {
        this.usersInRoom = usersInRoom;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Protocols.Room toProtocol(){
        Protocols.Room.Builder roomBuilder = Protocols.Room.newBuilder();
        roomBuilder.setRoomName(this.name);
        roomBuilder.setOwner(this.owner.getNick());
        return  roomBuilder.build();
    }

    @Override
    public String toString() {
        return "ServerRoom{" +
                "name='" + name + '\'' +
                ", usersInRoom.size=" + usersInRoom.size() +
                ", owner=" + owner +
                ", password='" + password + '\'' +
                ", adminPassword='" + adminPassword + '\'' +
                '}';
    }
}
