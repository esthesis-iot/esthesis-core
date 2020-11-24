package esthesis.platform.backend.server.mapper;

import esthesis.platform.backend.server.dto.CertificateDTO;
import esthesis.platform.backend.server.model.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CertificateMapper extends BaseMapper<CertificateDTO, Certificate> {

}
