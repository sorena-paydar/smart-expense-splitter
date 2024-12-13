package org.Smart.ExpenseSplitter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "`groups`")
@Data
public class GroupEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<ExpenseEntity> expenses;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<SettlementEntity> settlements;
}
