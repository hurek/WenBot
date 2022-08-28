package com.hurek.wenbot.listeners;

import com.hurek.wenbot.WenBotProperties;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WenListener implements EventListener<MessageCreateEvent> {
    WenBotProperties properties;
    Random random;
    static String ANSWERS_DIR = "src/main/resources/wen-answers";

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
                .flatMap(this::validateMessage)
                .switchIfEmpty(Mono.empty())
                .flatMap(this::reply)
                .then();
    }

    @Override
    public boolean resolveCommand(MessageCreateEvent event) {
        return possiblePatterns().anyMatch(pattern -> event.getMessage().getContent().toLowerCase().equals(pattern));
    }

    private Mono<Message> reply(Message message) {
        try {
            File[] files = new File(ANSWERS_DIR).listFiles();

            if (files == null) {
                log.error("Files not found");
                return Mono.empty();
            }

            List<String> fileNames = Stream.of(files)
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());

            File file = new File(ANSWERS_DIR + "/" + fileNames.get(random.nextInt(fileNames.size())));
            InputStream inputStream = new FileInputStream(file);

            return message.getChannel().flatMap(channel -> channel.createMessage(spec -> spec.addFile(file.getName(), inputStream)));
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage(), e.getCause());
            return Mono.empty();
        }
    }

    private Mono<Message> validateMessage(Message message) {
        return Mono.just(message)
                .filter(msg -> !msg.getContent().isEmpty())
                .filterWhen(msg -> msg.getChannel().map(channel -> channel instanceof TextChannel))
                .filter(this::validateChannelId)
                .filterWhen(msg -> Mono.justOrEmpty(msg.getAuthor().map(author -> !author.isBot())));
    }

    private boolean validateChannelId(Message message) {
        return Stream.of(properties.getDevChannelId(), properties.getProdChannelId())
                .anyMatch(id -> message.getChannelId().asString().equals(id));
    }

    private Stream<String> possiblePatterns() {
        return Stream.of(
                "when",
                "wen",
                "!wen",
                "!when",
                ".wen",
                ".when"
        );
    }
}
