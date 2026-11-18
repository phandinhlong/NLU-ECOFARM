package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.modules.ver.repository.TraceData;

public interface VerTraceRepository {

    TraceData load(String code);
}
