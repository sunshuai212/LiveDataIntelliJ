/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chomper.livedata

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.FileColorManager
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.usages.TextChunk
import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsagePresentation
import com.intellij.usages.impl.GroupNode
import com.intellij.usages.impl.UsageNode
import com.intellij.usages.impl.UsageViewImpl
import com.intellij.usages.rules.UsageInFile
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.UIUtil

import javax.swing.*
import javax.swing.table.TableCellRenderer
import java.awt.*

internal class ShowUsagesTableCellRenderer(private val myUsageView: UsageViewImpl) : TableCellRenderer {

    override fun getTableCellRendererComponent(
        list: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val usageNode = if (value is UsageNode) value else null

        val usage = usageNode?.usage

        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        val fileBgColor = getBackgroundColor(isSelected, usage)
        val bg = UIUtil.getListSelectionBackground()
        val fg = UIUtil.getListSelectionForeground()
        panel.background = if (isSelected) bg else fileBgColor ?: list.background
        panel.foreground = if (isSelected) fg else list.foreground

        if (usage == null || usageNode is ShowUsagesAction.StringNode) {
            panel.layout = BorderLayout()
            if (column == 0) {
                panel.add(JLabel("<html><body><b>$value</b></body></html>", SwingConstants.CENTER))
            }
            return panel
        }


        val textChunks = SimpleColoredComponent()
        textChunks.ipad = Insets(0, 0, 0, 0)
        textChunks.border = null

        if (column == 0) {
            val parent = usageNode.parent as GroupNode
            appendGroupText(parent, panel, fileBgColor)
            if (usage === ShowUsagesAction.MORE_USAGES_SEPARATOR) {
                textChunks.append("...<")
                textChunks.append("more usages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                textChunks.append(">...")
            }
        } else if (usage !== ShowUsagesAction.MORE_USAGES_SEPARATOR) {
            val presentation = usage.presentation
            val text = presentation.text

            if (column == 1) {
                val icon = presentation.icon
                textChunks.setIcon(icon ?: EmptyIcon.ICON_16)
                if (text.size != 0) {
                    val attributes = if (isSelected)
                        SimpleTextAttributes(bg, fg, fg, SimpleTextAttributes.STYLE_ITALIC)
                    else
                        deriveAttributesWithColor(text[0].simpleAttributesIgnoreBackground, fileBgColor)
                    textChunks.append(text[0].text, attributes)
                }
            } else if (column == 2) {
                for (i in 1 until text.size) {
                    val textChunk = text[i]
                    val attrs = textChunk.simpleAttributesIgnoreBackground
                    val attributes = if (isSelected)
                        SimpleTextAttributes(bg, fg, fg, attrs.style)
                    else
                        deriveAttributesWithColor(attrs, fileBgColor)
                    textChunks.append(textChunk.text, attributes)
                }
            } else {
                assert(false) { column }
            }
        }
        panel.add(textChunks)
        return panel
    }

    private fun deriveAttributesWithColor(attributes: SimpleTextAttributes, fileBgColor: Color?): SimpleTextAttributes {
        var attributes = attributes
        if (fileBgColor != null) {
            attributes = attributes.derive(-1, null, fileBgColor, null)
        }
        return attributes
    }

    private fun getBackgroundColor(isSelected: Boolean, usage: Usage?): Color? {
        var fileBgColor: Color? = null
        if (isSelected) {
            fileBgColor = UIUtil.getListSelectionBackground()
        } else {
            val virtualFile = if (usage is UsageInFile) usage.file else null
            if (virtualFile != null) {
                val project = myUsageView.project
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile != null && psiFile.isValid) {
                    val color = FileColorManager.getInstance(project).getRendererBackground(psiFile)
                    if (color != null) fileBgColor = color
                }
            }
        }
        return fileBgColor
    }

    private fun appendGroupText(node: GroupNode?, panel: JPanel, fileBgColor: Color?) {
        val group = node?.group ?: return
        val parentGroup = node.parent as GroupNode
        appendGroupText(parentGroup, panel, fileBgColor)
        if (node.canNavigateToSource()) {
            val renderer = SimpleColoredComponent()

            renderer.icon = group.getIcon(false)
            val attributes = deriveAttributesWithColor(SimpleTextAttributes.REGULAR_ATTRIBUTES, fileBgColor)
            renderer.append(group.getText(myUsageView), attributes)
            renderer.append(" ", attributes)
            renderer.ipad = Insets(0, 0, 0, 0)
            renderer.border = null
            panel.add(renderer)
        }
    }
}