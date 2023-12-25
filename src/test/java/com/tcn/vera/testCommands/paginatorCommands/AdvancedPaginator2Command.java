package com.tcn.vera.testCommands.paginatorCommands;

import com.tcn.vera.commands.templates.SlashCommandTemplate;
import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.pagination.AdvancedEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class AdvancedPaginator2Command extends SlashCommandTemplate {
    private final ButtonHandler handler;

    public AdvancedPaginator2Command(ButtonHandler handler){
        this.commandName = "ap2c";
        this.help = "This command is a test for an advanced paginator command that has a nested submenu";
        this.handler = handler;
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        new AdvancedEmbedPaginator.Builder()
                .setEvent(event)
                .addPageData("Hey! This is an embed.,And this is a very cool description!,#577e4f")
                .addPageData("WOAH! Is this another embed?,AND another cool description? I can't believe it...,#7869dc")
                .addPageData("Ok... This is the last one,This must be the last description then,#f78181")
                .setButtonHandler(handler)
                .setUserID(event.getUser().getIdLong())
                .setEmbedConsumer(AdvancedPaginator2Command::embedGenerator)
                .setEventSelectConsumer(AdvancedPaginator2Command::submenuGenerator)
                .build().paginate();

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
        paginator.getSlashCommandEvent().getHook().editOriginal("This is the secondary menu!").queue();

        AdvancedEmbedPaginator paginator2 = new AdvancedEmbedPaginator.Builder()
                .setEvent(paginator.getSlashCommandEvent())
                .addPageData("Selected paginator 1,And this is a very cool description!,#577e4f")
                .addPageData("Selected paginator 2,AND another cool description? I can't believe it...,#7869dc")
                .addPageData("Selected paginator 3,This must be the last description then,#f78181")
                .setButtonHandler(paginator.getbuttonHandler())
                .setEmbedConsumer(AdvancedPaginator2Command::embedGenerator)
                .setUserID(paginator.getUserID())
                .build();

        paginator2.paginate();

    }
}
