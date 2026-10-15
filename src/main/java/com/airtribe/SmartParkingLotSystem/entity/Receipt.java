package com.airtribe.SmartParkingLotSystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long receiptId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false)
    private long durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    protected Receipt() {}

    public Receipt(long receiptId, Booking booking, long durationMinutes, BigDecimal amount, LocalDateTime paidAt) {
        this.receiptId = receiptId;
        this.booking = booking;
        this.durationMinutes = durationMinutes;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public Receipt(Booking booking, long minutes, BigDecimal fee, LocalDateTime exitTime) {
        this.booking = booking;
        this.durationMinutes = minutes;
        this.amount = fee;
        this.paidAt = exitTime;
    }

    public long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(long receiptId) {
        this.receiptId = receiptId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
