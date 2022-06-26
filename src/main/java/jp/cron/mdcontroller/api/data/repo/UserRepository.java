package jp.cron.mdcontroller.api.data.repo;

import jp.cron.mdcontroller.api.data.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

public interface UserRepository extends MongoRepository<UserEntity, Long> {
}