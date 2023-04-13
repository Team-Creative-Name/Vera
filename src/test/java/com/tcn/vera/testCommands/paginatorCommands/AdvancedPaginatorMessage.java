package com.tcn.vera.testCommands.paginatorCommands;

import com.tcn.vera.commands.templates.ChatCommandTemplate;
import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.pagination.AdvancedEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class AdvancedPaginatorMessage extends ChatCommandTemplate {

    private final ButtonHandler buttonHandler;

    public AdvancedPaginatorMessage(ButtonHandler buttonHandler) {
        this.commandName = "advancedPaginatorMessage";
        this.buttonHandler = buttonHandler;
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
                .setEmbedConsumer(AdvancedPaginatorMessage::embedGenerator)
                .setMessageSelectConsumer(AdvancedPaginatorMessage::submenuGenerator)
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

    private static void submenuGenerator(Message message, Object pageData){
        //It is important to know that the event response already has your paginator page in it.
        // Be sure to remove any content or buttons you don't want.
        String pageTitle = ((String) pageData).split(",")[0];
        message.editMessage("You selected the page with the title: \"" + pageTitle + "\"" ).queue();
    }
}
