package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import com.bot.homework.model.Subject;
import com.bot.homework.model.user.UserRole;
import com.bot.homework.service.commands.*;
import com.bot.homework.service.utils.MessageSender;
import com.bot.homework.service.utils.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MainService extends TelegramLongPollingBot implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private final BotConfig config;
    private final StartService startService;
    private final RegistrationService registrationService;
    private final EditPersonalInfoService editService;
    private final HelpService helpService;
    private final UserRoleService userRoleService;
    private final GroupService groupService;
    private final HomeworkService homeworkService;
    private final CommandService commandService;



    public MainService(
            BotConfig config,
            StartService startService,
            RegistrationService registrationService,
            EditPersonalInfoService editService,
            HelpService helpService,
            UserRoleService userRoleService,
            GroupService groupService,
            HomeworkService homeworkService,
            CommandService commandService
    ) {
        this.config = config;
        this.startService = startService;
        this.registrationService = registrationService;
        this.editService = editService;
        this.helpService = helpService;
        this.userRoleService = userRoleService;
        this.groupService = groupService;
        this.homeworkService = homeworkService;
        this.commandService = commandService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            Long telegramId = msg.getFrom().getId();
            Long chatId = msg.getChatId();
            String text = msg.getText();
            UserRole role = this.userRoleService.defineUserRoleByTelegramId(telegramId);
            updateUserCommands(chatId, role);

            if (role == UserRole.NOT_REGISTERED) {
                switch (text) {
                    case "/start" -> this.startService.handleStart(msg);
                    case "/register" -> this.registrationService.startRegistration(telegramId, chatId);
                    default -> {
                        if (this.registrationService.isRegistering(telegramId)) {
                            this.registrationService.handle(msg);
                        } else {
                            sendMessage(chatId, "Сначала зарегистрируйтесь");
                        }
                    }
                }
            } else if (role == UserRole.TEACHER) {
                switch (text) {
                    case "/help" -> this.helpService.handleTeacherMessage(chatId);
                    case "/edit_personal_info" -> this.editService.editPersonalInfo(telegramId, chatId);
                    case "/create_group" -> this.groupService.startGroupCreation(telegramId, chatId);
                    case "/add_pupil_to_group" ->
                            this.groupService.startAddPupilToGroup(telegramId, chatId); // такой себе
                    case "/show_join_requests" -> this.groupService.showJoinGroupRequests(telegramId, chatId);
                    case "/remove_pupil_from_group" -> this.groupService.showAllGroupsByTeacherId(telegramId, chatId);
                    case "/set_homework" -> this.homeworkService.askGroupForSettingHomework(telegramId, chatId);
                    default -> {
                        if (this.editService.isEditing(telegramId)) {
                            this.editService.handleEditMessage(msg);
                        } else if (this.groupService.isCreating(telegramId)) {
                            this.groupService.handleGroupCreation(msg);
                        } else if (this.groupService.isAddingPupilToGroup(telegramId)) {
                            this.groupService.handleAddPupilToGroup(msg);
                        } else if (this.homeworkService.isSettingHomework(telegramId)) {
                            this.homeworkService.handleHomeworkInput(telegramId, chatId, text);
                        } else {
                            sendMessage(chatId, "Неизвестная комманда 🤔");
                        }
                    }
                }
            } else if (role == UserRole.PUPIL) {
                switch (text) {
                    case "/help" -> this.helpService.handlePupilMessage(chatId);
                    case "/edit_personal_info" -> this.editService.editPersonalInfo(telegramId, chatId);
                    case "/join_group" -> this.groupService.askGroupToJoinSubject(chatId);
                    case "/homework" -> this.homeworkService.showHomework(telegramId, chatId);
                    default -> {
                        if (this.editService.isEditing(telegramId)) {
                            this.editService.handleEditMessage(msg);
                        } else {
                            sendMessage(chatId, "Неизвестная комманда 🤔");
                        }
                    }
                }
            }
        }

        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            String data = callback.getData();
            Long telegramId = callback.getFrom().getId();
            Message message = (Message) callback.getMessage();
            Long chatId = message.getChatId();
            UserRole role = this.userRoleService.defineUserRoleByTelegramId(telegramId);

            if (role == UserRole.NOT_REGISTERED) {
                switch (data) {
                    case "REGISTER" -> this.registrationService.startRegistration(telegramId, chatId);
                    case "ROLE_TEACHER", "ROLE_PUPIL" ->
                            this.registrationService.handleRoleCallback(telegramId, chatId, data);
                    case "BACK_TO_ROLE", "BACK_TO_FIRSTNAME", "BACK_TO_LASTNAME" ->
                            this.registrationService.handleBackCallback(telegramId, chatId, data);
                    case "EDIT_FIRSTNAME", "EDIT_LASTNAME", "EDIT_PATRONYMIC" ->
                            this.editService.handleEditCallback(telegramId, chatId, data);
                }
            } else if (role == UserRole.TEACHER) {
                if (data.startsWith("APPROVE_")) {
                    Integer requestId = Integer.parseInt(data.replace("APPROVE_", ""));
                    this.groupService.approveRequest(requestId);
                }

                if (data.startsWith("REJECT_")) {
                    Integer requestId = Integer.parseInt(data.replace("REJECT_", ""));
                    this.groupService.rejectRequest(requestId);
                }

                if (data.startsWith("VIEW_PUPILS_IN_GROUP_")) {
                    Integer groupId = Integer.parseInt(data.replace("VIEW_PUPILS_IN_GROUP_", ""));
                    this.groupService.showAllPupilsInGroupByGroupId(groupId, chatId);
                }

                if (data.startsWith("REMOVE_PUPIL_")) {
                    String[] parts = data.split("_");
                    Integer groupId = Integer.parseInt(parts[2]);
                    Long pupilId = Long.parseLong(parts[3]);
                    this.groupService.removePupilFromGroup(groupId, pupilId, telegramId, chatId);
                }

                if (data.startsWith("HOMEWORK_FOR_GROUP_")) {
                    Integer groupId = Integer.parseInt(data.replace("HOMEWORK_FOR_GROUP_", ""));
                    this.homeworkService.askHomeworkTask(telegramId, groupId, chatId);
                }
            } else if (role == UserRole.PUPIL) {
                if (data.startsWith("SUBJECT_")) {
                    Integer subjectId = Integer.parseInt(data.replace("SUBJECT_", ""));
                    Subject subject = this.groupService.getSubjectById(subjectId);
                    this.groupService.showAllGroupsBySubject(subject, chatId);
                }

                if (data.startsWith("JOIN_GROUP_")) {
                    Integer groupId = Integer.parseInt(data.replace("JOIN_GROUP_", ""));
                    this.groupService.createJoinRequest(telegramId, groupId, chatId);
                }
            }

        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Cannot send the message", e);
        }
    }

    private void updateUserCommands(Long chatId, UserRole role) {
        try {
            execute(this.commandService.buildCommands(chatId, role));
        } catch (TelegramApiException e) {
            log.error("Cannot set commands", e);
        }
    }

}
