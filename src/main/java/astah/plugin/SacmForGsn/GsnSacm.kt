package astah.plugin.SacmForGsn

import java.util.*

class GsnSacm(val sacm: SACM<Int, String>) {
    fun addSolution(solution: GsnSacm) {
        val leaf = solution.sacm.getLeaf()
        val claim = sacm.getClaim()
        if (claim == null || leaf == null)
            throw Error()
        claim.toBeSupported = false
        sacm.addChild(SACM.CompositeSACM(SACMAssertedEvidenceNode(leaf.id,
                solution.sacm.getNode(), claim), Arrays.asList(solution.sacm)))
    }

    fun addSolution(content: String) = addSolution(solutionConstructor(content))

    fun addSubgoal(ugsn: GsnSacm) {
        val c = sacm.getCompositeSACM() ?: throw Error()
        val claim = sacm.getClaim() ?: throw Error()
        claim.toBeSupported = false
        sacm.addChild(SACM.CompositeSACM(SACMAssertedInferenceNode(ugsn.sacm.getNode().id,
                ugsn.sacm.getNode(), c.internal), Arrays.asList(ugsn.sacm)))
    }

    fun addStrategy(content: String, subgoals: List<SACM<Int, String>>): SACM<Int, String> {
        val claim = sacm.getClaim() ?: throw Error()
        val _ais = mutableListOf<SACMNode<Int, String>>()
        val ais = mutableListOf<SACM<Int, String>>()
        subgoals.forEach { subgoal ->
            val _ai = SACMAssertedInferenceNode(serialId++, subgoal.getNode(), claim)
            _ais.add(_ai)
            ais.add(SACM.CompositeSACM(_ai, Arrays.asList(subgoal)))
        }
        claim.toBeSupported = false
        val strategy = SACM.CompositeSACM(SACMArgumentReasoningNode(serialId++, content, _ais), ais)
        sacm.addChild(strategy)
        return strategy
    }

    fun addJustification(claimGsnSacm: GsnSacm) =
            abstractContextAdd(claimGsnSacm, ::SACMAssertedContextNode)

    fun addJustification(content: String) =
            abstractContextAdd(content, ::justificationConstructor, ::SACMAssertedContextNode)

    fun addContext(ieGsnSacm: GsnSacm) =
            abstractContextAdd(ieGsnSacm, ::SACMAssertedContextNode)

    fun addContext(content: String) =
            abstractContextAdd(content, ::contextConstructor, ::SACMAssertedContextNode)

    fun addAssumption(claimGsnSacm: GsnSacm) =
            abstractContextAdd(claimGsnSacm, ::SACMAssertedContextNode)

    fun addAssumption(content: String) =
            abstractContextAdd(content, ::assumptionConstructor, ::SACMAssertedContextNode)

    private fun abstractContextAdd(content: String, constructor: (String) -> GsnSacm,
                                   assertNode: (Int, SACMNode<Int, String>, SACMNode<Int, String>) ->
                                   SACMAssertedNode<Int, String>) =
            abstractContextAdd(constructor(content), assertNode)

    private fun abstractContextAdd(gsnSacm: GsnSacm,
                                   assertNode: (Int, SACMNode<Int, String>, SACMNode<Int, String>) ->
                                   SACMAssertedNode<Int, String>) {
        val c = sacm.getCompositeSACM() ?: throw Error()
        sacm.addChild(SACM.CompositeSACM(assertNode(gsnSacm.sacm.getNode().id,
                gsnSacm.sacm.getNode(), c.internal), Arrays.asList(gsnSacm.sacm)))
    }

    companion object {
        private var serialId = 0
        fun contextConstructor(content: String): GsnSacm =
                GsnSacm(SACM.UnitSACM(SACMContextNode(serialId++, content)))

        fun solutionConstructor(content: String): GsnSacm =
                GsnSacm(SACM.UnitSACM(SACMSolutionNode(serialId++, content)))

        fun goalClaimConstructor(content: String): GsnSacm =
                GsnSacm(SACM.CompositeSACM(SACMGoalClaimNode(serialId++, content,
                        true, false), mutableListOf()))

        fun justificationConstructor(content: String): GsnSacm =
                GsnSacm(SACM.CompositeSACM(SACMJustificationNode(serialId++, content), mutableListOf()))

        fun assumptionConstructor(content: String): GsnSacm =
                GsnSacm(SACM.CompositeSACM(SACMAssumptionNode(serialId++, content), mutableListOf()))
    }
}