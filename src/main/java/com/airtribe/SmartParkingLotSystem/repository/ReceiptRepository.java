package com.airtribe.SmartParkingLotSystem.repository;

import com.airtribe.SmartParkingLotSystem.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
