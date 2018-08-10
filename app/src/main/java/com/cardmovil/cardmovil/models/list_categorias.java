package com.cardmovil.cardmovil.models;

public class list_categorias {

    private String Cat_Desc, Cat_Codigo, Cat_Imagen, Item_Selecateg, SC_Estado;

    public String getCat_Desc() {
        return Cat_Desc;
    }

    public void setCat_Desc(String cat_Desc) {
        Cat_Desc = cat_Desc;
    }

    public String getCat_Codigo() {
        return Cat_Codigo;
    }

    public void setCat_Codigo(String cat_Codigo) {
        Cat_Codigo = cat_Codigo;
    }

    public String getCat_Imagen() {
        return Cat_Imagen;
    }

    public void setCat_Imagen(String cat_Imagen) {
        Cat_Imagen = cat_Imagen;
    }

    public String getSC_Estado() {
        return SC_Estado;
    }

    public void setSC_Estado(String SC_Estado) {
        this.SC_Estado = SC_Estado;
    }

    public String getItem_Selecateg() {
        return Item_Selecateg;
    }

    public void setItem_Selecateg(String item_Selecateg) {
        Item_Selecateg = item_Selecateg;
    }
}
