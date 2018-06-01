package handler;

import model.ServerRoom;
import model.User;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import protocols.Protocols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TcpServerHandler extends IoHandlerAdapter {
    //key is room name
    private Map<String, ServerRoom> serverRoomMap = new TreeMap<>();
    private List<User> loggedUsers = new ArrayList<>();

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Object response;

        if (message instanceof Protocols.LoginToServerRequest) {
            System.out.println("arrived LoginToServerRequest");
            response = handleLoginRequestMessage((Protocols.LoginToServerRequest) message);
        } else if (message instanceof Protocols.LeaveServerRequest) {
            System.out.println("arrived LeaveServerRequest");
            response = handleLeaveServerRequest((Protocols.LeaveServerRequest) message);
        } else if (message instanceof Protocols.ManageRoomRequest) {
            System.out.println("arrived ManageRoomRequest");
            response = handleManageRoomRequest((Protocols.ManageRoomRequest) message);
        } else {
            System.out.println("Unknown message: " + message.getClass());
            System.out.println(message.toString());
            response = message;
        }

        System.out.println("Handling message end, serverRoomMap.size = "+serverRoomMap.size());
        System.out.println("ServerRoomMap.size = "+serverRoomMap.size());
        System.out.println("LoggedUsers.size = "+loggedUsers.size());

        System.out.println("Logged users: ");
        for(User user: loggedUsers){
            System.out.println(user.toString());
            user.printMutedUsers();
        }
        System.out.println("Rooms: ");
        for (Map.Entry<String, ServerRoom> serverRoomEntry : serverRoomMap.entrySet())
            System.out.println(serverRoomEntry.getValue().toString());
        session.write(response);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("IDLE " + session.getIdleCount(status));
    }

    private Protocols.LoginToServerResponse handleLoginRequestMessage(Protocols.LoginToServerRequest request) {
        System.out.println(request.toString());
        User user = new User();
        user.setNick(request.getNick());
        loggedUsers.add(user);

        Protocols.LoginToServerResponse.Builder responseBuilder = Protocols.LoginToServerResponse.newBuilder();
        for (Map.Entry<String, ServerRoom> serverRoomEntry : serverRoomMap.entrySet())
            responseBuilder.addRoomList(serverRoomEntry.getValue().toProtocol());
        responseBuilder.setStatus(Protocols.StatusCode.OK);
        System.out.println("LoginToServerResponse end, loggedUsers.size = "+loggedUsers.size());

        return responseBuilder.build();
    }

    private Protocols.LeaveServerResponse handleLeaveServerRequest(Protocols.LeaveServerRequest request) {
        Protocols.LeaveServerResponse.Builder response = Protocols.LeaveServerResponse.newBuilder();
        loggedUsers.removeIf(user -> user.getNick().equals(request.getNick()));
        response.setStatus(Protocols.StatusCode.OK);
        System.out.println("LeaveServerRequestHandling end, loggedUsers.size = "+loggedUsers.size());

        return response.build();
    }

    private Protocols.ManageRoomResponse handleManageRoomRequest(Protocols.ManageRoomRequest request) {
        Protocols.ManageRoomResponse response;
        System.out.println("ENUM = " + request.getManageRoomEnum());
        switch (request.getManageRoomEnum()) {
            case CREATE_ROOM:
                response = createRoom(request);
                break;
            case JOIN_ROOM:
                response = addUserToRoom(request);
                break;
            case KICK_USER:
                response = handlekickUserFromRoom(request);
                break;
            case LEAVE_ROOM:
                response = handleLeaveRoom(request);
                break;
            case DELETE_ROOM:
                response = handleDeleteRoom(request);
                break;
            case MUTE_USER:
                response = handleMuteUser(request);
                break;
            case UNMUTE_USER:
                response = handleUnmuteUser(request);
                break;
            default:
                Protocols.ManageRoomResponse.Builder responseBuilder = Protocols.ManageRoomResponse.newBuilder();
                responseBuilder.setStatus(Protocols.StatusCode.SERVER_ERROR);
                response = responseBuilder.build();
        }
        return response;
    }

    private Protocols.ManageRoomResponse createRoom(Protocols.ManageRoomRequest request) {
        ServerRoom serverRoom = new ServerRoom();
        serverRoom.setName(request.getRoomName());
        User owner = new User();
        owner.setNick(request.getNick());

        serverRoom.setOwner(owner);
        serverRoom.setPassword(request.getPassword());
        serverRoom.setAdminPassword(request.getAdminPassword());

        serverRoomMap.put(request.getRoomName(), serverRoom);

        Protocols.ManageRoomResponse.Builder responseBuilder = Protocols.ManageRoomResponse.newBuilder();
        responseBuilder.setStatus(Protocols.StatusCode.OK);
        return responseBuilder.build();
    }

    private Protocols.ManageRoomResponse addUserToRoom(Protocols.ManageRoomRequest request) {
        Protocols.ManageRoomResponse.Builder responseBuilder = Protocols.ManageRoomResponse.newBuilder();

        ServerRoom serverRoom = serverRoomMap.get(request.getRoomName());
        User user = findUserFromNick(request.getNick());
        if (serverRoom != null && user != null && serverRoom.getPassword().equals(request.getPassword())) {
            serverRoom.getUsersInRoom().add(user);
            responseBuilder.setStatus(Protocols.StatusCode.OK);
        } else {
            responseBuilder.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        }
        return responseBuilder.build();
    }

    private Protocols.ManageRoomResponse handleLeaveRoom(Protocols.ManageRoomRequest request) {
        Protocols.ManageRoomResponse.Builder response = Protocols.ManageRoomResponse.newBuilder();
        User user = findUserFromNick(request.getNick());
        if (user != null && serverRoomMap.get(request.getRoomName()).getUsersInRoom().remove(user))
            response.setStatus(Protocols.StatusCode.OK);
        else
            response.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        return response.build();

    }

    private Protocols.ManageRoomResponse handlekickUserFromRoom(Protocols.ManageRoomRequest request) {
        Protocols.ManageRoomResponse.Builder response = Protocols.ManageRoomResponse.newBuilder();
        ServerRoom serverRoom = serverRoomMap.get(request.getRoomName());
        User userToKick = findUserFromNick(request.getOtherUserNick());
        if (userToKick != null && serverRoom.getAdminPassword().equals(request.getAdminPassword())
                && serverRoom.getUsersInRoom().remove(userToKick))
            response.setStatus(Protocols.StatusCode.OK);
        else
            response.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        return response.build();
    }

    private Protocols.ManageRoomResponse handleDeleteRoom(Protocols.ManageRoomRequest request) {
        Protocols.ManageRoomResponse.Builder response = Protocols.ManageRoomResponse.newBuilder();
        ServerRoom serverRoom = serverRoomMap.get(request.getRoomName());
        if (serverRoom.getAdminPassword().equals(request.getAdminPassword())) {
            System.out.println("Romoving room from map");
            serverRoomMap.remove(request.getRoomName());
            response.setStatus(Protocols.StatusCode.OK);
        } else
            response.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        return response.build();

    }

    private Protocols.ManageRoomResponse  handleMuteUser(Protocols.ManageRoomRequest request){
        Protocols.ManageRoomResponse.Builder response = Protocols.ManageRoomResponse.newBuilder();
        User user= findUserFromNick(request.getNick());
        User userToMute = findUserFromNick(request.getOtherUserNick());
        if(user!= null && userToMute != null) {
            user.getMutedUserNicks().add(userToMute.getNick());
            response.setStatus(Protocols.StatusCode.OK);
        } else
            response.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        return response.build();
    }

    private Protocols.ManageRoomResponse  handleUnmuteUser(Protocols.ManageRoomRequest request){
        Protocols.ManageRoomResponse.Builder response = Protocols.ManageRoomResponse.newBuilder();
        User user= findUserFromNick(request.getNick());
        User userToUnmute = findUserFromNick(request.getOtherUserNick());
        if(user != null && userToUnmute != null) {
            user.getMutedUserNicks().remove(userToUnmute.getNick());
            response.setStatus(Protocols.StatusCode.OK);
        } else
            response.setStatus(Protocols.StatusCode.BAD_CREDENTIALS);
        return response.build();

    }

    private User findUserFromNick(String nick) {
        for (User user : loggedUsers) {
            if (user.getNick().equals(nick))
                return user;
        }
        return null;
    }

}
