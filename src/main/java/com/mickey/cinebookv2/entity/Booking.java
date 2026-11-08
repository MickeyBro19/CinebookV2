package com.mickey.cinebookv2.entity;

import com.mickey.cinebookv2.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings",
    uniqueConstraints = @UniqueConstraint(name = "uk_booking_reference", columnNames = "booking_reference"),
    indexes = {@Index(name = "idx_booking_user", columnList = "user_id"), @Index(name = "idx_booking_show", columnList = "show_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, updatable = false, length = 40)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
