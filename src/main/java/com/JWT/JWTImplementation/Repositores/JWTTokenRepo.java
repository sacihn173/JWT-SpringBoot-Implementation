package com.JWT.JWTImplementation.Repositores;

import com.JWT.JWTImplementation.Models.JWTToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JWTTokenRepo extends JpaRepository<JWTToken, Integer> {

    Optional<JWTToken> findByToken(String token);

    @Query(
            value = "select * from jwt_token where customer_id = :id",
            nativeQuery = true
    )
    List<JWTToken> findAllTokensForACustomer(Integer id);

}
