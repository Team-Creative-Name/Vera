package com.tcn.vera.testCommands.paginatorCommands;

import com.tcn.vera.commands.templates.SlashCommandTemplate;
import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.pagination.AdvancedEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;

public class AdvancedPaginatorCommand extends SlashCommandTemplate {
    private final ButtonHandler handler;

    public AdvancedPaginatorCommand(ButtonHandler handler){
        this.handler = handler;
        this.commandName = "advanced_paginator_test";
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        ArrayList<String> pageDataList = new ArrayList<>();
        pageDataList.add("This was passed as the first element in an arraylist,List description #1,#577e4f");
        pageDataList.add("This was passed as the second element in an arraylist,List description #2,#7869dc");
        pageDataList.add("This was passed as the third element in an arraylist,List description #3,#f78181");

        AdvancedEmbedPaginator paginator = new AdvancedEmbedPaginator.Builder()
                .setEvent(event)
                .addPageDataList(pageDataList)
                .addPageData("Hey! This is an embed.,And this is a very cool description!,#577e4f")
                .addPageData("WOAH! Is this another embed?,AND another cool description? I can't believe it...,#7869dc")
                .addEmbed(new EmbedBuilder().setTitle("This was passed as a completed embed!").setDescription("This is a cool description!").setColor(Color.decode("#f78181")).build())
                .addPageData("Ok... This is the second to last one,This must be the second to last description then,#f78181")
                .addEmbed(new EmbedBuilder().setTitle("This was passed as a completed embed too!").setDescription("This is a cool description!").setColor(Color.decode("#f78181")).build())
                .setButtonHandler(handler)
                .setUserID(event.getUser().getIdLong())
                .setEmbedConsumer(AdvancedPaginatorCommand::embedGenerator)
                .setEventSelectConsumer(AdvancedPaginatorCommand::submenuGenerator)
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

    private static void submenuGenerator(SlashCommandInteractionEvent event, Object pageData){
        //It is important to know that the event response already has your paginator page in it.
        // Be sure to remove any content or buttons you don't want.
        if(pageData instanceof MessageEmbed){
            event.getHook().editOriginal("You selected an embed" ).queue();
        } else if (pageData instanceof String) {
            event.getHook().editOriginal("You selected a pageData" ).queue();
        }

    }
}
