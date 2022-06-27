package jp.test.servercreate.api.data.repo;

import jp.test.servercreate.api.data.entity.ServerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ServerRepository  extends MongoRepository<ServerEntity, String> {
    @Query("{owner:?0}")
    List<ServerEntity> findServersByOwner(Long owner);

    @Query("{port:?0}")
    ServerEntity findServerByPort(Integer port);
}
