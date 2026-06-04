package com.blueoauld.server.admin.application

import com.blueoauld.server.admin.application.type.ResetTarget
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.discord.infrastructure.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class AdminMemberResetCommand(

    private val adminService: AdminService,
) : SlashCommand {

    override val name: String = "초기화"

    override fun data(): SlashCommandData = Commands.slash(name, "회원의 특정 정보를 초기화합니다.")
        .addOptions(
            OptionData(OptionType.STRING, "항목", "초기화할 항목", true)
                .addChoices(ResetTarget.entries.map { Command.Choice(it.label, it.name) }),
            OptionData(OptionType.STRING, "닉네임", "대상 회원의 닉네임", true),
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val targetName = event.getOption("항목")?.asString
        val nickname = event.getOption("닉네임")?.asString

        if (targetName.isNullOrBlank() || nickname.isNullOrBlank()) {
            event.reply("항목과 닉네임을 모두 입력해주시길 바랍니다.").setEphemeral(true).queue()
            return
        }

        val target = runCatching { ResetTarget.valueOf(targetName) }.getOrNull()
        if (target == null) {
            event.reply("알 수 없는 항목입니다.").setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        try {
            adminService.resetMember(nickname, target)
        } catch (e: CustomException) {
            event.hook.sendMessage(e.errorCode.message).queue()
            return
        }

        event.hook.sendMessage("**$nickname** 회원의 **${target.label}** 항목을 초기화했습니다.").queue()
    }
}