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

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Y")
public class Bid extends Persistent {
    private AuctionItem item;
    private float amount;
    private Date datetime;
    private User bidder;

    @ManyToOne
    public AuctionItem getItem() {
        return item;
    }

    public void setItem(AuctionItem item) {
        this.item = item;
    }

    public float getAmount() {
        return amount;
    }

    @Column(nullable = false, name = "datetime")
    public Date getDatetime() {
        return datetime;
    }

    public void setAmount(float f) {
        amount = f;
    }

    public void setDatetime(Date date) {
        datetime = date;
    }

    @ManyToOne(optional = false)
    public User getBidder() {
        return bidder;
    }

    public void setBidder(User user) {
        bidder = user;
    }

    public String toString() {
        return bidder.getUserName() + " $" + amount;
    }

    @Transient
    public boolean isBuyNow() {
        return false;
    }

}
