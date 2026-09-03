package com.pocketlawbook.alaska.data.local

import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction

/**
 * Seed content for the verified-content store.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * NOT YET ATTORNEY-REVIEWED. This is a small demonstration set that exists so
 * the Home → Analysis → Action steps slice runs end to end. Every entry below
 * must be reviewed and signed off by a licensed Alaska attorney, and stamped
 * with an effective date and a last-reviewed date, before this app ships to
 * anyone. Do not add entries here from memory or from model output — an
 * invented citation is precisely the failure this architecture exists to
 * prevent. Entries come from primary sources, transcribed.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * The keys here are the contract with the analyzer: a violation key that has no
 * entry in this map is dropped from results rather than shown with unverified
 * text. See LegalAnalysisRepository.
 */
object VerifiedContentSeed {

    const val KEY_PROMPT_PROBABLE_CAUSE = "VIOLATION_48_HOUR_ARRAIGNMENT"
    const val KEY_MIRANDA = "VIOLATION_MIRANDA_RIGHTS"
    const val KEY_COUNSEL = "VIOLATION_RIGHT_TO_COUNSEL"
    const val KEY_SEARCH_WITHOUT_WARRANT = "VIOLATION_WARRANTLESS_SEARCH"
    const val KEY_HABITABILITY_HEAT = "VIOLATION_LANDLORD_HABITABILITY"
    const val KEY_LANDLORD_NONPAYMENT_NOTICE = "VIOLATION_LANDLORD_NONPAYMENT_NOTICE"
    const val KEY_RECORDING_POLICE = "VIOLATION_RIGHT_TO_RECORD_POLICE"

    val entries: Map<String, ActionStepEntity> = listOf(
        ActionStepEntity(
            violationKey = KEY_PROMPT_PROBABLE_CAUSE,
            jurisdiction = Jurisdiction.FEDERAL,
            description = "County of Riverside v. McLaughlin, 500 U.S. 44 (1991), applying " +
                "Gerstein v. Pugh, 420 U.S. 103 (1975): a person arrested without a warrant is " +
                "generally entitled to a judicial probable-cause determination within 48 hours.",
            actionSteps = listOf(
                "Write down the date and time you were arrested, as precisely as you can recall.",
                "Ask for a copy of your booking record, which shows the recorded time of arrest.",
                "Tell your attorney how long you were held before seeing a judge.",
                "Ask your attorney whether a motion based on the delay is appropriate in your case."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_MIRANDA,
            jurisdiction = Jurisdiction.FEDERAL,
            description = "Miranda v. Arizona, 384 U.S. 436 (1966): before questioning someone " +
                "who is in custody, police must advise them of the right to remain silent and " +
                "the right to an attorney.",
            actionSteps = listOf(
                "Write down what you were asked and what you said, while you still remember it.",
                "Note whether you were free to leave at the time of the questioning.",
                "Note whether you were read your rights, and at what point.",
                "Give these notes to your attorney rather than discussing them with anyone else."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_COUNSEL,
            jurisdiction = Jurisdiction.FEDERAL,
            description = "U.S. Const. amend. VI, and Miranda v. Arizona, 384 U.S. 436 (1966): " +
                "you may ask for a lawyer, and questioning is to stop once you do.",
            actionSteps = listOf(
                "Say clearly that you want a lawyer. An unambiguous request matters legally.",
                "After asking for a lawyer, you do not have to answer further questions.",
                "Write down the time you asked and whether questioning continued afterward.",
                "If you cannot afford a lawyer, ask for the public defender."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_SEARCH_WITHOUT_WARRANT,
            jurisdiction = Jurisdiction.FEDERAL,
            description = "U.S. Const. amend. IV: searches are generally unreasonable without a " +
                "warrant, subject to established exceptions.",
            actionSteps = listOf(
                "Write down what was searched, when, and who was present.",
                "Note whether you were shown a warrant, and whether you were asked for consent.",
                "Note whether you said yes, said no, or said nothing at all.",
                "Do not physically interfere with a search. Record what happened instead."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_HABITABILITY_HEAT,
            jurisdiction = Jurisdiction.ALASKA,
            description = "Alaska Uniform Residential Landlord and Tenant Act, AS 34.03.100: a " +
                "landlord must maintain the premises in a fit and habitable condition, including " +
                "supplying heat and running water.",
            actionSteps = listOf(
                "Photograph the conditions and record the indoor temperature with the date.",
                "Notify your landlord in writing and keep a copy of what you sent.",
                "Keep receipts for anything you have to spend because of the condition.",
                "Contact Alaska Legal Services Corporation if the condition is not fixed."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_LANDLORD_NONPAYMENT_NOTICE,
            jurisdiction = Jurisdiction.ALASKA,
            description = "AS 34.03.220(b): if rent is unpaid, a landlord must give written notice " +
                "of the nonpayment and of the intent to terminate the tenancy. The tenant then has " +
                "7 days after that notice to pay the rent in full before the landlord may terminate " +
                "the rental agreement and recover possession.",
            actionSteps = listOf(
                "Check whether the notice you got is in writing and says you have 7 days to pay in full.",
                "If you can pay within those 7 days, do so and keep a receipt or other proof of payment.",
                "If you were not given written notice, or given fewer than 7 days, write down exactly " +
                    "when and how you were told to leave.",
                "Contact Alaska Legal Services Corporation before you move out or sign anything the " +
                    "landlord gives you."
            )
        ),
        ActionStepEntity(
            violationKey = KEY_RECORDING_POLICE,
            jurisdiction = Jurisdiction.FEDERAL,
            description = "Fordyce v. City of Seattle, 55 F.3d 436 (9th Cir. 1995): the Ninth Circuit " +
                "(which includes Alaska) recognized a First Amendment right to film matters of public " +
                "interest, including police officers carrying out their duties in a public place.",
            actionSteps = listOf(
                "If you can do so safely, without touching the officer or blocking their movement, " +
                    "you may record police performing their duties in a public place.",
                "Do not physically interfere or ignore a lawful order to step back — recording is " +
                    "protected, obstruction is not.",
                "If an officer orders you to stop recording or takes your device, note the officer's " +
                    "name, badge number, and the time, as soon as you safely can.",
                "Do not delete anything from your device. Write down what happened while you remember it."
            )
        )
    ).associateBy { it.violationKey }
}
