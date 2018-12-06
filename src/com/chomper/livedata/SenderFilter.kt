package com.chomper.livedata

import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter

import javax.swing.*

/**
 * Created by kgmyshin on 2015/06/07.
 *
 * modify by likfe ( https://github.com/likfe/ ) in 2016/09/05
 *
 * add try-catch
 */
class SenderFilter internal constructor(private val eventClass: PsiClass) : Filter {

    override fun shouldShow(usage: Usage): Boolean {
        var element = (usage as UsageInfo2UsageAdapter).element
        if (element is PsiReferenceExpression) {
            if ((element.parent) is PsiMethodCallExpression) {
                val callExpression = element.parent as PsiMethodCallExpression
                val types = callExpression.argumentList.expressionTypes
                for (type in types) {
                    if (PsiUtils.getClass(type)!!.name == eventClass.name) {
                        // pattern : EventBus.getDefault().post(new Event());
                        return true
                    }
                }
                if ((element.parent) is PsiExpressionStatement) {
                    if ((element.parent.parent) is PsiCodeBlock) {
                        val codeBlock = element.parent.parent as PsiCodeBlock
                        val statements = codeBlock.statements
                        for (statement in statements) {
                            if (statement is PsiDeclarationStatement) {
                                val elements = statement.declaredElements
                                for (variable in elements) {
                                    if (variable is PsiLocalVariable) {
                                        val psiClass = PsiUtils.getClass(variable.typeElement.type)
                                        try {
                                            if (psiClass!!.name == eventClass.name) {
                                                // pattern :
                                                //   Event event = new Event();
                                                //   EventBus.getDefault().post(event);
                                                return true
                                            }
                                        } catch (e: NullPointerException) {
                                            println(e.toString())
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false
    }

    companion object {

        private val ICON = IconLoader.getIcon("/icons/icon.png")
    }
}
