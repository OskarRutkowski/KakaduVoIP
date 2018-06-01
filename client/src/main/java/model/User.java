package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String nick;
    private List<String> mutedUserNicks = new ArrayList<>();

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public List<String> getMutedUserNicks() {
        return mutedUserNicks;
    }

    public void setMutedUserNicks(List<String> mutedUserNicks) {
        this.mutedUserNicks = mutedUserNicks;
    }

    @Override
    public String toString() {
        return "User{" +
                "nick='" + nick + '\'' +
                ", mutedUserNicks.size=" + mutedUserNicks.size() +
                '}';
    }

    public void printMutedUsers(){
        for(String mutedUsersNick: mutedUserNicks)
            System.out.println(mutedUsersNick);
    }
}
