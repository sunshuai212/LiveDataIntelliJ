package com.chomper.livedata

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtilBase
import com.intellij.ui.awt.RelativePoint

class LineMarkerKt :LineMarkerProvider{

    val ICON = IconLoader.getIcon("/icons/icon.png")

    val MAX_USAGES = 100

    private val `SHOW-SENDERS` =
        GutterIconNavigationHandler<PsiElement> { e, psiElement ->
            if (psiElement is PsiMethod) {
                val project = psiElement.getProject()
                val javaPsiFacade = JavaPsiFacade.getInstance(project)
                val eventBusClass = javaPsiFacade.findClass(
                    "com.bilibili.bililive.videoliveplayer.ui.eventbus.LiveBusProvider",
                    GlobalSearchScope.allScope(project)
                )
                //PsiClass eventBusClass = javaPsiFacade.findClass("org.greenrobot.eventbus.EventBus", GlobalSearchScope.projectScope(project));
                val postMethod = eventBusClass!!.findMethodsByName("postEvent", false)[0]
                val eventClass = (psiElement.parameterList.parameters[0].typeElement!!.type as PsiClassType).resolve()
                ShowUsagesAction(SenderFilter(eventClass!!)).startFindUsages(
                    postMethod,
                    RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES
                )
            }
        }

    private val SHOW_RECEIVERS =
        GutterIconNavigationHandler<PsiElement> { e, psiElement ->
            if (psiElement is PsiMethodCallExpression) {
                 val expressionTypes = psiElement.argumentList.expressionTypes
                 if (expressionTypes.size > 0) {
                     val eventClass = PsiUtils.getClass(expressionTypes[0])
                     if (eventClass != null) {
                         ShowUsagesAction(ReceiverFilter()).startFindUsages(
                             eventClass,
                             RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES
                         )
                     }
                 }
            }
        }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (PsiUtils.isLiveDataPost(psiElement)) {
            return LineMarkerInfo(
                psiElement, psiElement.textRange, ICON,
                Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
                GutterIconRenderer.Alignment.LEFT
            )
        } else if (PsiUtils.isEventBusReceiver(psiElement)) {
            return LineMarkerInfo(
                psiElement, psiElement.textRange, ICON,
                Pass.UPDATE_ALL, null, `SHOW-SENDERS`,
                GutterIconRenderer.Alignment.LEFT
            )
        }
        return null
    }


    override fun collectSlowLineMarkers(list: List<PsiElement>, collection: Collection<LineMarkerInfo<*>>) {

    }
}