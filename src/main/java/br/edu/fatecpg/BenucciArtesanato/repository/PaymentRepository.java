package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}