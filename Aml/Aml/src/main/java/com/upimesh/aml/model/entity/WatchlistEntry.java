package com.upimesh.aml.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_entries", indexes = {
        @Index(name = "idx_watchlist_name", columnList = "name"),
        @Index(name = "idx_watchlist_type", columnList = "entityType"),
        @Index(name = "idx_watchlist_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String entityId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30) // "PEP", "SANCTIONS", "INTERNAL"
    private String entityType;

    @Column(nullable = false, length = 100) // "OFAC", "UN", "INTERNAL"
    private String source;

    @Column(length = 300)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
