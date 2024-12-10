package esthesis.service.command.impl.service;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
class CommandRequestServiceTest {

    @Inject
    CommandRequestService commandRequestService;

    @Inject
    TestHelper testHelper;

    int initialEntitiesSize = 0;

    @BeforeEach
    void setUp() {
        testHelper.clearDatabase();
        testHelper.createMultipleCommandRequestEntities();
        initialEntitiesSize = testHelper.findAllCommandRequestEntities().size();
        log.info("Initial entities size: {}", initialEntitiesSize);
    }

    @Test
    void purge() {
        commandRequestService.purge(7);
        assertEquals(initialEntitiesSize, testHelper.findAllCommandRequestEntities().size());

        commandRequestService.purge(5);
        assertEquals(initialEntitiesSize - 3, testHelper.findAllCommandRequestEntities().size());


        commandRequestService.purge(5);
        assertEquals(initialEntitiesSize - 3, testHelper.findAllCommandRequestEntities().size());

        commandRequestService.purge(3);
        assertEquals(initialEntitiesSize - 6, testHelper.findAllCommandRequestEntities().size());

        commandRequestService.purge(1);
        assertEquals(initialEntitiesSize - 9, testHelper.findAllCommandRequestEntities().size());

        commandRequestService.purge(0);
        assertEquals(0, testHelper.findAllCommandRequestEntities().size());

    }

    @Test
    void save() {
        CommandRequestEntity newCommandRequest =
                testHelper.makeCommandRequestEntity(
                                "hardware-id-1",
                                null,
                                CommandType.e,
                                ExecutionType.a
                        ).setCreatedOn(Instant.now())
                        .setDispatchedOn(null);

        commandRequestService.save(newCommandRequest);

        assertEquals(initialEntitiesSize + 1, testHelper.findAllCommandRequestEntities().size());
    }

    @Test
    void findById() {
        String commandId = testHelper.findOneCommandRequestEntity().getId().toString();
        CommandRequestEntity commandRequest = commandRequestService.findById(commandId);
        assertNotNull(commandRequest);
    }

    @Test
    void find() {
        List<CommandRequestEntity> results =
                commandRequestService.find(
                                testHelper.makePageable(0, 10),
                                false)
                        .getContent();

        assertEquals(10, results.size());
    }

    @Test
    void deleteById() {
        String commandId = testHelper.findOneCommandRequestEntity().getId().toString();
        commandRequestService.deleteById(commandId);
        assertEquals(initialEntitiesSize - 1, testHelper.findAllCommandRequestEntities().size());
    }
}
