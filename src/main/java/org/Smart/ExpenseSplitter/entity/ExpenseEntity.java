package org.Smart.ExpenseSplitter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.Smart.ExpenseSplitter.type.ExpenseType;

import java.math.BigDecimal;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "exenses")
@Data
public class ExpenseEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ExpenseType expenseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
