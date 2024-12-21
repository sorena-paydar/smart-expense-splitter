package org.Smart.ExpenseSplitter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<GroupEntity> groupsCreated;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private List<GroupEntity> groupsJoined;

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExpenseEntity> expensesPaid;

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SettlementEntity> settlementsAsPayer;

    @OneToMany(mappedBy = "payee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SettlementEntity> settlementsAsPayee;
}
