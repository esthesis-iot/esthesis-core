package esthesis.platform.server.service;

import esthesis.platform.server.dto.StoreDTO;
import esthesis.platform.server.model.Store;
import esthesis.platform.server.repository.CertificateRepository;
import esthesis.platform.server.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class StoreService extends BaseService<StoreDTO, Store> {

  private final StoreRepository storeRepository;
  private final CertificateRepository certificateRepository;

  public StoreService(StoreRepository storeRepository,
    CertificateRepository certificateRepository) {
    this.storeRepository = storeRepository;
    this.certificateRepository = certificateRepository;
  }

  public void test() {
    //    Store store = new Store();
    //    store.setName("test1");
    //    storeRepository.save(store);

    //    final Store store = storeRepository.findById(1l).get();
    //    final Certificate certificate = certificateRepository.findById(3l).get();
    //    store.setCertificates(ImmutableSet.of(certificate));
    //    storeRepository.save(store);

//        final Store store = storeRepository.findById(1l).get();
//        final Certificate certificate = certificateRepository.findById(6l).get();
//        store.getCertificates().add(certificate);
//        storeRepository.save(store);

//    certificateRepository.deleteById(6l);

  }
}
