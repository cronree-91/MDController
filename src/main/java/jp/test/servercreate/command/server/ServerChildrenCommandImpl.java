package jp.test.servercreate.command.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import jp.test.servercreate.api.data.entity.UserEntity;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.command.ChildrenCommandImpl;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public abstract class ServerChildrenCommandImpl extends ChildrenCommandImpl {
    protected DockerClient dockerClient;
    public ServerChildrenCommandImpl(DockerClient dockerClient, UserRepository userRepository, String name, String help, UserEntity.Permission permission) {
        super(userRepository, name, help, permission);
        this.dockerClient = dockerClient;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity user, InteractionHook hook) {
        List<Image> images = dockerClient.listImagesCmd().exec();
        Image image = images.stream().filter(i -> String.join("",i.getRepoTags()).equals("itzg/minecraft-server:latest")).findFirst().orElse(null);
        if (image == null){
            hook.sendMessage("Minecrat Serverがインストールされていません。\nインストールの準備をしています...").complete();
            final int[] step = {1};
            dockerClient.pullImageCmd("itzg/minecraft-server")
                    .withTag("latest")
                    .exec(new ResultCallback<PullResponseItem>() {
                        @Override
                        public void onStart(Closeable closeable) {
                            hook.editOriginal("インストールを開始します...").complete();
                        }

                        @Override
                        public void onNext(PullResponseItem object) {
                            hook.editOriginal("ステップ"+ step[0] +"を処理中...\n"+object.getStatus()+"\n"+object.getStream()).complete();
                            step[0]++;
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            hook.editOriginal(":x: エラーが発生しました。\n\n"+throwable.getClass().getCanonicalName()+"\n"+throwable.getMessage()).complete();
                        }

                        @Override
                        public void onComplete() {
                            hook.editOriginal(":o: インストールが完了しました。").complete();
                        }

                        @Override
                        public void close() throws IOException {
                        }
                    });
            return;
        }
        invoke(event, user, hook);
    }

    protected abstract void invoke(SlashCommandEvent event, UserEntity user, InteractionHook hook);
}
