package com.bot.homework.service.commands;

import com.bot.homework.model.user.UserRole;
import org.hibernate.dialect.unique.CreateTableUniqueDelegate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;

import java.util.List;

@Service
public class CommandService {

    public SetMyCommands buildCommands(Long chatId, UserRole role) {
        List<BotCommand> commands = switch (role) {
            case TEACHER -> teacherCommands();
            case PUPIL -> pupilCommands();
            case NOT_REGISTERED -> List.of(
                    new BotCommand("/start", "начать"),
                    new BotCommand("/register", "регистрация")
            );
        };

        return new SetMyCommands(
                commands,
                new BotCommandScopeChat(chatId.toString()),
                null
        );
    }

    private List<BotCommand> teacherCommands() {
        return List.of(
                new BotCommand("/help", "все команды"),
                new BotCommand("/edit_personal_info", "изменить информацию"),
                new BotCommand("/create_group", "создать группу"),
                new BotCommand("/add_pupil_to_group", "добавить ученика"),
                new BotCommand("/remove_pupil_from_group", "удалить ученика из группы"),
                new BotCommand("/show_join_requests", "заявки"),
                new BotCommand("/set_homework", "задать ДЗ")
        );
    }

    private List<BotCommand> pupilCommands() {
        return List.of(
                new BotCommand("/help", "все команды"),
                new BotCommand("/edit_personal_info", "изменить информацию"),
                new BotCommand("/join_group", "вступить в группу"),
                new BotCommand("/homework", "посмотреть ДЗ")
        );
    }
}
