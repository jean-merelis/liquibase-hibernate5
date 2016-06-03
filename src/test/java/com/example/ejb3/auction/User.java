package com.example.ejb3.auction;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;

@Entity
public class User extends Persistent {

    private String userName;
    private String password;
    private String email;
    private Name name;
    private List<Bid> bids;
    private List<AuctionItem> auctions;
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

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    public List<AuctionItem> getAuctions() {
        return auctions;
    }

    @OneToMany(mappedBy = "bidder", cascade = CascadeType.ALL)
    public List<Bid> getBids() {
        return bids;
    }

    @ElementCollection
    @CollectionTable(name = "user_phone")
    public Set<String> getPhones() {
        return phones;
    }

    public void setPhones(Set<String> phones) {
        this.phones = phones;
    }

    public void setAuctions(List<AuctionItem> list) {
        auctions = list;
    }

    public void setBids(List<Bid> list) {
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

}
