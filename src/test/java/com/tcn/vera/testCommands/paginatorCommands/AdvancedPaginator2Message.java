package com.tcn.vera.testCommands.paginatorCommands;

import com.tcn.vera.commands.templates.ChatCommandTemplate;
import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.pagination.AdvancedEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class AdvancedPaginator2Message extends ChatCommandTemplate {

    private static ButtonHandler buttonHandler;

    public AdvancedPaginator2Message(ButtonHandler buttonHandler) {
        this.commandName = "ap2m";
        AdvancedPaginator2Message.buttonHandler = buttonHandler;
    }

    @Override
    public void executeChatCommand(MessageReceivedEvent event, Message message, String messageContent) {
        event.getChannel().sendMessage("test").queue();

        AdvancedEmbedPaginator paginator = new AdvancedEmbedPaginator.Builder()
                .setEvent(event)
                .addPageData("Hey! This is an embed.,And this is a very cool description!,#577e4f")
                .addPageData("WOAH! Is this another embed?,AND another cool description? I can't believe it...,#7869dc")
                .addPageData("Ok... This is the last one,This must be the last description then,#f78181")
                .setButtonHandler(buttonHandler)
                .setUserID(event.getAuthor().getIdLong())
                .setEmbedConsumer(AdvancedPaginator2Message::embedGenerator)
                .setMessageSelectConsumer(AdvancedPaginator2Message::submenuGenerator)
                .build();

        paginator.paginate();
    }


    private static void embedGenerator(EmbedBuilder embedBuilder, Object pageData) {
        /* The pageData is the object you added to the pageDataList in the builder. In this case it is a string with the
         * title, description, and color of the embed seperated by a comma, but you can make it whatever you want.
         *
         * Remember to cast the pageData to whatever type you want it to be. (and make sure it is the correct type)
         */
        String[] splitData = ((String) pageData).split(",");
        embedBuilder.setTitle(splitData[0]);
        embedBuilder.setDescription(splitData[1]);
        embedBuilder.setColor(Color.decode(splitData[2]));
    }

    private static void submenuGenerator(AdvancedEmbedPaginator paginator, Object pageData){
        paginator.getSentMessage().editMessage("This is the secondary menu!").queue();

        AdvancedEmbedPaginator paginator2 = new AdvancedEmbedPaginator.Builder()
                .setSentMessage(paginator.getSentMessage())
                .addPageData("Selected paginator 1,And this is a very cool description!,#577e4f")
                .addPageData("Selected paginator 2,AND another cool description? I can't believe it...,#7869dc")
                .addPageData("Selected paginator 3,This must be the last description then,#f78181")
                .setButtonHandler(buttonHandler)
                .setUserID(paginator.getUserID())
                .setEmbedConsumer(AdvancedPaginator2Message::embedGenerator)
                .build();

        paginator2.paginate();
    }
}
