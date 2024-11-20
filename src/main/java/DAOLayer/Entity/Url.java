package DAOLayer.Entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "links_table", schema = "link_shortener")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int id;
    @Column(name = "original_url", length = 1000)
    public String originalUrl;
    @Column(name = "short_url", length = 45)
    public String shortUrl;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    public Timestamp createdAt;
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    public Timestamp updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}