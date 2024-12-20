package org.Smart.ExpenseSplitter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ExpenseEntity> expenses;

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    private List<SettlementEntity> settlementsAsPayer;

    @OneToMany(mappedBy = "payee", fetch = FetchType.LAZY)
    private List<SettlementEntity> settlementsAsPayee;
}
