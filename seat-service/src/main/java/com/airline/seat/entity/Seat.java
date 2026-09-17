package com.airline.seat.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_flight_seat", columnNames = {"flight_id", "seat_number"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false, length = 50)
    private String flightId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "is_preferred", nullable = false)
    private Boolean isPreferred;

    @Column(name = "base_price_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal basePriceMultiplier;

    public Seat() {
    }

    public Seat(Long id, String flightId, String seatNumber, SeatClass seatClass, Boolean isPreferred, BigDecimal basePriceMultiplier) {
        this.id = id;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.isPreferred = isPreferred;
        this.basePriceMultiplier = basePriceMultiplier;
    }

    public Seat(String flightId, String seatNumber, SeatClass seatClass, Boolean isPreferred, BigDecimal basePriceMultiplier) {
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.isPreferred = isPreferred;
        this.basePriceMultiplier = basePriceMultiplier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public Boolean getIsPreferred() {
        return isPreferred;
    }

    public void setIsPreferred(Boolean preferred) {
        isPreferred = preferred;
    }

    public BigDecimal getBasePriceMultiplier() {
        return basePriceMultiplier;
    }

    public void setBasePriceMultiplier(BigDecimal basePriceMultiplier) {
        this.basePriceMultiplier = basePriceMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id) &&
               Objects.equals(flightId, seat.flightId) &&
               Objects.equals(seatNumber, seat.seatNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flightId, seatNumber);
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", flightId='" + flightId + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatClass=" + seatClass +
                ", isPreferred=" + isPreferred +
                ", basePriceMultiplier=" + basePriceMultiplier +
                '}';
    }
}
