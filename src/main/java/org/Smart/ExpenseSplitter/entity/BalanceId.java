package org.Smart.ExpenseSplitter.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BalanceId implements Serializable {

    private Long groupId;  // Foreign key to GroupEntity
    private Long userId;   // Foreign key to the user who owes money
    private Long owesTo;   // Foreign key to the user who is owed money
}
