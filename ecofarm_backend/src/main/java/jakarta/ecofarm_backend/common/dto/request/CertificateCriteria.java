package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateCriteria extends BaseCriteria {

    @QueryParam("greenhouseId")
    @Positive
    private Long greenhouseId;

    @QueryParam("expiringInDays")
    @Positive
    private Integer expiringInDays;

    @QueryParam("lastIndex")
    @Positive
    private Long lastId;

    public Optional<Long> getGreenhouseFilter() {
        return Optional.ofNullable(greenhouseId);
    }

    // Số ngày cảnh báo hết hạn phải dương; số âm hoặc 0 được coi như không lọc,
    // nên khoảng fromDate..toDate luôn hợp lệ.
    public Optional<Integer> getExpiringFilter() {
        return Optional.ofNullable(expiringInDays).filter(value -> value > 0);
    }

    public Optional<Long> getLastIdFilter() {
        return Optional.ofNullable(lastId).filter(value -> value > 0);
    }
}
