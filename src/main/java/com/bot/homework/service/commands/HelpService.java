package com.bot.homework.service.commands;

import com.bot.homework.service.utils.MessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class HelpService {
    private final MessageSender sender;

    public HelpService(@Lazy MessageSender sender) {
        this.sender = sender;
    }

    public void handleTeacherMessage(Long chatId) {
        String text = """
                /help — список комманд
                
                /edit_personal_info — изменить информацию о себе
                
                /create_group — создать группу
                
                /add_pupil_to_group — добавить ученика в группу
                
                /remove_pupil_from_group — удалить ученика из группы
                
                /show_join_requests — заявки на вступление в ваши группы
                
                /set_homework — задать ДЗ
                
                """;
        this.sender.sendMessage(chatId, text);
    }

    public void handlePupilMessage(Long chatId) {
        String text = """
                /help — список комманд
                
                /edit_personal_info — изменить информацию о себе
                
                /join_group — вступить в учебную группу
                
                /homework — посмотреть все дз
                
                """;
        this.sender.sendMessage(chatId, text);
    }


}
