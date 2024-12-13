package org.Smart.ExpenseSplitter.entity;

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

    private String username;
    private String email;
    private String password;

    @OneToMany(mappedBy = "user")
    private List<ExpenseEntity> expenses;

    @OneToMany(mappedBy = "payer")
    private List<SettlementEntity> settlementsAsPayer;

    @OneToMany(mappedBy = "payee")
    private List<SettlementEntity> settlementsAsPayee;
}
