package esthesis.backend.mapper;

import esthesis.backend.dto.CertificateDTO;
import esthesis.backend.model.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CertificateMapper extends BaseMapper<CertificateDTO, Certificate> {

}
