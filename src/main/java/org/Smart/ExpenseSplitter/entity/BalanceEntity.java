package org.Smart.ExpenseSplitter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "balances")
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceEntity extends BaseEntity {

    @EmbeddedId
    private BalanceId id;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("owesTo")
    @JoinColumn(name = "owes_to", nullable = false)
    private UserEntity owesTo;
}
