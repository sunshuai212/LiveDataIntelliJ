package com.chomper.livedata

import com.intellij.psi.*
import org.jetbrains.kotlin.psi.*

/**
 * Created by kgmyshin on 2015/06/07.
 */
object PsiUtils {

    fun getClass(psiType: PsiType): PsiClass? {
        return if (psiType is PsiClassType) {
            psiType.resolve()
        } else null
    }

    fun isEventBusReceiver(psiElement: PsiElement): Boolean {
        if (psiElement is PsiMethod) {
            val modifierList = psiElement.modifierList
            for (psiAnnotation in modifierList.annotations) {
                if (psiAnnotation.qualifiedName == "org.greenrobot.eventbus.Subscribe") {
                    return true
                }
            }
        }
        return false
    }

    fun isEventBusPost(psiElement: PsiElement): Boolean {
        if (psiElement is PsiCallExpression) {
            val method = psiElement.resolveMethod()
            if (method != null) {
                val name = method.name
                val parent = method.parent
                if (name != null && name == "postEvent" && parent is PsiClass) {
                    if (isEventBusClass(parent) || isSuperClassEventBus(parent)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun isLiveDataPost(psiElement: PsiElement): Boolean {
        if (psiElement is KtClass) {
            return true
        }
        return false
    }

    private fun isEventBusClass(psiClass: PsiClass): Boolean {
        try {
            return psiClass.name == "LiveBusProvider"
        } catch (e: Exception) {
            println(e.toString())
            return false
        }

    }

    private fun isSuperClassEventBus(psiClass: PsiClass): Boolean {
        val supers = psiClass.supers
        if (supers.size == 0) {
            return false
        }
        for (superClass in supers) {
            try {
                if (superClass.name == "EventBus") {
                    return true
                }
            } catch (e: Exception) {
                println(e.toString())
            }

            //            if (superClass.getName().equals("EventBus")) {
            //                return true;
            //            }
        }
        return false
    }

}
