/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jumia.task.orders.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Amr
 */
@Entity
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ItemPK itemPK;
    @Column(name = "cost")
    private Double cost;
    @Column(name = "shipping_fee")
    private Double shippingFee;
    @Column(name = "tax_amount")
    private Double taxAmount;
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Order orderId;
    @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Product product;

    public Item() {
    }

    public Item(ItemPK itemPK) {
        this.itemPK = itemPK;
    }

    public Item(long itemId, long productId) {
        this.itemPK = new ItemPK(itemId, productId);
    }

    public ItemPK getItemPK() {
        return itemPK;
    }

    public void setItemPK(ItemPK itemPK) {
        this.itemPK = itemPK;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Order getOrderId() {
        return orderId;
    }

    public void setOrderId(Order orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itemPK != null ? itemPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Item)) {
            return false;
        }
        Item other = (Item) object;
        return !((this.itemPK == null && other.itemPK != null) || (this.itemPK != null && !this.itemPK.equals(other.itemPK)));
    }

    @Override
    public String toString() {
        return "com.jumia.task.orders.domain.Item[ itemPK=" + itemPK + " ]";
    }
    
}
