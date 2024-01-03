package ru.netology.patient.service.medical;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

class MedicalServiceImplTest {

    private PatientInfo patient1() {
        BloodPressure blood = new BloodPressure(120, 80);
        HealthInfo health = new HealthInfo(new BigDecimal("36.65"), blood);
        PatientInfo patientInfo = new PatientInfo("5e096c87-0fa9-4f71-bc62-cf52ebb76018", "Иван", "Петров", LocalDate.of(1980, 11, 26), health);
        return patientInfo;
    }

    private PatientInfoRepository patientInfoRepository() {
        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoRepository.getById(patient1().getId())).thenReturn(patient1());
        return patientInfoRepository;
    }

    private String message() {
        return String.format("Warning, patient with id: %s, need help", patient1().getId());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "60, 120, 1",
            "120, 80, 0"
    })
    void checkBloodPressureParameterized(Integer high, Integer low, Integer numberOfInvocations) {
        BloodPressure currentPressure = new BloodPressure(high, low);
        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository(), alertService);

        medicalService.checkBloodPressure(patient1().getId(), currentPressure);
        Mockito.verify(alertService, Mockito.times(numberOfInvocations)).send(message());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "38, 0",
            "33, 1"
    })
    void checkTemperatureParameterized(String temperature, Integer numberOfInvocations) {
        BigDecimal currentTemperature = new BigDecimal(temperature);
        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository(), alertService);

        medicalService.checkTemperature(patient1().getId(), currentTemperature);
        Mockito.verify(alertService, Mockito.times(numberOfInvocations)).send(message());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "120, 80, 38, 0, 0",
            "120, 80, 40, 0, 0",
            "120, 80, 33, 0, 1",
            "60, 120, 38, 1, 0"
    })
    void checkNormalPatientInfoParameterized(Integer high, Integer low, String temperature, Integer pressureInvocations, Integer temperatureInvocations) {
        checkBloodPressureParameterized(high, low, pressureInvocations);
        checkTemperatureParameterized(temperature, temperatureInvocations);

//        BloodPressure currentPressure = new BloodPressure(high, low);
//        BigDecimal currentTemperature = new BigDecimal(temperature);
//
//        SendAlertService alertService = Mockito.mock(SendAlertServiceImpl.class);
//        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository(), alertService);
//
//        medicalService.checkBloodPressure(patient1().getId(), currentPressure);
//        medicalService.checkTemperature(patient1().getId(), currentTemperature);
//        Mockito.verify(alertService, Mockito.times(pressureInvocations + temperatureInvocations)).send(message());
    }
}