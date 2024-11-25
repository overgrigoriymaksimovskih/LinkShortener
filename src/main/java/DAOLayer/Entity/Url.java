package DAOLayer.Entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "links_table", schema = "link_shortener")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;;
    @Column(name = "original_url", length = 1000)
    private String originalUrl;
    @Column(name = "short_url", length = 45)
    private String shortUrl;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private Timestamp createdAt;
//    @Column(name = "user_id", length = 45)
    @Column(name = "user_id", length = 45, nullable = true)
    private Long userId;

    public long getId() {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}