package astah.plugin.SacmForGsn

sealed class SACM<Id, Content>() {
    class UnitSACM<Id, Content>(val terminal: SACMNode<Id, Content>): SACM<Id, Content>()
    class CompositeSACM<Id, Content>(val internal: SACMInternalNode<Id, Content>,
                                     val edges: MutableList<SACM<Id, Content>>): SACM<Id, Content>()

    fun getNode(): SACMNode<Id, Content> {
        return when (this) {
            is UnitSACM -> terminal
            is CompositeSACM -> internal
        }
    }

    fun getClaim(): SACMClaimNode<Id, Content>? =
            if (this is CompositeSACM && internal is SACMClaimNode && internal.type == SACMType.Claim)
                internal
            else
                null

    fun isGoalClaim(): Boolean {
        val claim = getClaim()
        return if (claim != null) claim is SACMGoalClaimNode else false
    }

    fun getCompositeSACM(): CompositeSACM<Id, Content>? = if (this is CompositeSACM) this else null
    fun getStrategy(): SACMArgumentReasoningNode<Id, Content>? =
            if (this is CompositeSACM && internal is SACMArgumentReasoningNode &&
                    internal.type == SACMType.ArgumentReasoning) internal else null

    fun getLeaf(): SACMNode<Id, Content>? = if (this is UnitSACM) terminal else null
    fun hasChildren(): Boolean = this is CompositeSACM && !edges.isEmpty()
    fun getChildren(): List<SACM<Id, Content>> = (this as? CompositeSACM)?.edges ?: throw Error()
    fun addChild(child: SACM<Id, Content>) = if (this is CompositeSACM) edges += child else throw Error()
}

abstract class SACMNode<Id, Content>(val id: Id, var content: Content, val type: SACMType) {
    val stringId: String
        get() = type.sacmPrefix + id.toString()
}

abstract class SACMLeafNode<Id, Content>(id: Id, content: Content, type: SACMType) :
        SACMNode<Id, Content>(id, content, type) {
    init {
        if (!SACMType.isLeafType(type)) throw Error()
    }
}

abstract class SACMInternalNode<Id, Content>(id: Id, content: Content, type: SACMType) :
        SACMNode<Id, Content>(id, content, type) {
    init {
        if (!SACMType.isInternalType(type)) throw Error()
    }
}

abstract class SACMClaimNode<Id, Content>(id: Id, content: Content, var toBeSupported: Boolean, val assumed: Boolean):
        SACMInternalNode<Id, Content>(id, content, SACMType.Claim)

class SACMJustificationNode<Id, Content>(id: Id, content: Content):
        SACMClaimNode<Id, Content>(id, content, false, false)

class SACMAssumptionNode<Id, Content>(id: Id, content: Content):
        SACMClaimNode<Id, Content>(id, content, false, true)

class SACMGoalClaimNode<Id, Content>(id: Id, content: Content, toBeSupported: Boolean, assumed: Boolean):
        SACMClaimNode<Id, Content>(id, content, toBeSupported, assumed)

class SACMContextNode<Id, Content>(id: Id, content: Content):
        SACMLeafNode<Id, Content>(id, content, SACMType.ContextInformationElement)

abstract class SACMAssertedNode<Id, Content>(
        id: Id, type: SACMType, var source: SACMNode<Id, Content>, var target: SACMNode<Id, Content>):
        SACMInternalNode<Id, Content>(id, source.content, type)

class SACMAssertedInferenceNode<Id, Content>(id: Id, s: SACMNode<Id, Content>, t: SACMNode<Id, Content>):
        SACMAssertedNode<Id, Content>(id, SACMType.AssertedInference, s, t)

class SACMAssertedEvidenceNode<Id, Content>(id: Id, s: SACMNode<Id, Content>, t: SACMNode<Id, Content>):
        SACMAssertedNode<Id, Content>(id, SACMType.AssertedEvidence, s, t)

class SACMAssertedContextNode<Id, Content>(id: Id, s: SACMNode<Id, Content>, t: SACMNode<Id, Content>):
        SACMAssertedNode<Id, Content>(id, SACMType.AssertedContext, s, t)

class SACMSolutionNode<Id, Content>(id: Id, content: Content):
        SACMLeafNode<Id, Content>(id, content, SACMType.SolutionInformationElement)

class SACMArgumentReasoningNode<Id, Content>(id: Id, content: Content,
                                             var describedInference: List<SACMNode<Id, Content>>):
        SACMInternalNode<Id, Content>(id, content, SACMType.ArgumentReasoning)