package astah.plugin.SacmForGsn

import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

enum class AstahEdition { Pro, SysML, All; }
enum class PlaceType { STEREOTYPE, TYPE_CLASSIFIER, TYPE_BASE, NAME_SUFFIX; }

enum class GSNDiagramType {
    ObjectsInClassDiagram {
        override val astahEdition = AstahEdition.Pro
        override val nodeTypeDefinition = NodeTypeDefinition(PlaceType.TYPE_CLASSIFIER)
        override fun getNodeName(node: INodePresentation) = node.model.toString()
        override fun isSubjectNode(node: INodePresentation): Boolean =
                node.type == "InstanceSpecification" && node.model is IInstanceSpecification
        override fun isTarget(node: INodePresentation, link: ILinkPresentation): Boolean =
                (link.target.model as IInstanceSpecification) != node.model
    },
    ObjectsInActivityDiagram {
        override val astahEdition = AstahEdition.SysML
        override val nodeTypeDefinition = NodeTypeDefinition(PlaceType.TYPE_BASE)
        override fun getNodeName(node: INodePresentation) = node.model.toString()
        override fun isSubjectNode(node: INodePresentation): Boolean =
                node.type == "ObjectNode" && node.model is IObjectNode
        override fun isTarget(node: INodePresentation, link: ILinkPresentation): Boolean =
                (link.target.model as IObjectNode) != node.model
    },
    ClassInClassDiagram {
        override val astahEdition = AstahEdition.Pro
        override val nodeTypeDefinition = NodeTypeDefinition(PlaceType.STEREOTYPE)
        override fun getNodeName(node: INodePresentation) = node.model.toString()
        override fun isSubjectNode(node: INodePresentation): Boolean = node.type == "Class" && node.model is IClass
        override fun isTarget(node: INodePresentation, link: ILinkPresentation): Boolean =
                (link.target.model as IClass) != node.model
    },
    BlockInBlockDiagram {
        override val astahEdition = AstahEdition.SysML
        override val nodeTypeDefinition = NodeTypeDefinition(PlaceType.STEREOTYPE)
        override fun getNodeName(node: INodePresentation) = node.model.toString()
        override fun isSubjectNode(node: INodePresentation): Boolean = node.type == "Block" && node.model is IBlock
        override fun isTarget(node: INodePresentation, link: ILinkPresentation): Boolean =
                (link.target.model as IBlock) != node.model
    },
    TopicInMindMap {
        override val astahEdition = AstahEdition.All
        override val nodeTypeDefinition = NodeTypeDefinition(PlaceType.NAME_SUFFIX)
        override fun getNodeName(node: INodePresentation) = node.toString()
        override fun isSubjectNode(node: INodePresentation): Boolean = node.type == "Topic"
        override fun isTarget(node: INodePresentation, link: ILinkPresentation): Boolean =
                (link.target as INodePresentation) != node
    };

    abstract val astahEdition: AstahEdition // currently this property is not used by others
    abstract val nodeTypeDefinition: NodeTypeDefinition
    abstract fun getNodeName(node: INodePresentation) : String
    abstract fun isSubjectNode(node : INodePresentation) : Boolean
    abstract fun isTarget(node : INodePresentation, link : ILinkPresentation) : Boolean
}

class NodeTypeDefinition(val placeType: PlaceType) {
    private fun getNodeTypeAsStereotype(e: IElement): String? =
            e.stereotypes.firstOrNull { GSNType.obtainType(it) != null }
    fun getTypeName(node: INodePresentation): String? =
            when (placeType) {
                PlaceType.TYPE_CLASSIFIER -> (node.model as IInstanceSpecification).classifier.toString()
                PlaceType.STEREOTYPE -> getNodeTypeAsStereotype(node.model)
                PlaceType.TYPE_BASE -> (node.model as IObjectNode).base.toString()
                PlaceType.NAME_SUFFIX -> node.toString().substringAfter(':').trim()
            }
}