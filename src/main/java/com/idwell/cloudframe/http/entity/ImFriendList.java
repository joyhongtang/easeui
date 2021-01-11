package com.idwell.cloudframe.http.entity;

import java.util.ArrayList;

public class ImFriendList {
    public ArrayList<ImFriend> getList() {
        return list;
    }

    public void setList(ArrayList<ImFriend> list) {
        this.list = list;
    }

    ArrayList<ImFriend> list;
    String count;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
