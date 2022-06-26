package jp.cron.mdcontroller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.bot.MainBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;

@SpringBootApplication
public class MdControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdControllerApplication.class, args);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    ServerRepository serverRepository;

    @Bean
    public DockerClient dockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
//        return DockerClientBuilder.getInstance().build();
    }

    @PostConstruct
    @Autowired
    public void init() {
        UserEntity owner = userRepository.findById(MainBot.OWNER_ID).orElse(null);
        if (owner==null) {
            owner = new UserEntity();
            owner.id = MainBot.OWNER_ID;
        }
        owner.server_limit = 1000;
        for (UserEntity.Permission perm : UserEntity.Permission.values()) {
            if (!owner.permissions.contains(perm))
                owner.permissions.add(perm);
        }
        userRepository.save(owner);
    }

}
