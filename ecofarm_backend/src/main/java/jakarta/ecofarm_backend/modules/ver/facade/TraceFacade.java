package jakarta.ecofarm_backend.modules.ver.facade;

import jakarta.ecofarm_backend.modules.ver.dto.TraceResponse;

public interface TraceFacade {

    TraceResponse getTrace(String code);
}
