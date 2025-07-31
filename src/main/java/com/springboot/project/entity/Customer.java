package com.springboot.project.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "customers")
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account number is required")
    @Column(unique = true, name = "account_number")
    private String accountNumber;

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Telephone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid telephone format")
    private String telephone;

    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt =  LocalDateTime.now();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills;

    public Customer(String accountNumber, String name, String address, String telephone, String email) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.telephone = telephone;
        this.accountNumber = accountNumber;
    }

}
