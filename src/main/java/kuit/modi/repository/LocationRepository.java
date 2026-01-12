package kuit.modi.repository;

import kuit.modi.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByAddress(String address);
    List<Location> findAllByAddress(String address);
    Optional<Location> findByAddressAndLatitudeAndLongitude(String address, Double latitude, Double longitude);
}