/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2023 Thomas Wessel and the rest of Team Creative Name
 *
 *
 * This library is licensed under the GNU Lesser General Public License v2.1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 *
 * For more information, please check out the original repository of this project on github
 * https://github.com/Team-Creative-Name/Vera
 */
package com.tcn.vera.testCommands.slashCommands;

import com.tcn.vera.commands.SlashCommandTemplate;
import com.tcn.vera.interactions.AutoCompleteInterface;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutocompleteSlashCommand extends SlashCommandTemplate implements AutoCompleteInterface {

    public AutocompleteSlashCommand(){
        this.commandName = "autocomplete-test";
        this.help = "Test out the autocomplete!";
        this.slashCommand = Commands.slash(getCommandName(), "Whats your favorite word from the list?")
                .addOption(OptionType.STRING, "word", "favorite word", true, true);
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
       event.reply("You chose " + event.getOptions().get(event.getOptions().size() - 1).getAsString()).queue();
    }

    @Override
    public void executeAutocomplete(CommandAutoCompleteInteractionEvent event) {
        String[] words = new String[]{"bowl", "concern", "least", "river", "relative", "bedroom", "quarter", "luck", "army",
                "cheat", "commerce", "revenge", "realize", "pull", "official", "pattern", "film", "needle", "memory", "salary",
                "should", "opinion", "can", "copy", "tide"};

        //This could represent any number of autocompleted sections. It is important to check which field you're autocompleting
        if(event.getFocusedOption().getName().equalsIgnoreCase("word")){
            List<Command.Choice> options = Stream.of(words)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            System.out.println("test");
            event.replyChoices(options).queue();
        }
    }
}
