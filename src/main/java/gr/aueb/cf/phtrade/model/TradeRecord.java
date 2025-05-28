package gr.aueb.cf.phtrade.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "trade_records")
public class TradeRecord extends AbstractEntity implements IdentifiableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private Double amount;

    // το lazy δηλωνει οτι θελουμε να φορτωθει μονο αν ζητηθει.
    // (.EAGER φορτωνεται παντα)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_id")
    private Pharmacy giver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Pharmacy receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorder_id")
    private User recorder;

    @Column(name = "deleted_by_giver")
    private boolean deletedByGiver = false;

    @Column(name = "deleted_by_receiver")
    private boolean deletedByReceiver = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Override
    public String toString() {
        return "TradeRecord{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", giver=" + giver.getName() +
                ", receiver=" + receiver.getName() +
                ", recorder=" + recorder.getUsername() +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
