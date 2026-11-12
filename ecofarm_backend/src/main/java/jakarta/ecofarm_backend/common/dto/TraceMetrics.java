package jakarta.ecofarm_backend.common.dto;

import java.math.BigDecimal;

public class TraceMetrics {
    BigDecimal brix;
    BigDecimal avgWeightKg;

    public TraceMetrics() {}

    public BigDecimal getBrix() { return brix; }
    public void setBrix(BigDecimal brix) { this.brix = brix; }
    public BigDecimal getAvgWeightKg() { return avgWeightKg; }
    public void setAvgWeightKg(BigDecimal avgWeightKg) { this.avgWeightKg = avgWeightKg; }
}
