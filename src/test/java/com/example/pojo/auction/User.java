package com.example.pojo.auction;

import java.util.List;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;

public class User extends Persistent {
    private String userName;
    private String password;
    private String email;
    private Name name;
    private List bids;
    private List auctions;
    private Set<String> phones;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setEmail(String string) {
        email = string;
    }

    public void setPassword(String string) {
        password = string;
    }

    public void setUserName(String string) {
        userName = string;
    }

    public List getAuctions() {
        return auctions;
    }

    public List getBids() {
        return bids;
    }

    public void setAuctions(List list) {
        auctions = list;
    }

    public void setBids(List list) {
        bids = list;
    }

    public String toString() {
        return userName;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    @ElementCollection
    @CollectionTable(name = "user_phone")
    public Set<String> getPhones() {
        return phones;
    }

    public void setPhones(Set<String> phones) {
        this.phones = phones;
    }

}
