package jp.cron.mdcontroller.api.data.entity;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerEntity {
    @Id
    public String id;
    public Long owner;

    public String containerId;

    @Indexed(unique = true)
    public Integer port;

    public List<String> plugins = new ArrayList<>();

    public String version;
    public ServerType serverType;

    public ServerEntity() {
    }


    public void setupPort(ServerRepository serverRepository) {
        port = 49152;
        System.out.println(serverRepository);
        System.out.println(serverRepository.findServerByPort(port));
        while (serverRepository.findServerByPort(port) != null) {
            port++;
        }
    }

    public void createContainer(DockerClient dockerClient) {
        List<String> env = new ArrayList<>();
        env.add("EULA=TRUE");
        env.add("VERSION="+this.version);
        env.add("TYPE="+this.serverType.toString());
        env.add("MODS="+String.join(",", plugins));

        CreateContainerResponse res = dockerClient.createContainerCmd("itzg/minecraft-server")
                .withPortBindings(PortBinding.parse(this.port+":25565"))
                .withEnv(env)
                .withBinds(new Bind(Paths.get("./data/"+this.id).toAbsolutePath().toString(), new Volume("/data")))
                .withName(this.id)
                .exec();

        this.containerId = res.getId();
    }

    public enum ServerType {
        FORGE,
        FABRIC,
        QUILT,
        BUKKIT,
        SPIGOT,
        PAPER,
        AIRPLANE,
        PUFFERFISH,
        PURPUR,
        MAGMA,
        MOHIST,
        CATSERVER,
        LOLISERVER,
        CANYON,
        SPONGEVANILLA,
        LIMBO,
        CRUCIBLE
    }
}
