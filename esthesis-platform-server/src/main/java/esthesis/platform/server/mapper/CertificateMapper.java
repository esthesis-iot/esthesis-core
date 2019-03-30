package esthesis.platform.server.mapper;

import esthesis.platform.server.dto.CertificateDTO;
import esthesis.platform.server.model.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CertificateMapper extends BaseMapper<CertificateDTO, Certificate> {

}
