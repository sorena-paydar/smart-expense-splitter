package org.Smart.ExpenseSplitter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<GroupEntity> ownedGroups;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private List<GroupEntity> joinedGroups;

    @JsonIgnore
    @OneToMany(mappedBy = "payer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseEntity> paidExpenses;

    @JsonIgnore
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private List<ExpenseEntity> participatedExpenses;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BalanceEntity> balancesOwing;

    @JsonIgnore
    @OneToMany(mappedBy = "owesTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BalanceEntity> balancesOwed;

}
