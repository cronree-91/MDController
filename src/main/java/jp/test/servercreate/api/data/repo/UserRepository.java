package jp.test.servercreate.api.data.repo;

import jp.test.servercreate.api.data.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserEntity, Long> {
}