package at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps

import at.hannibal2.skyhanni.data.SkillExperience

class SkillLevelStep(
    val skillName: String,
    private val skillLevelNeeded: Int,
    skillExpNeeded: Long = SkillExperience.getExpForLevel(skillLevelNeeded),
) :
    ProgressionStep("$skillName $skillLevelNeeded", skillExpNeeded)
