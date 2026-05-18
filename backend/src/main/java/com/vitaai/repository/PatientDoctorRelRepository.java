package com.vitaai.repository;

import com.vitaai.entity.PatientDoctorRel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientDoctorRelRepository extends JpaRepository<PatientDoctorRel, PatientDoctorRel.PatientDoctorRelId> {
    List<PatientDoctorRel> findByPatientId(Long patientId);
    List<PatientDoctorRel> findByDoctorId(Long doctorId);
    List<PatientDoctorRel> findByDoctorIdAndAuthorization(Long doctorId, PatientDoctorRel.Authorization authorization);
}
