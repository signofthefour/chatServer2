package server;

import java.util.ArrayList;

public class GroupChat {
    private String name;
    private String id;
    private ArrayList<String> memberList = new ArrayList<String>(0);

    public GroupChat(String name) {setName(name);}
    public ArrayList<String> getMemberList() { return memberList;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public void addMember(String memName) {this.memberList.add(memName);}
}
