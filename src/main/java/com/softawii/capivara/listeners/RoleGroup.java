package com.softawii.capivara.listeners;

import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@IGroup(name = "role", description = "Role Group", hidden = false)
public class RoleGroup {

    // Specific Action
    public static final String actionButton = "role-action-button";
    public static final String removeAction = "role-remove-action";

    public static final String cleanAction = "role-clean-action";
    // Confirm or Deny Action
    public static final String confirmAction = "role-confirm-action";
    public static final String cancelAction = "role-cancel-action";

    @ICommand(name="create", description = "Comando para criar um novo cargo", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "O nome do cargo a ser criado", type = OptionType.STRING, required = false)
    @IArgument(name="channel", description = "O canal que o cargo será vinculado", type = OptionType.CHANNEL, required = false)
    @IArgument(name="color", description = "A cor do cargo em hexadecimal (#FFFFFF)", type = OptionType.STRING, required = false)
    @IArgument(name="visible", description = "Se o cargo deve ser exibido na lista de cargos separadamente, ali do lado sabe??", type = OptionType.BOOLEAN, required = false)
    @IArgument(name="mentionable", description = "Se o cargo pode ser mencionado, ou seja, @everyone!", type = OptionType.BOOLEAN, required = false)
    public static void create(SlashCommandInteractionEvent event) {
        // Decoding
        String name = event.getOption("name") != null ? event.getOption("name").getAsString() : null;
        GuildChannel channel = event.getOption("channel") != null ? event.getOption("channel").getAsGuildChannel() : null;

        if((name == null) == (channel == null)) {
            MessageEmbed embed = Utils.simpleEmbed("Erro", "Você deve informar apenas o nome do cargo ou apenas o canal de vinculação", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        } else if(channel != null) {
            name = channel.getName();
        }

        // Name Defined
        String finalName = name;
        if(event.getGuild().getRoles().stream().anyMatch(role -> role.getName().equals(finalName))) {
            MessageEmbed embed = Utils.simpleEmbed("Erro", "Já existe um cargo com esse nome", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        };

        // Color
        String strColor = event.getOption("color") != null ? event.getOption("color").getAsString() : null;

        Color color;
        if(strColor != null) {
            try {
                color = Color.decode(strColor);
            } catch (NumberFormatException e) {
                MessageEmbed embed = Utils.simpleEmbed("Erro", "A cor informada não é uma cor válida", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
        } else {
            RandomGenerator random = RandomGenerator.getDefault();
            color = new Color(random.nextInt(0, 255), random.nextInt(0, 255), random.nextInt(0, 255));
        }

        // Visible???
        boolean visible = event.getOption("visible") != null && event.getOption("visible").getAsBoolean();
        boolean mentionable = event.getOption("mentionable") == null || event.getOption("mentionable").getAsBoolean();

        event.getGuild().createRole().setName(finalName).setColor(color).setHoisted(visible).setMentionable(mentionable).queue(role -> {
            MessageEmbed embed = Utils.simpleEmbed("Cargo criado", "O cargo " + role.getAsMention()+ " foi criado com sucesso", Color.GREEN);
            event.replyEmbeds(embed).setEphemeral(true).queue();
        });
    }

    // DELETE SECTION -> DELETE / CONFIRM / CANCEL
    @ICommand(name="delete", description = "Quer deletar um cargo do servidor, amigao?", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="role", description = "O cargo que tu quer deletar!", type = OptionType.ROLE, required = true)
    public static void delete(SlashCommandInteractionEvent event) {
        // Not null, because it's server side and required
        Role role = event.getOption("role").getAsRole();
        String confirmId = String.format("%s:%s:%s", confirmAction, removeAction,  role.getId());
        String cancelId  = cancelAction;

        MessageEmbed messageEmbed = Utils.simpleEmbed("Você tem certeza disso?",
                "Você realmente quer deletar o cargo " + role.getAsMention() + "?",
                Color.ORANGE);

        Button successButton = Button.success(confirmId, "Sim! Pode continuar!");
        Button cancelButton  = Button.danger(cancelId, "Não, Deus me livre!!");

        event.replyEmbeds(messageEmbed).addActionRow(successButton, cancelButton).setEphemeral(true).queue();
    }

    @ICommand(name="clean", description = "Vamo fazer a limpa, hein?? Não vai sobrar ninguem!!", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="role", description = "Fala onde vamos limpar pro seu pai aqui!", type = OptionType.ROLE, required = true)
    public static void clean(SlashCommandInteractionEvent event) {
        // Not null, because it's server side and required
        Role role = event.getOption("role").getAsRole();
        String confirmId = String.format("%s:%s:%s", confirmAction, cleanAction,  role.getId());
        String cancelId  = cancelAction;

        MessageEmbed messageEmbed = Utils.simpleEmbed("Você tem certeza disso?",
                "Você realmente quer remover **TODOS** os usuários do cargo" + role.getAsMention() + "??? Isso é muito forte viu!",
                Color.ORANGE);

        Button successButton = Button.success(confirmId, "Sim! Vamos limpar tudo!!");
        Button cancelButton  = Button.danger(cancelId, "Não, terei piedade destes seres!!");

        event.replyEmbeds(messageEmbed).addActionRow(successButton, cancelButton).setEphemeral(true).queue();
    }

    @ISubGroup(name = "channel", description = "Channel SubGroup")
    public static class Channel {
        @ICommand(name = "permissions", description = "Quer atualizar rapidamente algumas permissões do seu cargo??", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "role", description = "Fala o cargo ai chefe", type = OptionType.ROLE, required = true)
        @IArgument(name = "related", description = "Pra qual canal que tu quer atualizar???", type = OptionType.CHANNEL, required = true)
        @IArgument(name = "permissions", description = "Permissions", type = OptionType.STRING, required = true,
                choices = {
                        @IArgument.IChoice(value = "Read", key = "Read"),
                        @IArgument.IChoice(value = "Write", key = "Write"),
                        @IArgument.IChoice(value = "Connect", key = "Connect"),
                        @IArgument.IChoice(value = "Speak", key = "Speak")
                })
        @IArgument(name = "allow", description = "Allow", type = OptionType.BOOLEAN, required = true)
        public static void permissions(SlashCommandInteractionEvent event) {

            Role role = event.getOption("role").getAsRole();
            String permission = event.getOption("permissions").getAsString();
            boolean allow = event.getOption("allow").getAsBoolean();


            List<String> voicePermissions = List.of("Connect", "Speak");
            List<String> textPermissions = List.of("Read", "Write");
            ChannelType channelType = event.getOption("related").getChannelType();

            // Everything is fine, let's update the permissions
            if(channelType.isAudio()) {
                VoiceChannel channel = event.getOption("related").getAsVoiceChannel();

                if(!voicePermissions.contains(permission)) {
                    MessageEmbed messageEmbed = Utils.simpleEmbed("Permissão inválida", "Permissões de voz só podem ser: " + voicePermissions.toString(), Color.RED);
                    event.replyEmbeds(messageEmbed).setEphemeral(true).queue();
                    return;
                }

                if(allow) role.getManager().givePermissions(getPermission(permission)).queue();
                else      role.getManager().revokePermissions(getPermission(permission)).queue();
            }
            if(channelType.isMessage()) {
                BaseGuildMessageChannel channel = (BaseGuildMessageChannel) event.getOption("related").getAsGuildChannel();

                if(!textPermissions.contains(permission)) {
                    MessageEmbed messageEmbed = Utils.simpleEmbed("Permissão inválida", "Permissões de texto só podem ser: " + textPermissions.toString(), Color.RED);
                    event.replyEmbeds(messageEmbed).setEphemeral(true).queue();
                    return;
                }

                if(allow) channel.getManager().putRolePermissionOverride(role.getIdLong(), List.of(getPermission(permission)), null).queue();
                else      channel.getManager().putRolePermissionOverride(role.getIdLong(), null, List.of(getPermission(permission))).queue();
            }

            MessageEmbed messageEmbed = Utils.simpleEmbed("Permissões atualizadas", String.format("Permissão '%s' no canal atualizada com sucesso para o cargo %s", permission, role.getAsMention()), Color.GREEN);
            event.replyEmbeds(messageEmbed).setEphemeral(true).queue();
        }

        public static Permission getPermission(String permission){
            return switch (permission){
                case "Read" ->  Permission.VIEW_CHANNEL;
                case "Write" -> Permission.MESSAGE_SEND;
                case "Connect" -> Permission.VOICE_CONNECT;
                case "Speak" -> Permission.VOICE_SPEAK;
                default -> throw new IllegalArgumentException("Invalid Permission");
            };
        }
    }

    // SECTION BUTTON
    @IButton(id=confirmAction)
    public static void confirm(ButtonInteractionEvent event) {
        System.out.println("Received confirm: " + event.getComponentId());

        /*
        Expected Args:
        - buttonId
        - actionId
        - roleId
         */
        String[] args = event.getComponentId().split(":");

        if (args.length != 3) {
            event.reply("Invalid arguments -> " + event.getComponentId()).setEphemeral(true).queue();
            return;
        }

        String actionId = args[1];
        String roleId   = args[2];

        if (actionId.equals(removeAction)) {
            // Guild never null because it's a button in a server
            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                MessageEmbed embed = Utils.simpleEmbed("Acharam o cargo antes de mim, temos um outro gerente na cidade",
                        "Verifica ai se deletaram o cargo '" + role.getName() + "' ok??", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            MessageEmbed embed = Utils.simpleEmbed("Deletei o cargo, de nada :ok_hand::skin-tone-3: ",
                                            "Foi esse aqui que apaguei ó '" + role.getName() + "' ok??", Color.GREEN);
            event.editMessageEmbeds(embed).setActionRows().queue();
            role.delete().queue();
        } else if (actionId.equals(cleanAction)) {
            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                MessageEmbed embed = Utils.simpleEmbed("Não achei esse cargo... :(", "Maior loucura! Talvez alguem já tenha excluido!", Color.RED);
                event.editMessageEmbeds(embed).setActionRows().queue();
                return;
            }


            Member selfMember = event.getGuild().getSelfMember();
            List<RestAction<Void>> removeActions = new ArrayList<>();
            event.getGuild().findMembersWithRoles(role).onSuccess(members -> {
                members.forEach(member -> {
                    if (selfMember.canInteract(member)) {
                        removeActions.add(event.getGuild().removeRoleFromMember(member, role));
                    }
                });
            });

            if (removeActions.isEmpty()) {
                MessageEmbed embed = Utils.simpleEmbed("Tentei o que pude", "Tentei fazer a limpa, mas não encontrei alguém que eu possa remover o cargo", Color.ORANGE);
                event.editMessageEmbeds(embed).setActionRows().queue();
            } else {
                RestAction.accumulate(removeActions, Collectors.toList()).queue(voids -> {
                    MessageEmbed embed = Utils.simpleEmbed("Fiz a limpa!", String.format("Consegui remover o cargo de %d membros", removeActions.size()), Color.GREEN);
                    event.editMessageEmbeds(embed).setActionRows().queue();
                });
            }
        }
    }

    @IButton(id=cancelAction)
    public static void cancel(ButtonInteractionEvent event) {
        MessageEmbed embed = Utils.simpleEmbed("Cancelado", "Cancelado com sucesso!! Não tente isso de novo heinn...", Color.RED);

        event.editMessageEmbeds(embed).setActionRows().queue();
    }

}

