package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMpPreferenceId(String preferenceId);

    Optional<Payment> findByMpPaymentId(String mpPaymentId);
}