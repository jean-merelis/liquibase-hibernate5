package com.example.ejb3.auction;

/*
 * #%L
 * Liquibase Hibernate 5 Integration
 * %%
 * Copyright (C) 2016 Liquibase.org
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "email"))
public class User extends Persistent {

    private String userName;
    private String password;
    private String email;
    private Name name;
    private List<Bid> bids;
    private List<AuctionItem> auctions;
    private List<String> phones;

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
    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
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
