package com.atb.appbankatb.Beans;

public class Transaction {


    Object libelle;
    Object date;
    Object price;

    Object ownerofService;

    public Transaction(Object libelle, Object date, Object price,Object ownerofService) {
        this.libelle = libelle;
        this.date = date;
        this.price = price;
        this.ownerofService = ownerofService;
    }

    public Object getOwnerofService() {
        return ownerofService;
    }

    public void setOwnerofService(Object ownerofService) {
        this.ownerofService = ownerofService;
    }

    public Object getLibelle() {
        return libelle;
    }

    public void setLibelle(Object libelle) {
        this.libelle = libelle;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }

    public Object getPrice() {
        return price;
    }

    public void setPrice(Object price) {
        this.price = price;
    }
}
