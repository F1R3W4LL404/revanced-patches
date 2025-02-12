package app.revanced.patches.youtube.layout.panels.fullscreen.popup.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.panels.fullscreen.popup.annotations.PlayerPopupPanelsCompatibility
import app.revanced.patches.youtube.layout.panels.fullscreen.popup.fingerprints.EngagementPanelControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("disable-fullscreen-panels-auto-popup")
@Description("Disables fullscreen panels from appearing automatically when going fullscreen (playlist or live chat).")
@PlayerPopupPanelsCompatibility
@Version("0.0.1")
class FullscreenPanelsPatch : BytecodePatch(
    listOf(
        EngagementPanelControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_player_popup_panels_enabled",
                StringResource("revanced_player_popup_panels_title", "Disable player popup panels"),
                false,
                StringResource("revanced_player_popup_panels_summary_on", "Player popup panels are disabled"),
                StringResource("revanced_player_popup_panels_summary_off", "Player popup panels are enabled")
            )
        )

        val engagementPanelControllerMethod = EngagementPanelControllerFingerprint
            .result?.mutableMethod ?: return EngagementPanelControllerFingerprint.toErrorResult()

        engagementPanelControllerMethod.addInstructions(
            0, """
            invoke-static { }, Lapp/revanced/integrations/patches/DisablePlayerPopupPanelsPatch;->disablePlayerPopupPanels()Z
            move-result v0
            if-eqz v0, :player_popup_panels
            if-eqz p4, :player_popup_panels
            const/4 v0, 0x0
            return-object v0
            :player_popup_panels
            nop
        """
        )

        return PatchResultSuccess()
    }
}
