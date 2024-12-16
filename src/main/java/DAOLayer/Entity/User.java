package DAOLayer.Entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users_table", schema = "link_shortener")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "login", length = 45)
    private String login;

    @Column(name = "password", length = 100)
    private String password;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
