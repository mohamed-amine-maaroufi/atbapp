package com.atb.appbankatb.Beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CodeQr {

    String id;
    String libelle;
    String description;
    String coleur;
    String id_client;

    public CodeQr(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description;
    }

    public String getColeur() {
        return coleur;
    }

    public void setColeur(String coleur) {
        coleur = coleur;
    }

    public String getId_client() {
        return id_client;
    }

    public void setId_client(String id_client) {
        this.id_client = id_client;
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("libelle", libelle);
        result.put("description", description);
        result.put("coleur", coleur);

        return result;
    }
}
