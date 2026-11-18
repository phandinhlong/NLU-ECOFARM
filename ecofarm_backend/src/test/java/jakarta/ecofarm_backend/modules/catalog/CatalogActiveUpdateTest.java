package jakarta.ecofarm_backend.modules.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CertificateRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.infrastructure.service.CertificateService;
import jakarta.ecofarm_backend.infrastructure.service.GreenhouseService;
import jakarta.ecofarm_backend.infrastructure.service.VarietyService;
import jakarta.ecofarm_backend.infrastructure.service.impl.CertificateServiceImpl;
import jakarta.ecofarm_backend.infrastructure.service.impl.GreenhouseServiceImpl;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyRequest;
import jakarta.ecofarm_backend.modules.catalog.facade.impl.CertificateFacadeImpl;
import jakarta.ecofarm_backend.modules.catalog.facade.impl.GreenhouseFacadeImpl;
import jakarta.ecofarm_backend.modules.catalog.facade.impl.VarietyFacadeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CatalogActiveUpdateTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Greenhouse greenhouse = Greenhouse.builder()
            .id(1L).code("GH-1").name("Greenhouse").status("ACTIVE").version(3L).build();
    private final Variety variety = Variety.builder()
            .id(2L).code("VAR-1").name("Variety").version(3L).build();
    private final Certificate certificate = Certificate.builder()
            .id(4L).greenhouse(greenhouse).certType("VIETGAP").certNo("CERT-1")
            .validFrom(LocalDate.of(2026, 1, 1)).validTo(LocalDate.of(2027, 1, 1))
            .fileKey("certificate.pdf").version(3L).build();
    private final GreenhouseFacadeImpl greenhouseFacade = new GreenhouseFacadeImpl();
    private final VarietyFacadeImpl varietyFacade = new VarietyFacadeImpl();
    private final CertificateFacadeImpl certificateFacade = new CertificateFacadeImpl();
    private boolean duplicateCertificate;
    private int saves;

    CatalogActiveUpdateTest() throws Exception {
        Batch batch = Batch.builder().greenhouse(greenhouse).variety(variety).build();
        greenhouse.getBatches().add(batch);
        greenhouse.getCertificates().add(certificate);
        variety.getBatches().add(batch);
        GreenhouseRepository greenhouseRepository = proxy(GreenhouseRepository.class, (p, method, args) -> switch (method.getName()) {
            case "findById" -> greenhouse.getId().equals(args[0]) ? greenhouse : null;
            case "existsByCode", "existsByCodeAndIdNot" -> false;
            case "countBatches", "countCertificates" -> 1L;
            case "save" -> { saves++; yield args[0]; }
            default -> throw new AssertionError("Unexpected repository call: " + method.getName());
        });
        VarietyService varietyService = proxy(VarietyService.class, (p, method, args) -> switch (method.getName()) {
            case "findById" -> variety.getId().equals(args[0]) ? variety : null;
            case "existsByCode", "existsByCodeAndIdNot" -> false;
            case "countBatches" -> 1L;
            case "save" -> { saves++; yield args[0]; }
            default -> throw new AssertionError("Unexpected service call: " + method.getName());
        });
        CertificateRepository certificateRepository = proxy(CertificateRepository.class, (p, method, args) -> switch (method.getName()) {
            case "findById" -> certificate.getId().equals(args[0]) ? certificate : null;
            case "existsByCertNo", "existsByCertNoAndIdNot" -> duplicateCertificate;
            case "save" -> { saves++; yield args[0]; }
            default -> throw new AssertionError("Unexpected repository call: " + method.getName());
        });
        GreenhouseServiceImpl greenhouseService = new GreenhouseServiceImpl();
        CertificateServiceImpl certificateService = new CertificateServiceImpl();
        inject(greenhouseService, "greenhouseRepository", greenhouseRepository);
        inject(certificateService, "certificateRepository", certificateRepository);
        inject(greenhouseFacade, "greenhouseService", greenhouseService);
        inject(varietyFacade, "varietyService", varietyService);
        inject(certificateFacade, "certificateService", certificateService);
        inject(certificateFacade, "greenhouseService", greenhouseService);
    }

    @Test
    void catalogFacadesDependOnServicesInsteadOfRepositories() {
        for (Class<?> facade : new Class<?>[]{GreenhouseFacadeImpl.class, VarietyFacadeImpl.class, CertificateFacadeImpl.class}) {
            for (Field field : facade.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.inject.Inject.class)) {
                    assertTrue(field.getType() == GreenhouseService.class
                            || field.getType() == VarietyService.class
                            || field.getType() == CertificateService.class,
                            facade.getSimpleName() + "." + field.getName() + " must inject a service");
                }
            }
        }
    }

    @Test
    void falseSoftDeletesAllThreeWithoutRemovingReferences() throws Exception {
        assertFalse(greenhouseFacade.updateGreenhouse(1L, greenhouseRequest(false)).getData().getActive());
        assertFalse(varietyFacade.updateVariety(2L, varietyRequest(false)).getData().getActive());
        assertFalse(certificateFacade.updateCertificate(4L, certificateRequest(false)).getData().getActive());
        assertFalse(greenhouse.isActive());
        assertFalse(variety.isActive());
        assertFalse(certificate.isActive());
        assertSame(greenhouse, certificate.getGreenhouse());
        assertSame(greenhouse, variety.getBatches().get(0).getGreenhouse());
        assertEquals(1, greenhouse.getBatches().size());
        assertEquals(1, greenhouse.getCertificates().size());
        assertEquals("ACTIVE", greenhouse.getStatus());
        assertEquals(3, saves);
    }

    @Test
    void trueCanReactivateAllThree() throws Exception {
        greenhouse.setActive(false);
        variety.setActive(false);
        certificate.setActive(false);
        assertTrue(greenhouseFacade.updateGreenhouse(1L, greenhouseRequest(true)).getData().getActive());
        assertTrue(varietyFacade.updateVariety(2L, varietyRequest(true)).getData().getActive());
        assertTrue(certificateFacade.updateCertificate(4L, certificateRequest(true)).getData().getActive());
    }

    @Test
    void omittedActivePreservesExistingState() throws Exception {
        greenhouse.setActive(false);
        variety.setActive(false);
        certificate.setActive(false);
        assertFalse(greenhouseFacade.updateGreenhouse(1L, greenhouseRequest(null)).getData().getActive());
        assertFalse(varietyFacade.updateVariety(2L, varietyRequest(null)).getData().getActive());
        assertFalse(certificateFacade.updateCertificate(4L, certificateRequest(null)).getData().getActive());
    }

    @Test
    void createDefaultsToActive() throws Exception {
        assertTrue(greenhouseFacade.createGreenhouse(greenhouseRequest(null)).getData().getActive());
        assertTrue(varietyFacade.createVariety(varietyRequest(null)).getData().getActive());
        assertTrue(certificateFacade.createCertificate(certificateRequest(null)).getData().getActive());
    }

    @Test
    void staleVersionsCannotChangeActive() throws Exception {
        GreenhouseRequest gh = greenhouseRequest(false);
        VarietyRequest va = varietyRequest(false);
        CertificateRequest cert = certificateRequest(false);
        inject(gh, "version", 2L);
        inject(va, "version", 2L);
        inject(cert, "version", 2L);
        assertEquals(ErrorCode.VERSION_CONFLICT.getCode(),
                assertThrows(ConflictException.class, () -> greenhouseFacade.updateGreenhouse(1L, gh)).getCode());
        assertEquals(ErrorCode.VERSION_CONFLICT.getCode(),
                assertThrows(ConflictException.class, () -> varietyFacade.updateVariety(2L, va)).getCode());
        assertEquals(ErrorCode.VERSION_CONFLICT.getCode(),
                assertThrows(ConflictException.class, () -> certificateFacade.updateCertificate(4L, cert)).getCode());
        assertTrue(greenhouse.isActive());
        assertTrue(variety.isActive());
        assertTrue(certificate.isActive());
        assertEquals(0, saves);
    }

    @Test
    void missingCertificateIsNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> certificateFacade.updateCertificate(99L, certificateRequest(false)));
        assertEquals(0, saves);
    }

    @Test
    void certificateRejectsDuplicateNumberAndInvalidDates() throws Exception {
        CertificateRequest request = certificateRequest(false);
        duplicateCertificate = true;
        assertEquals(ErrorCode.CERT_NO_EXISTS.getCode(),
                assertThrows(ConflictException.class, () -> certificateFacade.updateCertificate(4L, request)).getCode());
        duplicateCertificate = false;
        inject(request, "validTo", "2025-01-01");
        assertEquals(ErrorCode.INVALID_DATE.getCode(),
                assertThrows(ConflictException.class, () -> certificateFacade.updateCertificate(4L, request)).getCode());
        assertTrue(certificate.isActive());
        assertEquals(0, saves);
    }

    @Test
    void certificateCanBeEditedWhenItsGreenhouseIsSoftDeleted() throws Exception {
        greenhouse.setActive(false);
        CertificateRequest request = certificateRequest(false);
        inject(request, "certNo", " CERT-EDITED ");
        var response = certificateFacade.updateCertificate(4L, request).getData();
        assertEquals("CERT-EDITED", response.getCertNo());
        assertEquals("certificate.pdf", response.getFileKey());
        assertEquals(1L, response.getGreenhouseId());
        assertFalse(response.getActive());
    }

    private GreenhouseRequest greenhouseRequest(Boolean active) throws Exception {
        return mapper.readValue("{\"code\":\"GH-1\",\"name\":\"Greenhouse\",\"status\":\"ACTIVE\",\"version\":3"
                + activeField(active) + "}", GreenhouseRequest.class);
    }

    private VarietyRequest varietyRequest(Boolean active) throws Exception {
        return mapper.readValue("{\"code\":\"VAR-1\",\"name\":\"Variety\",\"version\":3"
                + activeField(active) + "}", VarietyRequest.class);
    }

    private CertificateRequest certificateRequest(Boolean active) throws Exception {
        return mapper.readValue("{\"greenhouseId\":1,\"certType\":\"VIETGAP\",\"certNo\":\"CERT-1\","
                + "\"validFrom\":\"2026-01-01\",\"validTo\":\"2027-01-01\",\"fileKey\":\"certificate.pdf\",\"version\":3"
                + activeField(active) + "}", CertificateRequest.class);
    }

    private String activeField(Boolean active) {
        return active == null ? "" : ",\"active\":" + active;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
