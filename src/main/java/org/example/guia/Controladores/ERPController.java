package org.example.guia.Controladores;

public class ERPController {

    String userEmail = "usuario@ejemplo.com";
    String odooDomain = "tuempresa.odoo.com";
    String odooDatabase = "tuBaseDeDatos";

    String odooLoginUrl = String.format("https://%s/web/login?db=%s&login=%s",
            odooDomain,
            odooDatabase,
            userEmail);
}
