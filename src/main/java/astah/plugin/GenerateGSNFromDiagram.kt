package astah.plugin

import astah.plugin.SacmForGsn.GSNDiagramType
import astah.plugin.SacmForGsn.GsnSacm
import astah.plugin.toxml.Sacm2xml
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.exception.InvalidUsingException
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException
import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.project.ProjectAccessor
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate
import com.change_vision.jude.api.inf.ui.IWindow
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.xml.stream.XMLStreamException

class GenerateGSNFromDiagram : IPluginActionDelegate {
    override fun run(window: IWindow) {
        try {
            val project = AstahAPI.getAstahAPI().projectAccessor
            val diagram = project.viewManager.diagramViewManager.currentDiagram
            val gsns = mutableListOf<GsnSacm>()
            val edition = project.astahEdition

            if (diagram is IMindMapDiagram) {
                gsns.addAll(GSNGenerator(GSNDiagramType.TopicInMindMap, diagram).gsns)
            } else if (diagram is IActivityDiagram) {
                gsns.addAll(GSNGenerator(GSNDiagramType.ObjectsInActivityDiagram, diagram).gsns)
            } else if (edition == "sysML" && diagram is IBlockDefinitionDiagram) {
                gsns.addAll(GSNGenerator(GSNDiagramType.BlockInBlockDiagram, diagram).gsns)
            } else if (edition == "professional" && diagram is IClassDiagram) {
                gsns.addAll(classDiagramGenerator(diagram))
            } else {
                JOptionPane.showMessageDialog(window.parent,
                        "The current diagram cannot generate sacm xmi files.")
            }
            if (!gsns.isEmpty())
                generateGSN(gsns, window, project)
            else
                JOptionPane.showMessageDialog(window.parent, "Any gsn cannot be found.")
        } catch (e: ProjectNotFoundException) {
            JOptionPane.showMessageDialog(window.parent,
                    "Project is not opened.  Please open the project or create new project.",
                    "Warning", JOptionPane.WARNING_MESSAGE)
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(window.parent,
                    "Unexpected error has occurred.", "Alert", JOptionPane.ERROR_MESSAGE)
            throw IPluginActionDelegate.UnExpectedException()
        }
    }

    @Throws(InvalidUsingException::class, ProjectNotFoundException::class, XMLStreamException::class)
    private fun generateGSN(gsn: List<GsnSacm>, window: IWindow, projectAccessor: ProjectAccessor) {
        val sacm2xml = Sacm2xml<Int, String>(window)
        sacm2xml.startWriting()
        sacm2xml.GSNtoXML(gsn.map { it.sacm })
        val stringWriter = sacm2xml.closeWriting()
        val outputFile = getOutputDirPath(window, projectAccessor)
        if (outputFile != null) {
            try {
                val bwriter = BufferedWriter(OutputStreamWriter(FileOutputStream(outputFile), "UTF-8"))
                bwriter.write(stringWriter.toString())
                bwriter.newLine()
                bwriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(ProjectNotFoundException::class)
    private fun getOutputDirPath(window: IWindow, projectAccessor: ProjectAccessor): String? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Choose a file you want to save"
        val projectDir = File(projectAccessor.projectPath).parentFile
        if (projectDir != null && projectDir.exists() && projectDir.canWrite())
            fileChooser.selectedFile = projectDir
        return if (fileChooser.showSaveDialog(window.parent) == JFileChooser.APPROVE_OPTION)
            fileChooser.selectedFile.absolutePath
        else
            null
    }

    private fun classDiagramGenerator(diagram: IClassDiagram): List<GsnSacm> {
        val ret = mutableListOf<GsnSacm>()
        if (diagram.presentations.any { it.model is IClass })
            ret.addAll(GSNGenerator(GSNDiagramType.ClassInClassDiagram, diagram).gsns)
        if (diagram.presentations.any { it.model is IInstanceSpecification })
            ret.addAll(GSNGenerator(GSNDiagramType.ObjectsInClassDiagram, diagram).gsns)
        return ret
    }
}