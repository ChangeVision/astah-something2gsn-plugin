package astah.plugin

import astah.plugin.SacmForGsn.*
import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

class GSNGenerator(val diagramType: GSNDiagramType, diagram: IDiagram) {
    val arrows : MutableList<ILinkPresentation> = mutableListOf()
    val nodes : MutableList<INodePresentation> = mutableListOf()
    val gsns : List<GsnSacm>

    init {
        diagram.presentations.forEach {
            if (it is ILinkPresentation) arrows += it
            if (it is INodePresentation && it.type != "Frame") nodes += it
        }
        gsns = nodes.filter(diagramType::isSubjectNode)
                .filter { n -> arrows.all { diagramType.isTarget(n, it) } }.mapNotNull(::generateClaim)
    }

    private fun obtainNext(a : INodePresentation): List<INodePresentation> =
        nodes.filter { b -> arrows.any { link -> link.source == a && link.target == b } }

    fun generateClaim(c: INodePresentation): GsnSacm? {
        val cNodeType = GSNType.obtainType(diagramType.nodeTypeDefinition.getTypeName(c))
        cNodeType?.let {
            when (cNodeType) {
                GSNType.Goal -> {
                    val nexts = obtainNext(c)
                    val ugsn = GsnSacm.goalClaimConstructor(diagramType.getNodeName(c))
                    nexts.mapNotNull(::generateArgument).forEach { (id, children) ->
                        val (nodeType, nodeName) = id
                        when (nodeType) {
                            GSNType.Strategy -> {
                                val subgoalUGSNs = mutableListOf<GsnSacm>()
                                val nonGoalUGSNs = mutableListOf<GsnSacm>()
                                children.forEach {
                                    if (it.sacm.getNode() is SACMGoalClaimNode)
                                        subgoalUGSNs += it
                                    else
                                        nonGoalUGSNs += it
                                }
                                val strategy = ugsn.addStrategy(nodeName, subgoalUGSNs.map { it.sacm })
                                nonGoalUGSNs.forEach {
                                    when (it.sacm.getNode()) {
                                        is SACMJustificationNode -> GsnSacm(strategy).addJustification(it)
                                        is SACMAssumptionNode -> GsnSacm(strategy).addAssumption(it)
                                        is SACMContextNode -> GsnSacm(strategy).addContext(it)
                                        else -> {
                                        } // non-goal children for a strategy must be one of the nodes above
                                    }
                                }
                            }
                            GSNType.Goal -> {
                            } // this plugin does not support a goal having sub-goals without strategies
                            GSNType.Solution -> ugsn.addSolution(nodeName)
                            GSNType.Context -> ugsn.addContext(nodeName)
                            GSNType.Justification -> ugsn.addJustification(nodeName)
                            GSNType.Assumption -> ugsn.addAssumption(nodeName)
                        }
                    }
                    return ugsn
                }
                GSNType.Solution ->
                    return GsnSacm.solutionConstructor(diagramType.getNodeName(c))
                GSNType.Context ->
                    return GsnSacm.contextConstructor(diagramType.getNodeName(c))
                GSNType.Justification ->
                    return GsnSacm.justificationConstructor(diagramType.getNodeName(c))
                GSNType.Assumption ->
                    return GsnSacm.assumptionConstructor(diagramType.getNodeName(c))
                else -> return null
            }
        } ?: return null
    }
    fun generateArgument(a : INodePresentation): Pair<Pair<GSNType, String>, List<GsnSacm>>? {
        val aNodeType = GSNType.obtainType(diagramType.nodeTypeDefinition.getTypeName(a))
        return aNodeType?.let{
            Pair(Pair(aNodeType, diagramType.getNodeName(a)), obtainNext(a).mapNotNull(::generateClaim))
        }
    }
}