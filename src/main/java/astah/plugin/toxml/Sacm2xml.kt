package astah.plugin.toxml

import astah.plugin.SacmForGsn.*
import com.change_vision.jude.api.inf.ui.IWindow
import java.io.StringWriter
import java.util.HashSet
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

class Sacm2xml<Id, Content> @Throws(XMLStreamException::class)
constructor(internal var window: IWindow) {
    var history: MutableSet<String>
    var outFactory: XMLOutputFactory
    var writer: XMLStreamWriter
    var stringWriter: StringWriter

    init {
        history = HashSet()
        outFactory = XMLOutputFactory.newInstance()
        stringWriter = StringWriter()
        writer = outFactory.createXMLStreamWriter(stringWriter)
    }

    @Throws(XMLStreamException::class)
    fun startWriting() {
        with(writer) {
            writeStartDocument() // start document
            writeStartElement("ARM:Argumentation") // start element
            writeAttribute("xmi:version", "2.0")
            writeAttribute("xmlns:xmi", "http://www.omg.org/XMI")
            writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            writeAttribute("xmlns:ARM", "http://schema.omg.org/SACM/1.0/Argumentation")
            writeAttribute("xmi:id", "acceptance_test_suite_validity")
            writeAttribute("id", "no_title")
            writeAttribute("description", "")
            writeAttribute("content", "")
            writeCharacters("\n")
        }
    }

    @Throws(XMLStreamException::class)
    fun closeWriting(): StringWriter {
        with(writer) {
            writeEndElement() // end element
            writeCharacters("\n")
            writeEndDocument() // end document
            close()
        }
        return stringWriter
    }

    @Throws(XMLStreamException::class)
    fun GSNtoXML(gsns: List<SACM<Int, String>>) {
        gsns.forEach { gsn ->
            if (gsn.hasChildren()) {
                GSNtoXML(gsn.getChildren())
            }
            if (SACMType.isArrowType(gsn.getNode().type)) {
                val node = gsn.getNode()
                if (node is SACMAssertedNode) {
                    arrowToXML(node)
                } else {
                    throw Error()
                }
            } else {
                elementToXML(gsn.getNode())
            }
        }
    }

    @Throws(XMLStreamException::class)
    private fun elementToXML(node: SACMNode<Int, String>) {
        if (history.contains(node.stringId)) {
            return
        }
        history.add(node.stringId)
        with(writer) {
            writeStartElement("argumentElement ") // start element
            writeAttribute("xsi:type", "ARM:" + node.type.sacmXsiType)
            writeAttribute("xmi:id", node.stringId)
            writeAttribute("id", node.stringId)
            writeAttribute("description", "")
            writeAttribute("content", node.content.toString())
            if (node.type == SACMType.Claim) {
                if (node is SACMClaimNode) {
                    writeAttribute("assumed", if (node.assumed) "true" else "false")
                    writeAttribute("toBeSupported", if (node.toBeSupported) "true" else "false")
                } else {
                    throw Error()
                }
            } else if (node.type == SACMType.SolutionInformationElement) {
                writeAttribute("url", "")
            } else if (node.type == SACMType.ArgumentReasoning) {
                if (node is SACMArgumentReasoningNode) {
                    writeAttribute("describedInference", node.describedInference.stream()
                            .map({ arg -> arg.stringId }).reduce("", { a, b -> a + " " + b }))
                } else {
                    throw Error()
                }
            }
            writeCharacters("\n")
            writeEndElement() // end element
            writeCharacters("\n")
        }
    }

    @Throws(XMLStreamException::class)
    private fun arrowToXML(arrow: SACMAssertedNode<Int, String>) {
        with(writer) {
            writeStartElement("argumentElement ") // start element
            writeAttribute("xsi:type", "ARM:" + arrow.type.sacmXsiType)
            writeAttribute("xmi:id", arrow.stringId)
            writeAttribute("id", arrow.stringId)
            writeAttribute("description", "")
            writeAttribute("content", "")
            writeAttribute("source", arrow.source.stringId)
            writeAttribute("target", arrow.target.stringId)
            writeCharacters("\n")
            writeEndElement() // end element
            writeCharacters("\n")
        }
    }
}