package org.tp1.hotellerie.model;

import javax.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "numero_carte_bleue", nullable = false)
    private String numeroCarteBleue;

    // Constructeur par défaut requis par JPA
    public Client() {
    }

    public Client(String nom, String prenom, String numeroCarteBleue) {
        this.nom = nom;
        this.prenom = prenom;
        this.numeroCarteBleue = numeroCarteBleue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumeroCarteBleue() {
        return numeroCarteBleue;
    }

    public void setNumeroCarteBleue(String numeroCarteBleue) {
        this.numeroCarteBleue = numeroCarteBleue;
    }
}
