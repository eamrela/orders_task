/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jumia.task.orders.domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Amr
 */
@Embeddable
public class ItemPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "item_id")
    private long itemId;
    @Basic(optional = false)
    @Column(name = "product_id")
    private long productId;

    public ItemPK() {
    }

    public ItemPK(long itemId, long productId) {
        this.itemId = itemId;
        this.productId = productId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) itemId;
        hash += (int) productId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ItemPK)) {
            return false;
        }
        ItemPK other = (ItemPK) object;
        if (this.itemId != other.itemId) {
            return false;
        }
        return this.productId == other.productId;
    }

    @Override
    public String toString() {
        return "com.jumia.task.orders.domain.ItemPK[ itemId=" + itemId + ", productId=" + productId + " ]";
    }
    
}
